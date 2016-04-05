/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import ActorClient from '../../utils/ActorClient';
import { escapeWithEmoji } from '../../utils/EmojiUtils'
import confirm from '../../utils/confirm'
import SharedContainer from '../../utils/SharedContainer';
import { FormattedMessage } from 'react-intl';

import MyProfileActions from '../../actions/MyProfileActionCreators';
import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import LoginActionCreators from '../../actions/LoginActionCreators';
import HelpActionCreators from '../../actions/HelpActionCreators';
import AddContactActionCreators from '../../actions/AddContactActionCreators';
import PreferencesActionCreators from '../../actions/PreferencesActionCreators';

import MyProfileStore from '../../stores/MyProfileStore';
import CreateGroupStore from '../../stores/CreateGroupStore';
import AddContactStore from '../../stores/AddContactStore';
import PreferencesStore from '../../stores/PreferencesStore';

import SvgIcon from '../common/SvgIcon.react';
import AvatarItem from '../common/AvatarItem.react';
import CreateGroupModal from '../modals/CreateGroup';
import MyProfileModal from '../modals/MyProfile.react';
import AddContactModal from '../modals/AddContact.react';
import PreferencesModal from '../modals/Preferences.react';

class HeaderSection extends Component {
  constructor(props) {
    super(props);

    this.openHelp = this.openHelp.bind(this);
    this.openTwitter = this.openTwitter.bind(this);
    this.openFacebook = this.openFacebook.bind(this);
    this.openHomePage = this.openHomePage.bind(this);
  }

  static getStores() {
    return [MyProfileStore, CreateGroupStore, AddContactStore, PreferencesStore];
  }

  static calculateState() {
    return {
      profile: MyProfileStore.getProfile(),
      isMyProfileOpen: MyProfileStore.isModalOpen(),
      isAddContactsOpen: AddContactStore.isOpen(),
      isCreateGroupOpen: CreateGroupStore.isModalOpen(),
      isPreferencesOpen: PreferencesStore.isOpen()
    }
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  componentWillMount() {
    this.setState({isOpened: false});
  }

  toggleHeaderMenu = () => {
    const { isOpened } = this.state;

    if (!isOpened) {
      this.setState({isOpened: true});
      document.addEventListener('click', this.closeHeaderMenu, false);
    } else {
      this.closeHeaderMenu();
    }
  };

  closeHeaderMenu = () => {
    this.setState({isOpened: false});
    document.removeEventListener('click', this.closeHeaderMenu, false);
  };

  openMyProfile = () => MyProfileActions.show();
  openCreateGroup = () => CreateGroupActionCreators.open();
  openAddContactModal = () => AddContactActionCreators.open();
  onSettingsOpen = () => PreferencesActionCreators.show();

  openHelp() {
    HelpActionCreators.open()
  }

  openTwitter(event) {
    const { twitter } = SharedContainer.get();

    event.preventDefault();
    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open(`https://twitter.com/${twitter}`, '_blank');
    }
  }

