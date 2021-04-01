import Header from './Header';

import {
  Route,
  Switch,
  withRouter,
} from 'react-router-dom';

import { Component } from 'react';

import HomeRoute from '../routes/HomeRoute';
import AddRepositoryRoute from '../routes/AddRepositoryRoute';
import RepositoryRoute from '../routes/RepositoryRoute';

class App extends Component {

  render() {
    return (
      <div className='app'>
        <Header />
        <Switch>
          
          <Route exact path='/' component={HomeRoute} />
          <Route exact path='/add-repository' component={AddRepositoryRoute} />
          <Route exact path='/repository/:owner/:name' component={RepositoryRoute} />

        </Switch>
      </div>
    );
  }

}

export default withRouter(App);
