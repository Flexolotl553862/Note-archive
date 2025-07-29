package org.example.notearchive.service;

import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.repository.StorageEntryRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StorageEntryService {
    private final StorageEntryRepository storageEntryRepository;

    public StorageEntryService(StorageEntryRepository storageEntryRepository) {
        this.storageEntryRepository = storageEntryRepository;
    }

    public void getPath(StorageEntry entry, List<StorageEntry> path) {
        path.add(entry);
        if (entry.getParent() != null) {
            getPath(entry.getParent(), path);
        } else {
            Collections.reverse(path);
        }
    }

    public List<StorageEntry> getPath(StorageEntry entry) {
        List<StorageEntry> path = new ArrayList<>();
        getPath(entry, path);
        return path;
    }

    public void createEntry(StorageEntry root, String fileName) {
        StorageEntry entry = new StorageEntry(
                fileName,
                Path.of(root.getPath(), fileName).toFile().getPath(),
                StorageEntry.ENTRY_TYPE.FILE,
                root.getParentNote()
        );
        entry.setParent(root);
        entry.setParentNote(root.getParentNote());
        root.addChild(entry);
        storageEntryRepository.save(root);
    }
}
