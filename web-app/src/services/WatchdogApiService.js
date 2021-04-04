
class WatchdogApiService {

    HOST = 'http://localhost:8080/api';
    TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYXBpIl0sImV4cCI6MTYxNzQwNDc1MywidXNlcl9uYW1lIjoidGVzdDEiLCJqdGkiOiI1NzAwMDM5Zi1hNzZkLTRmY2EtYmYzMS0yODNmNjBlZWM2MzQiLCJjbGllbnRfaWQiOiJ3YXRjaGRvZyIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdfQ.G-mDeeOG9EH5iuy8XxVd9pDYgK_X78jAZqiLdHZUEoc';

    async _api(endpoint, request) {
        if (request === undefined) {
            request = {};
        }
        if (request.headers === undefined) {
            request.headers = {};
        }

        request.headers['Authorization'] = `Bearer ${this.TOKEN}`;
        return fetch(`${this.HOST}${endpoint}`, request);
    }

    async getAllRepositories() {
        return this._api('/users-repositories/repositories')
                    .then(result => result.json());
    }

    async addRepository(repository) {
        return this._api('/users-repositories/repositories', {
            method: 'POST',
            body: JSON.stringify(repository),
            headers: {  
                "Content-type": "application/json"  
              },
        }).then(result => result.json());
    } 

    async getCommitStats(repositoryId) {
        return this._api(`/repositories/${repositoryId.owner}/${repositoryId.name}/stats`)
                    .then(response => {
                        if (response.status === 204) {
                            return {
                                message: 'No data is available yet. It will appear soon, please wait.',
                            };
                        }

                        return response.json();
                    });
    }

}

const watchdogApiService = new WatchdogApiService();
export default watchdogApiService;
