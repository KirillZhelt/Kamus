package com.kamus.dataloader.runner;

import com.kamus.dataloader.service.GithubDataLoaderService;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class PollLoaderRunner {

    private static final Logger logger = LoggerFactory.getLogger(PollLoaderRunner.class);

    private final GithubDataLoaderService loaderService;
    private final CompositeDisposable compositeDisposable;

    public PollLoaderRunner(GithubDataLoaderService loaderService) {
        this.loaderService = loaderService;
        this.compositeDisposable = new CompositeDisposable();
    }

    public void poll() {
        compositeDisposable.add(
                loaderService.findIssueId()
                        .subscribe((d) -> logger.info(d.toString()), (t) -> logger.error(t.toString()), () -> logger.info("complete"))
        );
    }

    @PreDestroy
    public void dispose() {
        compositeDisposable.dispose();
    }


}
