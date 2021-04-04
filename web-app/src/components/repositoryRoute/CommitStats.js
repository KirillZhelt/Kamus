import { React, Component } from 'react';

import watchdogApiService from '../../services/WatchdogApiService';

export default class CommitStats extends Component {

    constructor(props) {
        super(props);

        this.state = {
            commitStats: null,
            message: null,
        };
    }

    componentDidMount() {
        this._loadCommitStats();
    }

    render() {
        const commitStats = this.state.commitStats;

        return (
            <>
                <h3>Commit Stats</h3>
                {commitStats &&
                    <>
                        <p>Total number: {commitStats.commitsCount}</p>
                        <p>Last 30 days: {commitStats.commitsCount30Days}</p>
                        <p>Last week: {commitStats.commitsCountLastWeek}</p>
                        <p>Today: {commitStats.commitsCountToday}</p>
                    </>
                }

                {
                    <p>{this.state.message}</p>
                }
            </>
        );
    }

    _loadCommitStats() {
        watchdogApiService.getCommitStats(this.props.repositoryId).then(stats => {
            if (stats.stats !== undefined) {
                this.setState({
                    commitStats: stats.stats,
                    message: null,
                });
            } else {
                this.setState({
                    commitStats: null,
                    message: stats.message,
                });
            }
        });
    }

}
