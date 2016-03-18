/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { KeyCodes } from '../constants/ActorAppConstants';

import { preloadEmojiSheet } from '../utils/EmojiUtils'

import VisibilityActionCreators from '../actions/VisibilityActionCreators';
import QuickSearchActionCreators from '../actions/QuickSearchActionCreators';

import DefaultSidebar from './Sidebar.react';
import Favicon from './common/Favicon.react';

import ModalsWrapper from './modals/ModalsWrapper.react';
import MenuOverlay from './common/MenuOverlay.react';
import InviteUser from './modals/InviteUser.react';
import InviteByLink from './modals/invite-user/InviteByLink.react';
import EditGroup from './modals/EditGroup.react';

class Main extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  };

  static propTypes = {
    children: PropTypes.node
  };

  constructor(props, context) {
    super(props, context);

    this.components = {
      Sidebar: context.delegate.components.sidebar.Sidebar
    };

    document.addEventListener('visibilitychange', this.onVisibilityChange);
    document.addEventListener('keydown', this.onKeyDown, false);

    // Preload emoji spritesheet
    preloadEmojiSheet();

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
    }
  }

  onVisibilityChange = () => {
    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
    } else {
      VisibilityActionCreators.createAppHidden();
    }
  };

  onKeyDown = (event) => {
    // TODO: Make this hotkey work on windows
    if (event.keyCode === KeyCodes.K && event.metaKey) {
      event.stopPropagation();
      event.preventDefault();
      QuickSearchActionCreators.show();
    }
  };

  render() {
    const { Sidebar } = this.components;

    return (
      <div className="app">
        <Favicon/>

        {/*<Sidebar/>*/}
        {this.props.children}

        <ModalsWrapper/>
        <MenuOverlay/>
        <InviteUser/>
        <InviteByLink/>
        <EditGroup/>
      </div>
    );
  }
}

export default Main;
