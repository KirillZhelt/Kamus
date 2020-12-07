package com.kamus.dataloader.runner;

import com.kamus.common.grpcjava.Commit;
import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.db.model.CommitInfo;
import com.kamus.dataloader.db.model.LatestCommit;
import com.kamus.dataloader.db.model.LoadedCommit;
import com.kamus.dataloader.db.model.RepositoryId;
import com.kamus.dataloader.db.repostitory.LatestCommitRepository;
import com.kamus.dataloader.service.GithubDataLoaderService;
import com.kamus.dataloader.service.LoaderConfigurationUpdater;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PollLoaderRunner {

    private static final Logger logger = LoggerFactory.getLogger(PollLoaderRunner.class);

    private final LoaderConfigurationUpdater configurationUpdater;
    private final GithubDataLoaderService loaderService;
    private final LatestCommitRepository latestCommitRepository;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PollLoaderRunner(GithubDataLoaderService loaderService, LoaderConfigurationUpdater configurationUpdater,
                            LatestCommitRepository latestCommitRepository) {
        this.loaderService = loaderService;
        this.configurationUpdater = configurationUpdater;
        this.latestCommitRepository = latestCommitRepository;
    }

//    @Scheduled(fixedDelay = 1000)
//    public void scheduledPoll() {
//        // TODO: load new repos info according to the config and put it in kafka
//        // 1. load config or update if needed
//        // 2. load data according to config
//        // 3. put new messages in kafka
////        LoaderConfiguration configuration = configurationUpdater.getCurrentConfiguration();
////        configuration.getRepositoryList().stream().map(repo -> {
////            loaderService.
////        });
//
//        logger.info("scheduledPoll()");
//    }

    public void poll() {
        compositeDisposable.add(
                loaderService.getNewCommits(Repository.newBuilder().setOwner("KirillZhelt").setName("MayMayMay").build())
                        .subscribe(this::writeCommitsToDb,
                                e -> logger.error("Error while getting commits for the repository: " + e.toString()))
        );
    }

    private void writeCommitsToDb(List<Commit> commits) {
        if (commits.isEmpty()) {
            logger.info("No new commits!");
        } else {
            Commit latestCommit = commits.get(0);
            LatestCommit commit = new LatestCommit(
                    new RepositoryId(latestCommit.getRepository().getOwner(), latestCommit.getRepository().getName()),
                    latestCommit.getSha(),
                    toLocalDateTime(latestCommit.getCommitDate())
            );

            Set<LoadedCommit> loadedCommits = commits.stream().map(c -> new LoadedCommit(
                    commit,
                    c.getSha(),
                    new CommitInfo(
                            c.getRepository().getName(),
                            c.getRepository().getOwner(),
                            c.getStats().getAdditions(),
                            c.getStats().getDeletions(),
                            c.getStats().getChangedFiles(),
                            c.getAuthorName(),
                            c.getAuthorEmail(),
                            toLocalDateTime(c.getCommitDate())
                    )
            )).collect(Collectors.toSet());

            commit.setLoadedCommits(loadedCommits);
            latestCommitRepository.save(commit);
        }
    }

    private static LocalDateTime toLocalDateTime(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
    }

//    private void sendCommitsToKafka(List<Commit> commits) {
//        commits.forEach(commit -> compositeDisposable.add(
//                Observable.fromFuture(kafkaTemplate.send(KafkaConfig.COMMITS_TOPIC_NAME, RepositoryCommitMessage.newBuilder().setCommit(commit).build()))
//                        .subscribe(r -> {}, e -> logger.error("Wasn't able to send a commit: " + e.toString()))
//        ));
//    }

    @PreDestroy
    public void dispose() {
        compositeDisposable.dispose();
    }

}
