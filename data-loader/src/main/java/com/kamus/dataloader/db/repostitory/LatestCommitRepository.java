package com.kamus.dataloader.db.repostitory;

import com.kamus.core.db.RepositoryId;
import com.kamus.dataloader.db.model.LatestCommit;
import org.springframework.data.repository.CrudRepository;

public interface LatestCommitRepository extends CrudRepository<LatestCommit, RepositoryId> {

}
