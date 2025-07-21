package org.example.notearchive.filestorage;

import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.exception.StorageException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

public interface FileStorage {

    boolean tryToSaveAsNestedFolders(MultipartFile file, StorageEntry root) throws StorageException;

    void saveAsFile(InputStream inputStream, String originalFilename, StorageEntry root) throws StorageException;

    File getEntryContent(StorageEntry entry) throws StorageException;

    void deleteEntry(StorageEntry entry) throws StorageException;

    void createDirectory(String directoryName, StorageEntry parent) throws StorageException;
}
