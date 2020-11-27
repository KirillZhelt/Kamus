package com.kamus.dataloader.util;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloQueryCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.request.RequestHeaders;
import com.apollographql.apollo.rx3.Rx3Apollo;
import io.reactivex.rxjava3.core.Observable;

import java.util.List;
import java.util.Objects;

public class GraphQLObservableTemplate {

    private final ApolloClient apolloClient;
    private final RequestHeaders requestHeaders;

    public GraphQLObservableTemplate(ApolloClient apolloClient, String bearerToken) {
        Objects.requireNonNull(apolloClient);
        Objects.requireNonNull(bearerToken);

        this.apolloClient = apolloClient;
        this.requestHeaders = RequestHeaders.builder()
                                      .addHeader("Authorization", "Bearer " + bearerToken)
                                      .build();
    }

    public <D extends Operation.Data, T, V extends Operation.Variables> Observable<T> queryCall(Query<D, T, V> query) {
        ApolloQueryCall<T> call = apolloClient.query(query)
                                          .toBuilder()
                                          .requestHeaders(requestHeaders)
                                          .build();
        return wrapIntoObservable(call);
    }

    private static <T> Observable<T> wrapIntoObservable(ApolloQueryCall<T> call) {
        return Rx3Apollo.from(call).map(GraphQLObservableTemplate::tryGetValue);
    }

    private static <T> T tryGetValue(Response<T> response) {
        if (response.hasErrors()) {
            throw new GraphQLTemplateException("Cannot load data through GraphQL.", response.getErrors());
        } else {
            return response.getData();
        }
    }

    public static class GraphQLTemplateException extends RuntimeException {
        private final List<Error> errors;

        public GraphQLTemplateException(String message, List<Error> errors) {
            super(message);

            Objects.requireNonNull(errors);
            this.errors = errors;
        }

        public List<Error> getErrors() {
            return errors;
        }
    }

}
