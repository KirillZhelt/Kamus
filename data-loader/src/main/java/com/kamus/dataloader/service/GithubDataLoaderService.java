package com.kamus.dataloader.service;

import com.kamus.dataloader.queries.FindIssueIDQuery;
import com.kamus.dataloader.util.GraphQLObservableTemplate;
import io.reactivex.rxjava3.core.Observable;
import org.springframework.stereotype.Service;

@Service
public class GithubDataLoaderService {

    private final GraphQLObservableTemplate githubTemplate;

    public GithubDataLoaderService(GraphQLObservableTemplate githubTemplate) {
        this.githubTemplate = githubTemplate;
    }

    public Observable<FindIssueIDQuery.Data> findIssueId() {
        return githubTemplate.queryCall(new FindIssueIDQuery());
    }

}
