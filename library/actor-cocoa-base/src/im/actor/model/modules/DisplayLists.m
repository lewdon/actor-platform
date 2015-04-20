//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/DisplayLists.java
//

#line 1 "/Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/modules/DisplayLists.java"

#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "im/actor/model/droidkit/engine/ListEngine.h"
#include "im/actor/model/droidkit/engine/ListEngineDisplayExt.h"
#include "im/actor/model/entity/Dialog.h"
#include "im/actor/model/entity/Message.h"
#include "im/actor/model/entity/Peer.h"
#include "im/actor/model/modules/BaseModule.h"
#include "im/actor/model/modules/Contacts.h"
#include "im/actor/model/modules/DisplayLists.h"
#include "im/actor/model/modules/Messages.h"
#include "im/actor/model/modules/Modules.h"
#include "im/actor/model/modules/SearchModule.h"
#include "im/actor/model/mvvm/BindedDisplayList.h"
#include "im/actor/model/mvvm/MVVMEngine.h"
#include "java/lang/RuntimeException.h"
#include "java/util/HashMap.h"

@interface ImActorModelModulesDisplayLists () {
 @public
  AMBindedDisplayList *dialogGlobalList_;
  AMBindedDisplayList *contactsGlobalList_;
  JavaUtilHashMap *chatsGlobalLists_;
}
@end

J2OBJC_FIELD_SETTER(ImActorModelModulesDisplayLists, dialogGlobalList_, AMBindedDisplayList *)
J2OBJC_FIELD_SETTER(ImActorModelModulesDisplayLists, contactsGlobalList_, AMBindedDisplayList *)
J2OBJC_FIELD_SETTER(ImActorModelModulesDisplayLists, chatsGlobalLists_, JavaUtilHashMap *)

@interface ImActorModelModulesDisplayLists_$1 () {
 @public
  ImActorModelModulesDisplayLists *this$0_;
}
@end

J2OBJC_FIELD_SETTER(ImActorModelModulesDisplayLists_$1, this$0_, ImActorModelModulesDisplayLists *)

@interface ImActorModelModulesDisplayLists_$2 () {
 @public
  ImActorModelModulesDisplayLists *this$0_;
  AMPeer *val$peer_;
}
@end

J2OBJC_FIELD_SETTER(ImActorModelModulesDisplayLists_$2, this$0_, ImActorModelModulesDisplayLists *)
J2OBJC_FIELD_SETTER(ImActorModelModulesDisplayLists_$2, val$peer_, AMPeer *)


#line 18
@implementation ImActorModelModulesDisplayLists


#line 28
- (instancetype)initWithImActorModelModulesModules:(ImActorModelModulesModules *)modules {
  if (self =
#line 29
  [super initWithImActorModelModulesModules:modules]) {
    chatsGlobalLists_ =
#line 26
    [[JavaUtilHashMap alloc] init];
  }
  return self;
}


#line 32
- (AMBindedDisplayList *)getContactsGlobalList {
  
#line 33
  AMMVVMEngine_checkMainThread();
  
#line 35
  if (contactsGlobalList_ == nil) {
    contactsGlobalList_ = [self buildNewContactListWithBoolean:YES];
  }
  
#line 39
  return contactsGlobalList_;
}


#line 42
- (AMBindedDisplayList *)getDialogsGlobalList {
  
#line 43
  AMMVVMEngine_checkMainThread();
  
#line 45
  if (dialogGlobalList_ == nil) {
    dialogGlobalList_ = [self buildNewDialogsListWithBoolean:YES];
  }
  
#line 49
  return dialogGlobalList_;
}


#line 52
- (AMBindedDisplayList *)getMessagesGlobalListWithAMPeer:(AMPeer *)peer {
  
#line 53
  AMMVVMEngine_checkMainThread();
  
#line 55
  if (![((JavaUtilHashMap *) nil_chk(chatsGlobalLists_)) containsKeyWithId:peer]) {
    (void) [chatsGlobalLists_ putWithId:peer withId:[self buildNewChatListWithAMPeer:peer withBoolean:YES]];
  }
  
#line 59
  return [chatsGlobalLists_ getWithId:peer];
}


