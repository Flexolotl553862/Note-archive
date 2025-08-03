package org.example.notearchive.filestorage;

import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.exception.StorageException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.InputStream;

public interface FileStorage {

    boolean tryToSaveAsNestedFolders(MultipartFile file, StorageEntry root) throws StorageException;

    void saveAsFile(InputStream inputStream, String originalFilename, StorageEntry root) throws StorageException;

    void saveAsFile(MultipartFile file, StorageEntry root) throws StorageException;

    File getEntryContent(StorageEntry entry) throws StorageException;

    byte[] getEntryContentAsZip(StorageEntry entry) throws StorageException;

    void deleteEntryContent(StorageEntry entry) throws StorageException;

    void deleteNoteContent(Note note) throws StorageException;

    void createDirectory(String directoryName, StorageEntry parent) throws StorageException;

    ResponseEntity<Resource> getEntryContentForResponse(
            StorageEntry entry,
            String dispositionType,
            RedirectAttributes redirectAttributes
    );
}
