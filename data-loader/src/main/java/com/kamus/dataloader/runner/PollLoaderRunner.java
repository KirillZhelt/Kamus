package com.kamus.dataloader.runner;

import com.kamus.common.grpcjava.Repository;
import com.kamus.dataloader.grpcjava.LoaderConfiguration;
import com.kamus.dataloader.service.CommitsPusherService;
import com.kamus.dataloader.service.GithubDataLoaderService;
import com.kamus.dataloader.service.LoaderConfigurationService;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class PollLoaderRunner {

    private static final Logger logger = LoggerFactory.getLogger(PollLoaderRunner.class);

    private final LoaderConfigurationService configurationUpdater;
    private final GithubDataLoaderService loaderService;
    private final CommitsPusherService commitsPusherService;

    private final AtomicBoolean pollInProgress = new AtomicBoolean(false);

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PollLoaderRunner(GithubDataLoaderService loaderService, LoaderConfigurationService configurationUpdater,
                            CommitsPusherService commitsPusherService) {
        this.loaderService = loaderService;
        this.configurationUpdater = configurationUpdater;
        this.commitsPusherService = commitsPusherService;
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduledPoll() {
        logger.info("scheduledPoll()");

        Set<LoaderConfiguration> configuration = configurationUpdater.getCurrentConfiguration();
        if (configuration.isEmpty()) {
            logger.info("Loader configuration is empty. Skipping the polls.");
        } else {
            pollBuckets(configuration);
        }
    }

    private void pollBuckets(Set<LoaderConfiguration> bucketsConfiguration) {
        if (pollInProgress.compareAndSet(false, true)) {
            List<Single<Object>> pollSingles = bucketsConfiguration.stream().map(this::poll).collect(Collectors.toList());

            compositeDisposable.add(
                    Single.merge(pollSingles).toList(bucketsConfiguration.size()).subscribe(
                            r -> pollInProgress.set(false),
                            e -> logger.error("Exception occured while polling the buckets: {}", e.toString())
                    )
            );
        } else {
            logger.warn("Poll is still in progress. Skipping the poll on the current scheduled run.");
        }
    }

    private Single<Object> poll(LoaderConfiguration configuration) {
        Set<Repository> repositories = new HashSet<>(configuration.getRepositoryList());

        logger.info("Polling repositories: {}, for bucket: {}", repositories, configuration.getBucket());

        return loaderService.getNewCommits(repositories)
                                                .flatMap(commits -> {
                                                    if (commits.isEmpty()) {
                                                        logger.info("No new commits to push for repos: {}", repositories);
                                                        return Single.just(CommitsPusherService.SUCCESS);
                                                    } else {
                                                        return commitsPusherService.pushCommits(commits);
                                                    }
                                                })
                       .doOnSuccess(c -> logger.info("Poll for bucket {} completed successfully", configuration.getBucket()))
                       .doOnError(e -> logger.error("Error while getting commits for the bucket: {}. Exception occured: {}",
                               configuration.getBucket(), e.toString()));
    }

    @PreDestroy
    public void dispose() {
        compositeDisposable.dispose();
    }

}
