package org.example.notearchive.service;

import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.filestorage.FileStorage;
import org.example.notearchive.repository.StorageEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class StorageService {
    private final StorageEntryRepository storageEntryRepository;
    private final FileStorage fileStorage;

    public StorageService(StorageEntryRepository storageEntryRepository, FileStorage fileStorage) {
        this.storageEntryRepository = storageEntryRepository;
        this.fileStorage = fileStorage;
    }

    public void uploadMultipartFile(MultipartFile file, long parentId) throws StorageException {
        StorageEntry parent = storageEntryRepository
                .findById(parentId).orElseThrow(() -> new StorageException("Parent folder not found", null));

        uploadMultipartFile(
                file,
                parent,
                false
        );
    }

    public void uploadMultipartFile(MultipartFile file, StorageEntry root, boolean saveAsNestedFolders)
            throws StorageException {

        if (!saveAsNestedFolders || !fileStorage.tryToSaveAsNestedFolders(file, root)) {
            try {
                fileStorage.saveAsFile(file.getInputStream(), file.getOriginalFilename(), root);
            } catch (IOException e) {
                throw new StorageException("Could not save file", e);
            }
        }
        storageEntryRepository.save(root);
    }

    public void addMarkdownDescription(Note note, String markdownDescription) throws StorageException {
        fileStorage.saveAsFile(new ByteArrayInputStream(markdownDescription.getBytes()), "description.md", note.getContent());
        storageEntryRepository.save(note.getContent());
    }
}
