package com.kamus.dataloader.runner;

import com.kamus.common.grpcjava.Commit;
import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.config.KafkaConfig;
import com.kamus.dataloader.grpcjava.RepositoryCommitMessage;
import com.kamus.dataloader.service.GithubDataLoaderService;
import com.kamus.dataloader.service.LoaderConfigurationUpdater;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;

@Component
public class PollLoaderRunner {

    private static final Logger logger = LoggerFactory.getLogger(PollLoaderRunner.class);

    private final LoaderConfigurationUpdater configurationUpdater;
    private final GithubDataLoaderService loaderService;
    private final KafkaTemplate<String, RepositoryCommitMessage> kafkaTemplate;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PollLoaderRunner(GithubDataLoaderService loaderService, LoaderConfigurationUpdater configurationUpdater,
                            KafkaTemplate<String, RepositoryCommitMessage> kafkaTemplate) {
        this.loaderService = loaderService;
        this.configurationUpdater = configurationUpdater;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduledPoll() {
        // TODO: load new repos info according to the config and put it in kafka
        // 1. load config or update if needed
        // 2. load data according to config
        // 3. put new messages in kafka
//        LoaderConfiguration configuration = configurationUpdater.getCurrentConfiguration();
//        configuration.getRepositoryList().stream().map(repo -> {
//            loaderService.
//        });

        logger.info("scheduledPoll()");
    }

    public void poll() {
        compositeDisposable.add(
                loaderService.getNewCommits(Repository.newBuilder().setOwner("KirillZhelt").setName("MayMayMay").build())
                        .subscribe(this::sendCommitsToKafka,
                                e -> logger.error("Error while getting commits for the repository: " + e.toString()))
        );
    }

    private void sendCommitsToKafka(List<Commit> commits) {
        commits.forEach(commit -> compositeDisposable.add(
                Observable.fromFuture(kafkaTemplate.send(KafkaConfig.COMMITS_TOPIC_NAME, RepositoryCommitMessage.newBuilder().setCommit(commit).build()))
                        .subscribe(r -> {}, e -> logger.error("Wasn't able to send a commit: " + e.toString()))
        ));
    }

    @PreDestroy
    public void dispose() {
        compositeDisposable.dispose();
    }

}
