package im.actor.server.group

import java.time.{ LocalDateTime, ZoneOffset }

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.pattern.pipe
import akka.persistence.{ RecoveryCompleted, RecoveryFailure }
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import com.trueaccord.scalapb.GeneratedMessage
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.groups.{ UpdateGroupInvite, UpdateGroupUserInvited, UpdateGroupUserKick, UpdateGroupUserLeave }
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, _ }
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.file.Avatar
import im.actor.server.models.UserState.Registered
import im.actor.server.office.PeerOffice.MessageSentComplete
import im.actor.server.office.{ StopOffice, PeerOffice, PushTexts }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.user.{ UserOffice, UserOfficeRegion }
import im.actor.server.util.ACLUtils._
import im.actor.server.util.IdUtils._
import im.actor.server.util.{ FileStorageAdapter, GroupServiceMessages, HistoryUtils, UserUtils }
import im.actor.server.{ models, persist ⇒ p }
import im.actor.utils.cache.CacheHelpers._

private[group] case class Member(
  userId:        Int,
  inviterUserId: Int,
  invitedAt:     DateTime
)

private[group] case class Bot(
  userId: Int,
  token:  String
)

private[group] case class Group(
  id:               Int,
  accessHash:       Long,
  creatorUserId:    Int,
  createdAt:        DateTime,
  members:          Map[Int, Member],
  invitedUserIds:   Set[Int],
  title:            String,
  isPublic:         Boolean,
  lastSenderId:     Option[Int],
  lastReceivedDate: Option[DateTime],
  lastReadDate:     Option[DateTime],
  bot:              Option[Bot],
  avatar:           Option[Avatar]
)

trait GroupEvent

private[group] object GroupOfficeActor {

  private case class Initialized(groupUsersIds: Set[Int], invitedUsersIds: Set[Int], isPublic: Boolean)

  ActorSerializer.register(5001, classOf[GroupCommands.Create])
  ActorSerializer.register(5002, classOf[GroupCommands.CreateResponse])
  ActorSerializer.register(5003, classOf[GroupCommands.Invite])
  ActorSerializer.register(5004, classOf[GroupCommands.Join])
  ActorSerializer.register(5005, classOf[GroupCommands.Kick])
  ActorSerializer.register(5006, classOf[GroupCommands.Leave])
  ActorSerializer.register(5007, classOf[GroupCommands.SendMessage])
  ActorSerializer.register(5008, classOf[GroupCommands.MessageReceived])
  ActorSerializer.register(5009, classOf[GroupCommands.MessageRead])
  ActorSerializer.register(5010, classOf[GroupCommands.UpdateAvatar])

  ActorSerializer.register(6001, classOf[GroupEvents.MessageRead])
  ActorSerializer.register(6002, classOf[GroupEvents.MessageReceived])
  ActorSerializer.register(6003, classOf[GroupEvents.UserInvited])
  ActorSerializer.register(6004, classOf[GroupEvents.UserJoined])
  ActorSerializer.register(6005, classOf[GroupEvents.Created])
  ActorSerializer.register(6006, classOf[GroupEvents.BotAdded])
  ActorSerializer.register(6007, classOf[GroupEvents.UserKicked])
  ActorSerializer.register(6008, classOf[GroupEvents.UserLeft])
  ActorSerializer.register(6009, classOf[GroupEvents.AvatarUpdated])

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    userOfficeRegion:    UserOfficeRegion,
    fsAdapter:           FileStorageAdapter
  ): Props = Props(classOf[GroupOfficeActor], db, seqUpdManagerRegion, userOfficeRegion, fsAdapter)
}

