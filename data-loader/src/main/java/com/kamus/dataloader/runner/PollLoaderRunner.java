package com.kamus.dataloader.runner;

import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.service.GithubDataLoaderService;
import com.kamus.dataloader.service.LoaderConfigurationUpdater;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class PollLoaderRunner {

    private static final Logger logger = LoggerFactory.getLogger(PollLoaderRunner.class);

    private final GithubDataLoaderService loaderService;
    private final LoaderConfigurationUpdater configurationUpdater;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PollLoaderRunner(GithubDataLoaderService loaderService, LoaderConfigurationUpdater configurationUpdater) {
        this.loaderService = loaderService;
        this.configurationUpdater = configurationUpdater;
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
                        .subscribe((d) -> logger.info(d.toString()), (t) -> logger.error(t.toString()), () -> logger.info("complete"))
        );
    }

    @PreDestroy
    public void dispose() {
        compositeDisposable.dispose();
    }


}
