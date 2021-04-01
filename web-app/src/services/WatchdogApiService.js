
class WatchdogApiService {

    HOST = 'http://localhost:8080/api';
    TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYXBpIl0sImV4cCI6MTYxNzM0NTIzMCwidXNlcl9uYW1lIjoidGVzdDEiLCJqdGkiOiJjY2M1MTgzZi1jMTc2LTQ0ZTYtODFkYy0xMThmZGRhNmMwOWQiLCJjbGllbnRfaWQiOiJ3YXRjaGRvZyIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdfQ.jZsyBzy_5qVTRKQkiBIs3qMz6o9JbFQXhHkwsx0ydrk';

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

}

const watchdogApiService = new WatchdogApiService();
export default watchdogApiService;