private[group] final class GroupOfficeActor(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  userOfficeRegion:    UserOfficeRegion,
  fsAdapter:           FileStorageAdapter
) extends PeerOffice with GroupCommandHandlers with ActorLogging with Stash with GroupsImplicits {

  import GroupCommands._
  import GroupErrors._
  import GroupOfficeActor._
  import HistoryUtils._
  import SeqUpdatesManager._
  import UserUtils._

  implicit private val system: ActorSystem = context.system
  implicit private val ec: ExecutionContext = context.dispatcher

  implicit private val timeout: Timeout = Timeout(10.seconds)

  protected val groupId = self.path.name.toInt

  override def persistenceId = s"group_${groupId}"

  context.setReceiveTimeout(15.minutes)

  private val MaxCacheSize = 100L

  type AuthIdRandomId = (Long, Long)
  implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  def receiveCommand = creating

  def creating: Receive = {
    case Create(groupId, creatorUserId, creatorAuthId, title, randomId, userIds) ⇒
      val date = new DateTime

      val rng = ThreadLocalRandom.current()

      val accessHash = rng.nextLong()
      val botUserId = nextIntId(rng)
      val botToken = accessToken(rng)

      val events = Vector(
        GroupEvents.Created(creatorUserId, accessHash, title),
        GroupEvents.BotAdded(botUserId, botToken)
      )

      userIds.filterNot(_ == creatorUserId) foreach { userId ⇒
        val randomId = rng.nextLong()
        context.parent ! Invite(groupId, userId, creatorUserId, creatorAuthId, randomId)
      }

      var stateMaybe: Option[Group] = None

      persist[GeneratedMessage](events) {
        case evt: GroupEvents.Created ⇒
          val group = initState(evt)

          stateMaybe = Some(group)

          val serviceMessage = GroupServiceMessages.groupCreated

          val update = UpdateGroupInvite(groupId = groupId, inviteUserId = creatorUserId, date = date.getMillis, randomId = randomId)

          db.run(
            for {
              _ ← p.Group.create(
                models.Group(groupId, group.creatorUserId, group.accessHash, group.title, group.isPublic, group.createdAt, ""),
                randomId
              )
              _ ← p.GroupUser.create(groupId, creatorUserId, creatorUserId, date, None)
              _ ← HistoryUtils.writeHistoryMessage(
                models.Peer.privat(creatorUserId),
                models.Peer.group(group.id),
                date,
                randomId,
                serviceMessage.header,
                serviceMessage.toByteArray
              )
              SeqState(seq, state) ← broadcastClientUpdate(creatorUserId, creatorAuthId, update, None, false)
            } yield CreateResponse(group.accessHash, seq, state, date.getMillis)
          ) pipeTo sender()

        case evt @ GroupEvents.BotAdded(userId, token) ⇒
          stateMaybe = stateMaybe map { state ⇒
            val newState = updateState(evt, state)
            context become working(newState)
            newState
          }

          val rng = ThreadLocalRandom.current()

          val bot = models.User(
            id = userId,
            accessSalt = nextAccessSalt(rng),
            name = "Bot",
            countryCode = "US",
            sex = models.NoSex,
            state = Registered,
            createdAt = LocalDateTime.now(ZoneOffset.UTC),
            isBot = true
          )

          db.run(DBIO.sequence(Seq(
            p.User.create(bot),
            p.GroupBot.create(groupId, bot.id, token)
          )))
      }
  }

  def working(group: Group): Receive = {
    case SendMessage(groupId, senderUserId, senderAuthId, hash, randomId, message, isFat) ⇒
      if (hash == group.accessHash) {
        if (hasMember(group, senderUserId) || isBot(group, senderUserId)) {
          context.become {
            case MessageSentComplete ⇒
              unstashAll()
              context become working(group)
            case msg ⇒
              stash()
          }

          val date = new DateTime
          val replyTo = sender()

          sendMessage(group, senderUserId, senderAuthId, group.members.keySet, randomId, date, message, isFat) onComplete {
            case Success(seqstatedate) ⇒
              replyTo ! seqstatedate
              self ! MessageSentComplete
            case Failure(e) ⇒
              replyTo ! Status.Failure(e)
              log.error(e, "Failed to send message")
              self ! MessageSentComplete
          }
        } else {
          sender() ! Status.Failure(NotAMember)
        }
      } else {
        sender() ! Status.Failure(InvalidAccessHash)
      }
    case MessageReceived(groupId, receiverUserId, _, date, receivedDate) ⇒
      if (!group.lastReceivedDate.exists(_.getMillis >= date) && !group.lastSenderId.contains(receiverUserId)) {
        persist(GroupEvents.MessageReceived(date)) { evt ⇒
          context become working(updateState(evt, group))

          val update = UpdateMessageReceived(groupPeerStruct(groupId), date, receivedDate)

          db.run(for {
            otherAuthIds ← p.AuthId.findIdByUserIds(group.members.keySet - receiverUserId)
            _ ← persistAndPushUpdates(otherAuthIds.toSet, update, None)
          } yield {

            // TODO: Move to a History Writing subsystem
            db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.group(groupId), new DateTime(date)))
          }) onFailure {
            case e ⇒
              log.error(e, "Failed to mark messages received")
          }
        }
      }
    case MessageRead(groupId, readerUserId, readerAuthId, date, readDate) ⇒
      db.run(broadcastOtherDevicesUpdate(readerUserId, readerAuthId, UpdateMessageReadByMe(groupPeerStruct(groupId), date), None))

      if (!group.lastReadDate.exists(_.getMillis >= date) && !group.lastSenderId.contains(readerUserId)) {
        persist(GroupEvents.MessageRead(readerUserId, date)) { evt ⇒
          context become working(updateState(evt, group))

          if (group.invitedUserIds.contains(readerUserId)) {

            db.run(for (_ ← p.GroupUser.setJoined(groupId, readerUserId, LocalDateTime.now(ZoneOffset.UTC))) yield {
              val randomId = ThreadLocalRandom.current().nextLong()
              self ! SendMessage(groupId, readerUserId, readerAuthId, group.accessHash, randomId, GroupServiceMessages.userJoined)
            })
          }

          val update = UpdateMessageRead(groupPeerStruct(groupId), date, readDate)
          val memberIds = group.members.keySet

          db.run(p.AuthId.findIdByUserIds(memberIds) flatMap { authIds ⇒
            persistAndPushUpdates(authIds.toSet, update, None) andThen
              markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date))
          }) onFailure {
            case e ⇒
              log.error(e, "Failed to mark messages read")
          }
        }
      }
    case Invite(groupId, inviteeUserId, inviterUserId, inviterAuthId, randomId) ⇒
      if (!hasMember(group, inviteeUserId)) {
        val dateMillis = System.currentTimeMillis()

        persist(GroupEvents.UserInvited(inviteeUserId, inviterUserId, dateMillis)) { evt ⇒
          context become working(updateState(evt, group))

          val replyTo = sender()
          val date = new DateTime(dateMillis)

          invite(group, inviteeUserId, inviterUserId, inviterAuthId, randomId, date) pipeTo replyTo onFailure {
            case e ⇒ replyTo ! Status.Failure(e)
          }
        }
      } else {
        sender() ! Status.Failure(GroupErrors.UserAlreadyInvited)
      }
    case Join(groupId, joiningUserId, joiningUserAuthId, invitingUserId) ⇒
      if (!hasMember(group, joiningUserId)) {
        val date = System.currentTimeMillis()

        persist(GroupEvents.UserJoined(joiningUserId, invitingUserId, date)) { evt ⇒
          context become working(updateState(evt, group))

          val replyTo = sender()
          val memberIds = group.members.keySet

          val action: DBIO[(SeqStateDate, Vector[Sequence], Long)] = {
            context become working(updateState(evt, group))

            val isMember = memberIds.contains(joiningUserId)

            // TODO: Move to view
            for {
              updates ← if (isMember) {
                DBIO.failed(UserAlreadyJoined)
              } else {
                val date = new DateTime
                val dateMillis = date.getMillis
                val randomId = ThreadLocalRandom.current().nextLong()
                for {
                  _ ← p.GroupUser.create(groupId, joiningUserId, invitingUserId, date, Some(LocalDateTime.now(ZoneOffset.UTC)))
                  seqstatedate ← DBIO.from(sendMessage(group, joiningUserId, joiningUserAuthId, memberIds, randomId, date, GroupServiceMessages.userJoined, isFat = true))
                } yield (seqstatedate, memberIds.toVector :+ invitingUserId, randomId)
              }
            } yield updates
          }
          db.run(action) pipeTo replyTo onFailure {
            case e ⇒ replyTo ! Status.Failure(e)
          }
        }
      } else {
        sender() ! Status.Failure(GroupErrors.UserAlreadyInvited)
      }
    case Kick(groupId, kickedUserId, kickerUserId, kickerAuthId, randomId) ⇒
      val replyTo = sender()
      val date = new DateTime

      persist(GroupEvents.UserKicked(kickedUserId, kickerUserId, date.getMillis)) { evt ⇒
        context become working(updateState(evt, group))

        val update = UpdateGroupUserKick(groupId, kickedUserId, kickerUserId, date.getMillis, randomId)
        val serviceMessage = GroupServiceMessages.userKicked(kickedUserId)

        val action: DBIO[SeqStateDate] = {
          for {
            _ ← p.GroupUser.delete(groupId, kickedUserId)
            _ ← p.GroupInviteToken.revoke(groupId, kickedUserId) //TODO: move to cleanup helper, with all cleanup code and use in kick/leave
            (SeqState(seq, state), _) ← broadcastClientAndUsersUpdate(kickerUserId, kickerAuthId, group.members.keySet - kickedUserId, update, Some(PushTexts.Kicked), isFat = false)
            // TODO: Move to a History Writing subsystem
            _ ← HistoryUtils.writeHistoryMessage(
              models.Peer.privat(kickerUserId),
              models.Peer.group(groupId),
              date,
              randomId,
              serviceMessage.header,
              serviceMessage.toByteArray
            )
          } yield SeqStateDate(seq, state, date.getMillis)
        }
        db.run(action) pipeTo replyTo onFailure {
          case e ⇒ replyTo ! Status.Failure(e)
        }
      }
    case Leave(groupId, userId, authId, randomId) ⇒
      val replyTo = sender()
      val date = new DateTime

      persist(GroupEvents.UserLeft(userId, date.getMillis)) { evt ⇒
        context become working(updateState(evt, group))

        val update = UpdateGroupUserLeave(groupId, userId, date.getMillis, randomId)
        val serviceMessage = GroupServiceMessages.userLeft(userId)
        val memberIds = group.members.keySet

        val action: DBIO[SeqStateDate] = {
          for {
            _ ← p.GroupUser.delete(groupId, userId)
            _ ← p.GroupInviteToken.revoke(groupId, userId) //TODO: move to cleanup helper, with all cleanup code and use in kick/leave
            (SeqState(seq, state), _) ← broadcastClientAndUsersUpdate(userId, authId, memberIds - userId, update, Some(PushTexts.Left), isFat = false)
            // TODO: Move to a History Writing subsystem
            _ ← HistoryUtils.writeHistoryMessage(
              models.Peer.privat(userId),
              models.Peer.group(groupId),
              date,
              randomId,
              serviceMessage.header,
              serviceMessage.toByteArray
            )
          } yield SeqStateDate(seq, state, date.getMillis)
        }
        db.run(action) pipeTo replyTo onFailure {
          case e ⇒ replyTo ! Status.Failure(e)
        }
      }
    case UpdateAvatar(groupId, clientUserId, clientAuthId, fileLocationOpt, randomId) ⇒
      updateAvatar(group, sender(), clientUserId, clientAuthId, fileLocationOpt, randomId)
    case StopOffice     ⇒ context stop self
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopOffice)
  }

  var groupStateMaybe: Option[Group] = None

  override def receiveRecover = {
    case evt: GroupEvents.Created ⇒
      groupStateMaybe = Some(initState(evt))
    case evt: GroupEvent ⇒
      groupStateMaybe = groupStateMaybe map (updateState(evt, _))
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
    case RecoveryCompleted ⇒
      groupStateMaybe match {
        case Some(group) ⇒ context become working(group)
        case None        ⇒ context become creating
      }
    case unmatched ⇒
      log.error("Unmatched recovery event {}", unmatched)
  }

  private def initState(evt: GroupEvents.Created): Group = {
    Group(
      id = groupId,
      accessHash = evt.accessHash,
      title = evt.title,
      creatorUserId = evt.creatorUserId,
      createdAt = evt.createdAt,
      members = Map(evt.creatorUserId → Member(evt.creatorUserId, evt.creatorUserId, evt.createdAt)),
      isPublic = false,
      lastSenderId = None,
      lastReceivedDate = None,
      lastReadDate = None,
      bot = None,
      invitedUserIds = Set.empty,
      avatar = None
    )
  }

  protected def updateState(evt: GroupEvent, state: Group): Group = {
    evt match {
      case GroupEvents.BotAdded(userId, token) ⇒
        state.copy(bot = Some(Bot(userId, token)))
      case GroupEvents.MessageReceived(date) ⇒
        state.copy(lastReceivedDate = Some(new DateTime(date)))
      case GroupEvents.MessageRead(userId, date) ⇒
        state.copy(
          lastReadDate = Some(new DateTime(date)),
          invitedUserIds = state.invitedUserIds - userId
        )
      case GroupEvents.UserInvited(userId, inviterUserId, invitedAt) ⇒
        state.copy(
          members = state.members + (userId → Member(userId, inviterUserId, new DateTime(invitedAt))),
          invitedUserIds = state.invitedUserIds + userId
        )
      case GroupEvents.UserJoined(userId, inviterUserId, invitedAt) ⇒
        state.copy(
          members = state.members + (userId → Member(userId, inviterUserId, new DateTime(invitedAt)))
        )
      case GroupEvents.UserKicked(userId, kickerUserId, _) ⇒
        state.copy(members = state.members - userId)
      case GroupEvents.UserLeft(userId, _) ⇒
        state.copy(members = state.members - userId)
      case GroupEvents.AvatarUpdated(avatar) ⇒
        state.copy(avatar = avatar)
    }
  }

  private def hasMember(group: Group, userId: Int): Boolean = group.members.keySet.contains(userId)

  private def isBot(group: Group, userId: Int): Boolean = group.bot map (_.userId == userId) getOrElse (false)

  private def sendMessage(group: Group, senderUserId: Int, senderAuthId: Long, groupUsersIds: Set[Int], randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Future[SeqStateDate] = {
    val groupPeer = groupPeerStruct(groupId)
    val memberIds = group.members.keySet

    withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) { () ⇒
      memberIds foreach { userId ⇒
        if (userId != senderUserId) {
          UserOffice.deliverMessage(userId, groupPeer, senderUserId, randomId, date, message, isFat)
        }
      }

      for {
        SeqState(seq, state) ← UserOffice.deliverOwnMessage(senderUserId, groupPeer, senderAuthId, randomId, date, message, isFat)
      } yield {
        db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.group(groupPeer.id), date, randomId, message.header, message.toByteArray))
        SeqStateDate(seq, state, date.getMillis)
      }
    }
  }

  private def invite(group: Group, userId: Int, inviterUserId: Int, inviterAuthId: Long, randomId: Long, date: DateTime): Future[SeqStateDate] = {
    val dateMillis = date.getMillis
    val memberIds = group.members.keySet

    db.run {
      val inviteeUpdate = UpdateGroupInvite(groupId = groupId, randomId = randomId, inviteUserId = inviterUserId, date = dateMillis)

      val userAddedUpdate = UpdateGroupUserInvited(groupId = groupId, userId = userId, inviterUserId = inviterUserId, date = dateMillis, randomId = randomId)
      val serviceMessage = GroupServiceMessages.userInvited(userId)

      for {
        _ ← p.GroupUser.create(groupId, userId, inviterUserId, date, None)
        _ ← broadcastUserUpdate(userId, inviteeUpdate, pushText = Some(PushTexts.Invited), isFat = true)
        // TODO: #perf the following broadcasts do update serializing per each user
        _ ← DBIO.sequence(memberIds.toSeq.filterNot(_ == inviterUserId).map(broadcastUserUpdate(_, userAddedUpdate, Some(PushTexts.Added), isFat = true))) // use broadcastUsersUpdate maybe?
        seqstate ← broadcastClientUpdate(inviterUserId, inviterAuthId, userAddedUpdate, pushText = None, isFat = true)
        // TODO: Move to a History Writing subsystem
        _ ← HistoryUtils.writeHistoryMessage(
          models.Peer.privat(inviterUserId),
          models.Peer.group(groupId),
          date,
          randomId,
          serviceMessage.header,
          serviceMessage.toByteArray
        )
      } yield {
        SeqStateDate(seqstate.seq, seqstate.state, dateMillis)
      }
    }
  }

  private def initialize(): Future[Initialized] = {
    db.run(for {
      groupOpt ← p.Group.find(groupId)
      groupUsers ← p.GroupUser.find(groupId)
    } yield {
      val (groupUsersIds, invitedUsersIds) = groupUsers.foldLeft((Set.empty[Int], Set.empty[Int])) {
        case ((group, invited), groupUser) ⇒
          groupUser.joinedAt match {
            case Some(_) ⇒ (group + groupUser.userId, invited)
            case None    ⇒ (group + groupUser.userId, invited + groupUser.userId)
          }
      }

      groupOpt match {
        case Some(group) ⇒
          Initialized(groupUsersIds, invitedUsersIds, group.isPublic)
        case None ⇒
          throw new Exception(s"Cannot find group")
      }
    })
  }

  private def broadcastGroupMessage(senderUserId: Int, senderAuthId: Long, groupUsersIds: Set[Int], update: UpdateMessage, isFat: Boolean) = {
    val updateHeader = update.header
    val updateData = update.toByteArray
    val (updateUserIds, updateGroupIds) = updateRefs(update)

    for {
      clientUser ← getUserUnsafe(senderUserId)
      seqstates ← DBIO.sequence((groupUsersIds - senderUserId).toSeq map { userId ⇒
        for {
          pushText ← getPushText(update.message, clientUser, userId)
          seqstates ← broadcastUserUpdate(userId, updateHeader, updateData, updateUserIds, updateGroupIds, Some(pushText), Some(groupPeerStruct(groupId)), isFat)
        } yield seqstates
      }) map (_.flatten)
      selfseqstates ← notifyUserUpdate(senderUserId, senderAuthId, updateHeader, updateData, updateUserIds, updateGroupIds, None, None, isFat)
    } yield seqstates ++ selfseqstates
  }

}