#line 62
- (AMBindedDisplayList *)buildNewDialogsListWithBoolean:(jboolean)isGlobalList {
  
#line 63
  AMMVVMEngine_checkMainThread();
  
#line 65
  id<DKListEngine> dialogsEngine = [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) getDialogsEngine];
  if (!([DKListEngineDisplayExt_class_() isInstance:dialogsEngine])) {
    @throw [[JavaLangRuntimeException alloc] initWithNSString:@"Dialogs ListEngine must implement ListEngineDisplayExt for using global list"];
  }
  
#line 70
  id<AMBindedDisplayList_BindHook> hook = nil;
  if (isGlobalList) {
    hook = [[ImActorModelModulesDisplayLists_$1 alloc] initWithImActorModelModulesDisplayLists:self];
  }
  
#line 85
  AMBindedDisplayList *displayList = [[AMBindedDisplayList alloc] initWithDKListEngineDisplayExt:(id<DKListEngineDisplayExt>) check_protocol_cast(dialogsEngine, @protocol(DKListEngineDisplayExt)) withBoolean:
#line 86
  isGlobalList withInt:ImActorModelModulesDisplayLists_LOAD_PAGE withInt:ImActorModelModulesDisplayLists_LOAD_GAP withAMBindedDisplayList_BindHook:hook];
  [displayList initTopWithBoolean:NO];
  return displayList;
}


#line 91
- (AMBindedDisplayList *)buildNewContactListWithBoolean:(jboolean)isGlobalList {
  
#line 92
  AMMVVMEngine_checkMainThread();
  
#line 94
  id<DKListEngine> contactsEngine = [((ImActorModelModulesContacts *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getContactsModule])) getContacts];
  if (!([DKListEngineDisplayExt_class_() isInstance:contactsEngine])) {
    @throw [[JavaLangRuntimeException alloc] initWithNSString:@"Contacts ListEngine must implement ListEngineDisplayExt for using global list"];
  }
  
#line 99
  AMBindedDisplayList *contactList = [[AMBindedDisplayList alloc] initWithDKListEngineDisplayExt:(id<DKListEngineDisplayExt>) check_protocol_cast(contactsEngine, @protocol(DKListEngineDisplayExt)) withBoolean:
#line 100
  isGlobalList withInt:ImActorModelModulesDisplayLists_LOAD_PAGE withInt:ImActorModelModulesDisplayLists_LOAD_GAP withAMBindedDisplayList_BindHook:nil];
  [contactList initTopWithBoolean:NO];
  return contactList;
}


#line 105
- (AMBindedDisplayList *)buildNewChatListWithAMPeer:(AMPeer *)peer
                                        withBoolean:(jboolean)isGlobalList {
  
#line 106
  AMMVVMEngine_checkMainThread();
  
#line 108
  id<DKListEngine> messagesEngine = [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) getConversationEngineWithAMPeer:peer];
  if (!([DKListEngineDisplayExt_class_() isInstance:messagesEngine])) {
    @throw [[JavaLangRuntimeException alloc] initWithNSString:@"Conversation ListEngine must implement ListEngineDisplayExt for using global list"];
  }
  
#line 113
  id<AMBindedDisplayList_BindHook> hook = nil;
  if (isGlobalList) {
    hook = [[ImActorModelModulesDisplayLists_$2 alloc] initWithImActorModelModulesDisplayLists:self withAMPeer:peer];
  }
  
#line 132
  AMBindedDisplayList *chatList = [[AMBindedDisplayList alloc] initWithDKListEngineDisplayExt:(id<DKListEngineDisplayExt>) check_protocol_cast(messagesEngine, @protocol(DKListEngineDisplayExt)) withBoolean:
#line 133
  isGlobalList withInt:ImActorModelModulesDisplayLists_LOAD_PAGE withInt:ImActorModelModulesDisplayLists_LOAD_GAP withAMBindedDisplayList_BindHook:hook];
  [chatList initTopWithBoolean:NO];
  return chatList;
}


#line 138
- (AMBindedDisplayList *)buildMediaListWithAMPeer:(AMPeer *)peer {
  
#line 139
  AMMVVMEngine_checkMainThread();
  
#line 141
  id<DKListEngine> mediaEngine = [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getMessagesModule])) getMediaEngineWithAMPeer:peer];
  if (!([DKListEngineDisplayExt_class_() isInstance:mediaEngine])) {
    @throw [[JavaLangRuntimeException alloc] initWithNSString:@"Media ListEngine must implement ListEngineDisplayExt for using global list"];
  }
  
