package com.kamus.watchdog.service;

import com.kamus.loaderconfig.grpcjava.TrackRepositoryRequest;
import com.kamus.watchdog.db.model.Repository;
import com.kamus.watchdog.db.model.User;
import com.kamus.watchdog.db.repository.RepositoriesRepository;
import com.kamus.watchdog.http.model.RepositoryDto;
import com.kamus.watchdog.service.exception.CannotTrackRepositoryException;
import com.kamus.watchdog.service.exception.RepositoryDoesNotExistsException;
import com.kamus.watchdog.service.exception.UserDoesNotExistException;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.kamus.watchdog.config.KafkaConfig.TRACK_REPOSITORY_TOPIC_NAME;

@Service
public class RepositoriesService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoriesService.class);

    private final DbUserDetailsService userService;
    private final RepositoriesRepository repositoriesRepository;
    private final KafkaTemplate<String, TrackRepositoryRequest> trackRepositoryProducer;
    private final GitHub github;

    public RepositoriesService(DbUserDetailsService userService,
                               RepositoriesRepository repositoriesRepository,
                               KafkaTemplate<String, TrackRepositoryRequest> trackRepositoryProducer,
                               GitHub github) {
        this.userService = userService;
        this.repositoriesRepository = repositoriesRepository;
        this.trackRepositoryProducer = trackRepositoryProducer;
        this.github = github;
    }

    public List<RepositoryDto> findUserRepositories(String username) {
        User user = userService.findUser(username).orElseThrow(() -> new UserDoesNotExistException(username));

        return repositoriesRepository.findAllByUser(user)
                       .stream()
                       .map(r -> new RepositoryDto(r.getId().getName(), r.getId().getOwner()))
                       .collect(Collectors.toList());
    }

    public boolean addRepository(String username, RepositoryDto repositoryDto) throws IOException {
        User user = userService.findUser(username).orElseThrow(() -> new UserDoesNotExistException(username));

        if (!repositoryExists(repositoryDto)) {
            throw new RepositoryDoesNotExistsException(repositoryDto);
        }

        Repository repository = new Repository(repositoryDto.getOwner(), repositoryDto.getName(), user);

        if (repositoriesRepository.existsById(repository.getId())) {
            return false;
        }

        logger.info("Adding repository {} for user {}", repositoryDto, username);

        try {
            com.kamus.common.grpcjava.Repository repositoryProto = com.kamus.common.grpcjava.Repository.newBuilder()
                                                                           .setName(repositoryDto.getName())
                                                                           .setOwner(repositoryDto.getOwner())
                                                                           .build();
            trackRepositoryProducer.send(TRACK_REPOSITORY_TOPIC_NAME,
                    TrackRepositoryRequest.newBuilder().setRepository(repositoryProto).build()).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Got an exception while sending a repository to track.repository topic");
            throw new CannotTrackRepositoryException("Wasn't able to send a message to track.repository topic", e);
        }

        repositoriesRepository.save(repository);
        return true;
    }

    public boolean removeRepository(String username, String owner, String name) {
        User user = userService.findUser(username).orElseThrow(() -> new UserDoesNotExistException(username));
        Repository repository = new Repository(owner, name, user);

        if (repositoriesRepository.existsById(repository.getId())) {
            return false;
        }

        repositoriesRepository.delete(repository);
        return true;
    }

    private boolean repositoryExists(RepositoryDto repositoryDto) throws IOException {
        try {
            github.getRepository(repositoryDto.getOwner() + "/" + repositoryDto.getName());
            return true;
        } catch (GHFileNotFoundException e) {
            return false;
        }
    }

}
