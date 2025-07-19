package org.example.notearchive.repository;

import org.example.notearchive.domain.StorageEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageEntryRepository extends CrudRepository<StorageEntry, Long> {

}
