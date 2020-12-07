package com.kamus.dataloader.db.repostitory;

import com.kamus.dataloader.db.model.LatestCommit;
import com.kamus.dataloader.db.model.RepositoryId;
import org.springframework.data.repository.CrudRepository;

public interface LatestCommitRepository extends CrudRepository<LatestCommit, RepositoryId> {

}
