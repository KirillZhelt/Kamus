import { Link } from 'react-router-dom';

import '../../styles/components/homeRoute/RepositoryDetail.css';
import '../../styles/GithubLanguageColors.css';

import { AiOutlineStar } from 'react-icons/ai';

export default function RepositoryDetail(props) {
    const repo = props.repository;

    return (
        <div className='repository-detail'>
            <Link to={`/repository/${repo.owner.login}/${repo.name}`}>
                <p className='repository-detail-name'>{repo.owner.login} / {repo.name} </p>
            </Link>

            {repo.description &&
                <p className='description'>{repo.description}</p>
            }

            <div className='d-flex align-items-center'>
                {repo.language && 
                    <>
                        <div className={`language-circle ${repo.language}`}></div>
                        <span className='ml-1 language-name'>{repo.language}</span>
                    </>
                }

                {repo.updated_at &&
                    <span className='updated-at'>Updated {new Intl.DateTimeFormat("en-GB", {
                        year: "numeric",
                        month: "long",
                        day: "2-digit"
                    }).format(new Date(repo.updated_at))}
                    </span>
                }

                <div className='stargazers align-items-center'>
                    <AiOutlineStar />
                    <span className='stargazers-count'>{repo.stargazers_count}</span>
                </div>
            </div>

            <hr style={{width: '600px'}} />
        </div>
    );
}