  openFacebook(event) {
    const { facebook } = SharedContainer.get();

    event.preventDefault();
    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open(`https://facebook.com/${facebook}`, '_blank');
    }
  }

  openHomePage(event) {
    const { homePage } = SharedContainer.get();

    event.preventDefault();
    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open(homePage, '_blank');
    }
  }

  setLogout = () => {
    const { intl } = this.context;
    confirm(intl.messages['modal.confirm.logout']).then(
      () => LoginActionCreators.setLoggedOut(),
      () => {}
    );
  };

  renderTwitterLink() {
    const { twitter } = SharedContainer.get();
    if (!twitter) return null;

    return (
      <li className="dropdown__menu__item">
        <a href={`https://twitter.com/${twitter}`} onClick={this.openTwitter}>
          <SvgIcon className="icon icon--dropdown sidebar__header__twitter" glyph="twitter" />
          <FormattedMessage id="menu.twitter"/>
        </a>
      </li>
    );
  }

  renderFacebookLink() {
    const { facebook } = SharedContainer.get();
    if (!facebook) return null;

    return (
      <li className="dropdown__menu__item">
        <a href={`https://facebook.com/${facebook}`} onClick={this.openFacebook}>
          <SvgIcon className="icon icon--dropdown sidebar__header__facebook" glyph="facebook" />
          <FormattedMessage id="menu.facebook"/>
        </a>
      </li>
    );
  }

  renderHomeLink() {
    const { homePage } = SharedContainer.get();
    if (!homePage) return null;

    return (
      <li className="dropdown__menu__item">
        <a href={homePage} onClick={this.openHomePage}>
          <i className="material-icons">public</i>
          <FormattedMessage id="menu.homePage"/>
        </a>
      </li>
    );
  }

  renderHelpLink() {
    const { helpPhone } = SharedContainer.get();
    if (!helpPhone) return null;

    if (/@/.test(helpPhone)) {
      return (
        <li className="dropdown__menu__item">
          <a href={`mailto:${helpPhone}`}>
            <i className="material-icons">help</i>
            <FormattedMessage id="menu.helpAndFeedback"/>
          </a>
        </li>
      );
    } else {
      return (
        <li className="dropdown__menu__item" onClick={this.openHelp}>
          <i className="material-icons">help</i>
          <FormattedMessage id="menu.helpAndFeedback"/>
        </li>
      );
    }
  }

  render() {
    const { profile, isOpened, isMyProfileOpen, isCreateGroupOpen, isAddContactsOpen, isPreferencesOpen } = this.state;

    if (!profile) return null;

    const headerClass = classnames('sidebar__header', 'sidebar__header--clickable', {
      'sidebar__header--opened': isOpened
    });
    const menuClass = classnames('dropdown', {
      'dropdown--opened': isOpened
    });

    return (
      <header className={headerClass}>
        <div className="sidebar__header__user row" onClick={this.toggleHeaderMenu}>
          <AvatarItem image={profile.avatar}
                      placeholder={profile.placeholder}
                      size="tiny"
                      title={profile.name} />
          <span className="sidebar__header__user__name col-xs"
                dangerouslySetInnerHTML={{__html: escapeWithEmoji(profile.name)}}/>
          <div className={menuClass}>
            <span className="dropdown__button">
              <i className="material-icons">arrow_drop_down</i>
            </span>
            <ul className="dropdown__menu dropdown__menu--right">
              <li className="dropdown__menu__item" onClick={this.openMyProfile}>
                <i className="material-icons">edit</i>
                <FormattedMessage id="menu.editProfile"/>
              </li>
              <li className="dropdown__menu__item" onClick={this.openAddContactModal}>
                <i className="material-icons">person_add</i>
                <FormattedMessage id="menu.addToContacts"/>
              </li>
              <li className="dropdown__menu__item" onClick={this.openCreateGroup}>
                <i className="material-icons">group_add</i>
                <FormattedMessage id="menu.createGroup"/>
              </li>
              <li className="dropdown__menu__separator"/>
              <li className="dropdown__menu__item" onClick={this.onSettingsOpen}>
                <i className="material-icons">settings</i>
                <FormattedMessage id="menu.preferences"/>
              </li>

              {this.renderHelpLink()}
              {this.renderTwitterLink()}
              {this.renderFacebookLink()}
              {this.renderHomeLink()}

              <li className="dropdown__menu__separator"/>
              <li className="dropdown__menu__item" onClick={this.setLogout}>
                <FormattedMessage id="menu.signOut"/>
              </li>
            </ul>
          </div>
        </div>


        {/* Modals */}
        {/* TODO: Move all modals to other place */}
        {isMyProfileOpen ? <MyProfileModal/> : null}
        {isCreateGroupOpen ? <CreateGroupModal/> : null}
        {isAddContactsOpen ? <AddContactModal/> : null}
        {isPreferencesOpen ? <PreferencesModal/> : null}

      </header>
    );

  }
}

export default Container.create(HeaderSection);
