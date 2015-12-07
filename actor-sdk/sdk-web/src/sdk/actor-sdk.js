/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import 'babel-polyfill';
import RouterContainer from '../utils/RouterContainer';
import DelegateContainer from '../utils/DelegateContainer';
import SDKDelegate from './actor-sdk-delegate';
import { endpoints, bugsnagApiKey, mixpanelAPIKey } from '../constants/ActorAppConstants'
import Pace from 'pace';

import React, { Component, PropTypes } from 'react';
import Router from 'react-router';
import ReactMixin from 'react-mixin';
import Actor from 'actor-js';

import { IntlMixin } from 'react-intl';
import crosstab from 'crosstab';

import LoginActionCreators from '../actions/LoginActionCreators';

import LoginStore from '../stores/LoginStore';

import Deactivated from '../components/Deactivated.react.js';
import Login from '../components/Login.react.js';
import Main from '../components/Main.react.js';
import JoinGroup from '../components/JoinGroup.react.js';
import Install from '../components/Install.react.js';

import { initBugsnag } from '../utils/Bugsnag';
import { initMixpanel } from '../utils/Mixpanel';
import { extendL18n, getIntlData } from '../l18n';

const { DefaultRoute, Route, RouteHandler } = Router;

Pace.start({
  ajax: false,
  restartOnRequestAfter: false,
  restartOnPushState: false
});

window.isJsAppLoaded = false;
window.jsAppLoaded = () => {
  window.isJsAppLoaded = true;
};

class App extends Component {
  static childContextTypes =  {
    delegate: PropTypes.object
  };

  static propTypes =  {
    delegate: PropTypes.object
  };

  getChildContext() {
    return {
      delegate: this.props.delegate
    };
  }

  constructor(props) {
    super(props);
  }

  render() {
    return <RouteHandler/>;
  }
}

ReactMixin.onClass(App, IntlMixin);

class ActorSDK {
  constructor(options) {
    options = options || {};

    this.endpoints = (options.endpoints && options.endpoints.length > 0) ? options.endpoints : endpoints;
    this.bugsnagApiKey = options.bugsnagApiKey ? options.bugsnagApiKey : bugsnagApiKey;
    this.mixpanelAPIKey = options.mixpanelAPIKey ? options.mixpanelAPIKey : mixpanelAPIKey;

    this.delegate = options.delegate ? options.delegate : new SDKDelegate();
    DelegateContainer.set(this.delegate);

    initBugsnag(this.bugsnagApiKey);
    initMixpanel(this.mixpanelAPIKey);

    if (this.delegate.l18n) {
      extendL18n();
    }
  }

  _starter() {
    const ActorInitEvent = 'concurrentActorInit';

    if (crosstab.supported) {
      crosstab.on(ActorInitEvent, (msg) => {
        if (msg.origin !== crosstab.id && window.location.hash !== '#/deactivated') {
          window.location.assign('#/deactivated');
          window.location.reload();
        }
      });
    }

    const appRootElemet = document.getElementById('actor-web-app');

    if (window.location.hash !== '#/deactivated') {
      if (crosstab.supported)
        crosstab.broadcast(ActorInitEvent, {});

      window.messenger = Actor.create(this.endpoints);
    }

    const loginComponent = this.delegate.components.login || Login;
    const intlData = getIntlData();

    const routes = (
      <Route handler={App} name="app" path="/">
        <Route handler={loginComponent} name="login" path="/auth"/>

        <Route handler={Main} name="main" path="/im/:id"/>
        <Route handler={JoinGroup} name="join" path="/join/:token"/>
        <Route handler={Deactivated} name="deactivated" path="/deactivated"/>
        <Route handler={Install} name="install" path="/install"/>

        <DefaultRoute handler={Main}/>
      </Route>
    );

    const router = Router.create(routes, Router.HashLocation);

    RouterContainer.set(router);

    router.run((Root) => React.render(<Root {...intlData} delegate={this.delegate}/>, appRootElemet));

    if (window.location.hash !== '#/deactivated') {
      if (LoginStore.isLoggedIn()) {
        LoginActionCreators.setLoggedIn({redirect: false});
      }
    }
  };

  startApp() {
    if (window.isJsAppLoaded) {
      this._starter();
    } else {
      window.jsAppLoaded = this._starter.bind(this);
    }
  }
}

export default ActorSDK;
