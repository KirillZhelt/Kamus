
class WatchdogApiService {

    HOST = 'http://localhost:8080/api';
    TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYXBpIl0sImV4cCI6MTYxNzM5MjY3MiwidXNlcl9uYW1lIjoidGVzdDEiLCJqdGkiOiIwM2FlOWE5ZC0yZDYxLTQyNTQtOGNlZi1jMjZkZWU0MGFjNGYiLCJjbGllbnRfaWQiOiJ3YXRjaGRvZyIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdfQ.iogYA2E5ktA5NL34TMnURV5hno9TQxHCv-W_FyXZ1js';

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
