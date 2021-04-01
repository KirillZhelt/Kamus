import { React, Component } from 'react';
import { FormControl } from 'react-bootstrap';

import watchdogApiService from '../services/WatchdogApiService';
import githubService from '../services/GithubService';

import RepositoryDetail from '../components/homeRoute/RepositoryDetail';

export default class HomeRoute extends Component {

    constructor(props) {
        super(props);

        this.state = {
            searchQuery: null,
            repositories: []
        };
    }

    componentDidMount() {
        this._loadRepositories();
    }

    render() {
        const repositoryItems = this._filterBySearchQuery(this.state.repositories)
                                            .map(repo => <li key={repo.owner.login + '-' + repo.name}><RepositoryDetail repository={repo} /></li>);

        return (
            <>
                <div style={{display: 'inline-block', width: '400px', marginLeft: '40px', paddingLeft: '7px', marginTop: '20px'}}>
                    <FormControl type='text' placeholder='Find a repository...' onChange={(e) => this._onSearchQueryChange(e.target.value)} />

                    <hr style={{width: '600px'}} />
                </div>

                <ul style={{listStyleType: 'none'}}>
                    {repositoryItems}
                </ul>
            </>
          );
    }

    _onSearchQueryChange(searchQuery) {
        this.setState({
            searchQuery
        });
    }

    _loadRepositories() {
        watchdogApiService.getAllRepositories().then(repositories => {
            const repoDetailsPromises = repositories.map(repo => githubService.getRepositoryDetails(repo.owner, repo.name));

            Promise.all(repoDetailsPromises).then(repoDetails => {
                this.setState({
                    repositories: repoDetails,
                });
            });
        });
    }

    _filterBySearchQuery(repositories) {
        if (!this.state.searchQuery) {
            return repositories;
        }

        return repositories.filter(repo => repo.name.includes(this.state.searchQuery) || repo.owner.login.includes(this.state.searchQuery));
    }
            
}
