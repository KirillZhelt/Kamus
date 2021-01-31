package com.kamus.loaderconfig.service;

import com.kamus.common.grpcjava.Repository;
import com.kamus.loaderconfig.db.repository.RepositoriesRepository;
import com.kamus.loaderconfig.db.model.RepositoryId;
import com.kamus.loaderconfig.db.model.TrackedRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TrackedRepositoriesService {

    private final RepositoriesRepository repositoriesRepository;

    public TrackedRepositoriesService(RepositoriesRepository repositoriesRepository) {
        this.repositoriesRepository = repositoriesRepository;
    }

    public void trackRepository(Repository repository) {
        RepositoryId id = toRepositoryId(repository);

        Optional<TrackedRepository> trackedRepositoryOptional = repositoriesRepository.findById(id);
        if (trackedRepositoryOptional.isPresent()) {
            TrackedRepository trackedRepository = trackedRepositoryOptional.get();
            trackedRepository.setTracked(true);
            repositoriesRepository.save(trackedRepository);
        } else {
            repositoriesRepository.save(new TrackedRepository(id, 1, true));
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