#line 146
  AMBindedDisplayList *mediaList = [[AMBindedDisplayList alloc] initWithDKListEngineDisplayExt:(id<DKListEngineDisplayExt>) check_protocol_cast(mediaEngine, @protocol(DKListEngineDisplayExt)) withBoolean:
#line 147
  NO withInt:ImActorModelModulesDisplayLists_LOAD_PAGE withInt:ImActorModelModulesDisplayLists_LOAD_GAP withAMBindedDisplayList_BindHook:nil];
  [mediaList initTopWithBoolean:NO];
  return mediaList;
}


#line 152
- (AMBindedDisplayList *)buildNewSearchListWithBoolean:(jboolean)isGlobalList {
  
#line 153
  AMMVVMEngine_checkMainThread();
  
#line 155
  id<DKListEngine> contactsEngine = [((ImActorModelModulesSearchModule *) nil_chk([((ImActorModelModulesModules *) nil_chk([self modules])) getSearch])) getSearchList];
  if (!([DKListEngineDisplayExt_class_() isInstance:contactsEngine])) {
    @throw [[JavaLangRuntimeException alloc] initWithNSString:@"Search ListEngine must implement ListEngineDisplayExt for using global list"];
  }
  
#line 160
  AMBindedDisplayList *contactList = [[AMBindedDisplayList alloc] initWithDKListEngineDisplayExt:(id<DKListEngineDisplayExt>) check_protocol_cast(contactsEngine, @protocol(DKListEngineDisplayExt)) withBoolean:
#line 161
  isGlobalList withInt:ImActorModelModulesDisplayLists_LOAD_PAGE withInt:ImActorModelModulesDisplayLists_LOAD_GAP withAMBindedDisplayList_BindHook:nil];
  [contactList initEmpty];
  return contactList;
}

- (void)copyAllFieldsTo:(ImActorModelModulesDisplayLists *)other {
  [super copyAllFieldsTo:other];
  other->dialogGlobalList_ = dialogGlobalList_;
  other->contactsGlobalList_ = contactsGlobalList_;
  other->chatsGlobalLists_ = chatsGlobalLists_;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesDisplayLists)

@implementation ImActorModelModulesDisplayLists_$1


#line 75
- (void)onScrolledToEnd {
  
#line 76
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([this$0_ modules])) getMessagesModule])) loadMoreDialogs];
}


#line 80
- (void)onItemTouchedWithId:(AMDialog *)item {
}

- (instancetype)initWithImActorModelModulesDisplayLists:(ImActorModelModulesDisplayLists *)outer$ {
  this$0_ = outer$;
  return [super init];
}

- (void)copyAllFieldsTo:(ImActorModelModulesDisplayLists_$1 *)other {
  [super copyAllFieldsTo:other];
  other->this$0_ = this$0_;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesDisplayLists_$1)

@implementation ImActorModelModulesDisplayLists_$2


#line 118
- (void)onScrolledToEnd {
  
#line 119
  [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([this$0_ modules])) getMessagesModule])) loadMoreHistoryWithAMPeer:val$peer_];
}


#line 123
- (void)onItemTouchedWithId:(AMMessage *)item {
  if ([((AMMessage *) nil_chk(item)) getSenderId] != [this$0_ myUid]) {
    
#line 126
    [((ImActorModelModulesMessages *) nil_chk([((ImActorModelModulesModules *) nil_chk([this$0_ modules])) getMessagesModule])) onInMessageShownWithAMPeer:val$peer_ withLong:[item getRid] withLong:[item getSortDate] withBoolean:NO];
  }
}

- (instancetype)initWithImActorModelModulesDisplayLists:(ImActorModelModulesDisplayLists *)outer$
                                             withAMPeer:(AMPeer *)capture$0 {
  this$0_ = outer$;
  val$peer_ = capture$0;
  return [super init];
}

- (void)copyAllFieldsTo:(ImActorModelModulesDisplayLists_$2 *)other {
  [super copyAllFieldsTo:other];
  other->this$0_ = this$0_;
  other->val$peer_ = val$peer_;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelModulesDisplayLists_$2)
