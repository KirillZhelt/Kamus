package com.kamus.dataloader.config;

import com.apollographql.apollo.ApolloClient;
import com.kamus.dataloader.util.GraphQLObservableTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GithubClientConfig {

    @Bean
    public ApolloClient githubApolloClient(@Value("${github.api.graphql.endpoint}") String githubGraphQlHost) {
        return ApolloClient.builder()
                .serverUrl(githubGraphQlHost)
                .build();
    }

    @Bean
    public GraphQLObservableTemplate githubTemplate(ApolloClient apolloClient, @Value("${github.api.bearer}") String bearerToken) {
        return new GraphQLObservableTemplate(apolloClient, bearerToken);
    }

}
