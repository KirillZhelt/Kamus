package com.kamus.loaderconfig.service;

import com.kamus.common.grpcjava.Repository;
import com.kamus.core.db.RepositoryId;
import com.kamus.loaderconfig.config.KafkaProducerConfig;
import com.kamus.loaderconfig.db.repository.RepositoriesRepository;
import com.kamus.loaderconfig.db.model.TrackedRepository;
import com.kamus.core.model.BucketId;
import com.kamus.loaderconfig.grpcjava.AssignedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackedRepositoriesService {

    private static final Logger logger = LoggerFactory.getLogger(TrackedRepositoriesService.class);

    private final RepositoriesRepository repositoriesRepository;
    private final BucketSortingHatService sortingHatService;
    private final KafkaTemplate<String, AssignedRepository> assignedRepositoryKafka;

    public TrackedRepositoriesService(RepositoriesRepository repositoriesRepository,
                                      BucketSortingHatService sortingHatService,
                                      KafkaTemplate<String, AssignedRepository> assignedRepositoryKafka) {
        this.repositoriesRepository = repositoriesRepository;
        this.sortingHatService = sortingHatService;
        this.assignedRepositoryKafka = assignedRepositoryKafka;
    }

    public void trackRepositories(List<Repository> repositories) {
        List<TrackedRepository> trackedRepositories = assignAndTrackRepositories(repositories);

        trackedRepositories.forEach(r -> {
            AssignedRepository assignedRepository = AssignedRepository.newBuilder()
                                                            .setBucketId(r.getBucketId())
                                                            .setRepository(toProtoRepository(r.getId()))
                                                            .build();

            ListenableFuture<SendResult<String, AssignedRepository>> sendFuture =
                    assignedRepositoryKafka.send(KafkaProducerConfig.ASSIGNED_REPOS_TOPIC_NAME, assignedRepository);

            sendFuture.completable().whenComplete((sendResult, t) -> {
               if (Objects.nonNull(t)) {
                   logger.error("Exception thrown while sending AssignedRepository to Kafka: " + t);
               }
            });

        });
    }

    @Transactional
    List<TrackedRepository> assignAndTrackRepositories(List<Repository> repositories) {
        // remove already tracked repos from the list
        // assign new repos to least loaded bucket
        // save to db
        List<RepositoryId> newReposIds = repositories
                                                 .stream()
                                                 .map(r -> new RepositoryId(r.getOwner(), r.getName()))
                                                 .collect(Collectors.toList());

        Set<RepositoryId> alreadyTrackedReposIds =
                repositoriesRepository.findByIdIn(newReposIds)
                        .stream()
                        .map(TrackedRepository.RepositoryIdView::getId)
                        .collect(Collectors.toSet());

        List<Repository> repositoriesToTrack = new ArrayList<>(repositories);
        repositoriesToTrack.removeIf(r -> alreadyTrackedReposIds.contains(new RepositoryId(r.getOwner(), r.getName())));

        BucketId assignedBucket = sortingHatService.assign(repositories);

        List<TrackedRepository> trackedRepositories = repositoriesToTrack
                                                              .stream()
                                                              .map(r -> new TrackedRepository(
                                                                      new RepositoryId(r.getOwner(), r.getName()),
                                                                      assignedBucket.getBucketId(),
                                                                      1,
                                                                      true))
                                                              .collect(Collectors.toList());
        repositoriesRepository.saveAll(trackedRepositories);

        return trackedRepositories;
    }

    public void trackRepository(Repository repository, int bucketId) {
        RepositoryId id = toRepositoryId(repository);

        Optional<TrackedRepository> trackedRepositoryOptional = repositoriesRepository.findById(id);
        if (trackedRepositoryOptional.isPresent()) {
            TrackedRepository trackedRepository = trackedRepositoryOptional.get();
            trackedRepository.setTracked(true);
            repositoriesRepository.save(trackedRepository);
        } else {
            repositoriesRepository.save(new TrackedRepository(id, 1, bucketId, true));
        }
    }

    public void untrackRepository(Repository repository) {
        RepositoryId id = toRepositoryId(repository);

        Optional<TrackedRepository> trackedRepositoryOptional = repositoriesRepository.findById(id);
        if (trackedRepositoryOptional.isPresent()) {
            TrackedRepository trackedRepository = trackedRepositoryOptional.get();
            trackedRepository.setTracked(false);
            repositoriesRepository.save(trackedRepository);
        }
    }

    public List<Repository> getTrackedRepositories() {
        return repositoriesRepository.findAllByTrackedTrue()
                       .stream()
                       .map(trackedRepository -> toProtoRepository(trackedRepository.getId()))
                       .collect(Collectors.toList());
    }

    public List<Repository> getTrackedRepositoriesForBucket(int bucketId) {
        return repositoriesRepository.findAllByBucketId(bucketId)
                       .stream()
                       .map(trackedRepository -> toProtoRepository(trackedRepository.getId()))
                       .collect(Collectors.toList());
    }

    private static RepositoryId toRepositoryId(Repository repository) {
        return new RepositoryId(repository.getOwner(), repository.getName());
    }

    private static Repository toProtoRepository(RepositoryId repositoryId) {
        return Repository.newBuilder()
                .setName(repositoryId.getName())
                .setOwner(repositoryId.getOwner())
                .build();
    }

}
