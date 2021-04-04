import { React, Component } from 'react';
import GithubButton from 'react-github-btn';
import { AiOutlineStar } from 'react-icons/ai';

import CommitStats from '../components/repositoryRoute/CommitStats';

import RepositoryId from '../model/RepositoryId';

import githubService from '../services/GithubService';

import '../styles/components/repositoryRoute/RepositoryRoute.css';

export default class RepositoryRoute extends Component {

    constructor(props) {
        super(props);

        this.state = {
            repositoryDetail: null,
        }
    }

    componentDidMount() {
        this._loadGithubDetails();
    }

    render() {
        const repositoryDetail = this.state.repositoryDetail;

        return (
            <>
                <div className='repository'>
                    {repositoryDetail &&
                        <>
                            <p className='repository-name'>{repositoryDetail.owner.login} / {repositoryDetail.name} </p>
                            <GithubButton href={repositoryDetail.html_url}>View on Github</GithubButton>

                            {repositoryDetail.description &&
                                <p className='description'>{repositoryDetail.description}</p>
                            }

                            <div className='d-flex align-items-center'>
                                {repositoryDetail.language && 
                                    <>
                                        <div className={`language-circle ${repositoryDetail.language}`}></div>
                                        <span className='ml-1 language-name'>{repositoryDetail.language}</span>
                                    </>
                                }

                                {repositoryDetail.updated_at &&
                                    <span className='updated-at'>Updated {new Intl.DateTimeFormat("en-GB", {
                                        year: "numeric",
                                        month: "long",
                                        day: "2-digit"
                                    }).format(new Date(repositoryDetail.updated_at))}
                                    </span>
                                }

                                <div className='stargazers align-items-center'>
                                    <AiOutlineStar />
                                    <span className='stargazers-count'>{repositoryDetail.stargazers_count}</span>
                                </div>
                            </div>

                            <hr style={{width: '600px', display: 'inline-block'}} />

                            <CommitStats repositoryId={new RepositoryId(repositoryDetail.owner.login, repositoryDetail.name)} />
                        </>
                    }
                </div>
            </>
        );
    }

    _loadGithubDetails() {
        githubService.getRepositoryDetails(this.props.match.params.owner, this.props.match.params.name).then(details => {
            this.setState({
                repositoryDetail: details
            });
        });
    }

}
