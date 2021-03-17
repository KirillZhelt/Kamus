package com.kamus.loaderconfig.kafka;

import com.kamus.loaderconfig.grpcjava.TrackRepositoryRequest;
import com.kamus.loaderconfig.service.TrackedRepositoriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrackRepositoryConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TrackRepositoryConsumer.class);

    private final TrackedRepositoriesService repositoriesService;

    public TrackRepositoryConsumer(TrackedRepositoriesService repositoriesService) {
        this.repositoriesService = repositoriesService;
    }

    @KafkaListener(topics = "track.repository", containerFactory = "trackRepositoryListenerContainerFactory")
    public void trackRepositories(@Payload List<TrackRepositoryRequest> trackRepositoryRequest) {
        logger.info("Tracking repositories {}", trackRepositoryRequest);

        repositoriesService.trackRepositories(trackRepositoryRequest
                                                      .stream()
                                                      .map(TrackRepositoryRequest::getRepository)
                                                      .collect(Collectors.toList()));
    }

}
