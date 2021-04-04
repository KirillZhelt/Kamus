package com.kamus.dataloader.service;

import com.kamus.common.grpcjava.Commit;
import com.kamus.core.db.RepositoryId;
import com.kamus.dataloader.config.KafkaConfig;
import com.kamus.dataloader.db.model.LatestCommit;
import com.kamus.dataloader.db.repostitory.LatestCommitRepository;
import com.kamus.dataloader.grpcjava.RepositoryCommitMessage;
import com.kamus.dataloader.model.FetchResult;
import io.reactivex.rxjava3.core.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommitsPusherService {

    public static final Object SUCCESS = new Object();

    private static final Logger logger = LoggerFactory.getLogger(CommitsPusherService.class);

    private final LatestCommitRepository latestCommitRepository;
    private final KafkaTemplate<String, RepositoryCommitMessage> kafkaTemplate;

    public CommitsPusherService(LatestCommitRepository latestCommitRepository, KafkaTemplate<String, RepositoryCommitMessage> kafkaTemplate) {
        this.latestCommitRepository = latestCommitRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Single<Object> pushCommits(List<FetchResult> fetchResults) {
        return Single.merge(
                fetchResults.stream()
                        .filter(fr -> !fr.getCommits().isEmpty())
                        .map(this::pushCommits)
                        .collect(Collectors.toList()))
                       .toList()
                       .map(it -> SUCCESS);
    }

    public Single<Object> pushCommits(FetchResult fetchResult) {
        return sendCommitsToKafka(fetchResult.getCommits())
                .flatMap(it -> writeCommitsToDb(fetchResult.getCommits(), fetchResult));
    }

    private Single<Object> writeCommitsToDb(List<Commit> commits, FetchResult fetchResult) {
        if (commits.isEmpty()) {
            logger.info("No new commits!");
        } else {
            Commit latestCommit = commits.get(0);
            LatestCommit commit = new LatestCommit(
                    new RepositoryId(latestCommit.getRepository().getOwner(), latestCommit.getRepository().getName()),
                    latestCommit.getSha(),
                    toLocalDateTime(latestCommit.getCommitDate()),
                    fetchResult.getNextCursor().orElse(null)
            );

            latestCommitRepository.save(commit);
        }

        // we use blocking calls while saving to db, so just return void single after it
        return Single.just(SUCCESS);
    }

    private static LocalDateTime toLocalDateTime(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
    }

    private Single<Object> sendCommitsToKafka(List<Commit> commits) {
        List<Single<SendResult<String, RepositoryCommitMessage>>> sendSingles =
                commits.stream()
                        .map(commit ->
                                     Single.fromFuture(kafkaTemplate.send(KafkaConfig.COMMITS_TOPIC_NAME, commit.getSha(), RepositoryCommitMessage.newBuilder().setCommit(commit).build()))
                                             .doOnError(e -> logger.error("Wasn't able to send a commit: " + e.toString()))
                       )
                        .collect(Collectors.toList());

        return Single.merge(sendSingles).toList(commits.size()).map(it -> SUCCESS);
    }

}
