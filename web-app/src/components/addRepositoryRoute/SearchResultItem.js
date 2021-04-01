import { React, Component } from 'react';

import { IoMdBookmarks } from 'react-icons/io';

import '../../styles/components/addRepositoryRoute/AddRepositoryRoute.css';

export default class SearchResultItem extends Component {

    render() {
        const searchResult = this.props.searchResult;

        return (
            <div onClick={() => this.props.onClick(searchResult.owner.login, searchResult.name)} className='searchResultItem d-flex align-items-center' style={{borderBottom: '1px solid grey', marginBottom: '0', paddingTop: '8px', paddingBottom: '8px', paddingLeft: '10px'}}>
                <IoMdBookmarks />
                <p style={{display: 'inline-block', marginBottom: '0', marginLeft: '8px'}}>
                    {searchResult.owner.login}/{searchResult.name}
                </p>
            </div>
        );
    }

}
