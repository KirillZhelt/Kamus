
class WatchdogApiService {

    HOST = 'http://localhost:8080/api';
    TOKEN = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYXBpIl0sImV4cCI6MTYxNzMwNTI5NCwidXNlcl9uYW1lIjoidGVzdDEiLCJqdGkiOiJjYzlhMWFmZi1iMjRmLTQxY2QtYjFlMC1mM2Y1MTY2MDAzMjgiLCJjbGllbnRfaWQiOiJ3YXRjaGRvZyIsInNjb3BlIjpbInJlYWQiLCJ3cml0ZSJdfQ.kmY5vZ1HJIgen0hDHx4Yi5quNxuwtfWda3MhxMhjG1w';

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

}

const watchdogApiService = new WatchdogApiService();
export default watchdogApiService;
