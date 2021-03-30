package com.kamus.watchdog.http;

import com.kamus.watchdog.http.model.RepositoryDto;
import com.kamus.watchdog.service.RepositoriesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users-repositories")
public class RepositoriesController {

    private final RepositoriesService repositoriesService;

    public RepositoriesController(RepositoriesService repositoriesService) {
        this.repositoriesService = repositoriesService;
    }

    @GetMapping("/repositories")
    public List<RepositoryDto> getUsersRepositories(OAuth2Authentication auth) {
        return repositoriesService.findUserRepositories(auth.getName());
    }

    @PostMapping("/repositories")
    public ResponseEntity<RepositoryDto> addRepository(@RequestBody RepositoryDto repositoryDto, OAuth2Authentication auth) throws IOException {
        boolean created = repositoriesService.addRepository(auth.getName(), repositoryDto);
        return new ResponseEntity<>(repositoryDto, created ? HttpStatus.CREATED : HttpStatus.OK);
    }

    @DeleteMapping("/repositories/{owner}/{name}")
    public boolean removeRepository(@PathVariable String owner, @PathVariable String name, OAuth2Authentication auth) {
        return repositoriesService.removeRepository(auth.getName(), owner, name);
    }

}
