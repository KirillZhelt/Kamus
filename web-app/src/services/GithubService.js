
class GithubService {

    HOST = 'https://api.github.com'
    TOKEN = '861717ad52a6226e1294ff8eb4eff1a1c2abc957'

    cache = {}

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

    async getRepositoryDetails(owner, name) {
        return this._api(`/repos/${owner}/${name}`).then(resp => resp.json());
    }

    async searchRepository(searchQuery, maxResultCount) {
        return this._api(`/search/repositories?q=${searchQuery}&per_page=${maxResultCount}`).then(resp => resp.json()).then(result => result.items);
    }

}

const githubService = new GithubService();
export default githubService;
