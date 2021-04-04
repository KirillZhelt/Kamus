package com.kamus.watchdog.http;

import com.kamus.watchdog.http.model.RepositoryDto;
import com.kamus.watchdog.http.model.RepositoryStatsDto;
import com.kamus.watchdog.service.RepositoriesService;
import com.kamus.watchdog.service.RepositoryStatsService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class RepositoryCountsController {

    private final RepositoriesService repositoriesService;
    private final RepositoryStatsService repositoryStatsService;

    public RepositoryCountsController(RepositoriesService repositoriesService,
                                      RepositoryStatsService repositoryStatsService) {
        this.repositoriesService = repositoriesService;
        this.repositoryStatsService = repositoryStatsService;
    }

    @GetMapping("/repositories/{owner}/{name}/stats")
    public ResponseEntity<RepositoryStatsDto> stats(@PathVariable String owner, @PathVariable String name, OAuth2Authentication auth) {
        if (!repositoriesService.repositoryExistsForUser(auth.getName(), owner, name)) {
            return ResponseEntity.notFound().build();
        }

        try {
            RepositoryStatsDto statsDto = repositoryStatsService.getStats(new RepositoryDto(name, owner));
            return ResponseEntity.ok(statsDto);
        } catch (StatusRuntimeException e) {
            if (!e.getStatus().equals(Status.NOT_FOUND)) {
                throw e;
            }

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

}
