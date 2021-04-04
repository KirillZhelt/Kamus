package com.kamus.commitsanalyzer.grpc;

import com.google.protobuf.Any;
import com.kamus.commitsanalyzer.topology.stream.CommitsCounterStreams;
import com.kamus.common.grpcjava.Repository;
import com.kamus.loaderconfig.grpcjava.CommitsAnalyzerServiceGrpc;
import com.kamus.loaderconfig.grpcjava.CommitsCountFor31DaysResponse;
import com.kamus.loaderconfig.grpcjava.ShardingKey;
import com.kamus.loaderconfig.grpcjava.TotalCommitsForResponse;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
public class CommitsAnalyzerService
        extends CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceImplBase
        implements StreamsBuilderFactoryBean.Listener, KafkaStreams.StateListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommitsAnalyzerService.class);

    private static final Metadata.Key<ShardingKey> SHARDING_KEY =
            Metadata.Key.of(
                    "SHARDING_KEY-bin",
                    ProtoUtils.metadataMarshaller(ShardingKey.getDefaultInstance()));

    private final HostInfo host;
    private final CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceStub commitsAnalyzerStub;

    private final KafkaProtobufSerde<Repository> repositorySerde;

    private KafkaStreams streams;

    private ReadOnlyKeyValueStore<Repository, Long> commitsCountPerRepositoryStore;
    private ReadOnlyWindowStore<Repository, Long> commitsCountPerRepositoryFor31DaysStore;

    public CommitsAnalyzerService(StreamsBuilderFactoryBean streamsBuilderFactoryBean,
                                  HostInfo hostInfo,
                                  CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceStub commitsAnalyzerStub,
                                  KafkaProtobufSerde<Repository> repositorySerde) {
        streamsBuilderFactoryBean.addListener(this);
        streamsBuilderFactoryBean.setStateListener(this);
        this.streams = streamsBuilderFactoryBean.getKafkaStreams();

        this.host = hostInfo;
        this.commitsAnalyzerStub = commitsAnalyzerStub;

        this.repositorySerde = repositorySerde;
    }

    @Override
    public void commitsCountPerRepositoryStore(Repository repository, StreamObserver<TotalCommitsForResponse> responseObserver) {
        if (!checkHost(repository, repositorySerde.serializer(), CommitsCounterStreams.COMMITS_COUNT_PER_REPOSITORY_STORE)) {
            Metadata header = new Metadata();

            ShardingKey key = ShardingKey.newBuilder().setKey(Any.pack(repository, "")).build();
            header.put(SHARDING_KEY, key);

            CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceStub stub = MetadataUtils.attachHeaders(commitsAnalyzerStub, header);

            stub.commitsCountPerRepositoryStore(repository, responseObserver);
            return;
        }

        Long commitsCount = this.commitsCountPerRepositoryStore.get(repository);
        if (Objects.isNull(commitsCount)) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
            return;
        }

        responseObserver.onNext(TotalCommitsForResponse.newBuilder().setCommitsCount(commitsCount).setInstance(host.toString()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void commitsCountPerRepositoryFor31DaysAggregatedByDay(Repository repository, StreamObserver<CommitsCountFor31DaysResponse> responseObserver) {
        if (!checkHost(repository, repositorySerde.serializer(), CommitsCounterStreams.COMMITS_COUNT_PER_REPOSITORY_STORE_31_DAYS)) {
            Metadata header = new Metadata();

            ShardingKey key = ShardingKey.newBuilder().setKey(Any.pack(repository, "")).build();
            header.put(SHARDING_KEY, key);

            CommitsAnalyzerServiceGrpc.CommitsAnalyzerServiceStub stub = MetadataUtils.attachHeaders(commitsAnalyzerStub, header);

            stub.commitsCountPerRepositoryFor31DaysAggregatedByDay(repository, responseObserver);
            return;
        }

        CommitsCountFor31DaysResponse.Builder responseBuilder = CommitsCountFor31DaysResponse.newBuilder()
                                                                        .setInstance(host.toString());
        try (WindowStoreIterator<Long> commitCounts = commitsCountPerRepositoryFor31DaysStore.fetch(repository,
                Instant.now().minus(31, ChronoUnit.DAYS),
                Instant.now())) {
            while (commitCounts.hasNext()) {
                KeyValue<Long, Long> next = commitCounts.next();
                responseBuilder.putCommitsCountForDay(Instant.ofEpochMilli(next.key).toString(), next.value);
            }
        }

        if (responseBuilder.getCommitsCountForDayCount() == 0) {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
            return;
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void streamsAdded(String id, KafkaStreams streams) {
        if (Objects.nonNull(this.streams)) {
            throw new IllegalStateException("KafkaStreams has already been initialized");
        }

        this.streams = streams;
    }

    @Override
    public void onChange(KafkaStreams.State newState, KafkaStreams.State oldState) {
        if (Objects.isNull(streams)) {
            return;
        }

        if (newState.isRunningOrRebalancing()) {
            StoreQueryParameters<ReadOnlyKeyValueStore<Repository, Long>> commitsCountPerRepositoryStoreParameters =
                    StoreQueryParameters.fromNameAndType(
                            CommitsCounterStreams.COMMITS_COUNT_PER_REPOSITORY_STORE,
                            QueryableStoreTypes.<Repository, Long>keyValueStore()).enableStaleStores();

            StoreQueryParameters<ReadOnlyWindowStore<Repository, Long>> commitsCountPerRepositoryFor31DaysStoreParameters =
                    StoreQueryParameters.fromNameAndType(
                            CommitsCounterStreams.COMMITS_COUNT_PER_REPOSITORY_STORE_31_DAYS,
                            QueryableStoreTypes.<Repository, Long>windowStore()).enableStaleStores();

            try {
                this.commitsCountPerRepositoryStore = streams.store(commitsCountPerRepositoryStoreParameters);
                this.commitsCountPerRepositoryFor31DaysStore = streams.store(commitsCountPerRepositoryFor31DaysStoreParameters);
            } catch (InvalidStateStoreException e) {
                LOGGER.warn("An exception thrown while trying to obtain a store with parameters: {}. Probably store doesn't exists now.", e.toString());
            }
        }
    }

    private <K> boolean checkHost(K key, Serializer<K> keySerializer, String storeName) {
        KeyQueryMetadata metadata = streams.queryMetadataForKey(storeName, key, keySerializer);
        return thisHost(metadata.getActiveHost());
    }

    private boolean thisHost(HostInfo activeHost) {
        return host.equals(activeHost);
    }

}
