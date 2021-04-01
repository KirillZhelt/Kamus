import { React, Component } from 'react';
import { Form, Button, Col } from 'react-bootstrap';
import SearchResultItem from '../components/addRepositoryRoute/SearchResultItem';
import AddRepositoryItem from '../components/addRepositoryRoute/AddRepositoryItem';

import githubService from '../services/GithubService';
import watchdogApiService from '../services/WatchdogApiService';

import '../styles/components/addRepositoryRoute/AddRepositoryRoute.css';

export default class AddRepositoryRoute extends Component {

    constructor(props) {
        super(props);

        this.state = {
            searchQuery: '',
            repositoriesToAdd: [],
            searchResults: []
        };
    }

    render() {
        const repositoryToAddItems = this.state.repositoriesToAdd.map(repo => <AddRepositoryItem key={`${repo.owner}/${repo.name}`} repo={repo} />);
        const searchResultItems = this.state.searchResults.map(searchResult => <SearchResultItem key={`${searchResult.owner.login}/${searchResult.name}`} searchResult={searchResult} onClick={(owner, name) => this._addRepository(owner, name)} />);

        return (
            <div style={{display: 'inline-block', width: '800px', marginLeft: '40px', paddingLeft: '7px', marginTop: '20px'}}>
                <Form>
                    <Form.Row className='align-items-center'>
                        <Col xs='auto'>
                            <Form.Control style={{display:'inline', width:'300px'}} type='text' placeholder='Find a repository...' value={this.state.searchQuery} onChange={(e) => this._onSearchQueryChange(e.target.value)} />
                        </Col>
                        <Col xs='auto'>
                            <Button style={{display: 'inline'}} onClick={() => this._searchForRepos()}>Search</Button>
                        </Col>
                    </Form.Row>
                </Form>

                {searchResultItems.length !== 0 &&
                <div className='searchResults'>
                    {searchResultItems}
                </div>
                }


                <hr style={{width: '800px'}} />

                {repositoryToAddItems.length !== 0 &&
                <div>
                    {repositoryToAddItems}
                </div>
                }
                {repositoryToAddItems.length === 0 &&
                    <p>Hint: Search for the repositories and add them to start analyzing</p>
                }

                <Button onClick={() => this._trackRepositories()}>Add repositories</Button>
            </div>
        );
    }

    _onSearchQueryChange(searchQuery) {
        this.setState({
            searchQuery
        });
    }

    _searchForRepos() {
        if (!this.state.searchQuery) {
            return;
        }

        githubService.searchRepository(this.state.searchQuery, 5).then(repos => {
            this.setState({ searchResults: repos });
        });
    }

    _addRepository(owner, name) {
        this._clearSearchQuery();

        this.setState({
            repositoriesToAdd: [...this.state.repositoriesToAdd, { owner, name }],
        });
    }

    _clearSearchQuery() {
        this.setState({
            searchQuery: '',
            searchResults: [],
        });
    }

    async _trackRepositories() {
        const promises = this.state.repositoriesToAdd.map(repo => watchdogApiService.addRepository(repo));
        await Promise.all(promises);

        this.props.history.push(`/`);
    }

}
