package org.example.notearchive.filestorage;

import org.apache.tika.Tika;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.exception.StorageException;
import org.example.notearchive.repository.StorageEntryRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class SystemStorage implements FileStorage {

    private final String storagePath;
    private final StorageEntryRepository storageEntryRepository;

    public SystemStorage(StorageEntryRepository storageEntryRepository) {
        this.storagePath = System.getenv("STORAGE_PATH");
        this.storageEntryRepository = storageEntryRepository;
    }

    public void createDirectory(String name, StorageEntry parent) throws StorageException {
        StorageEntry entry = new StorageEntry(
                name,
                Path.of(parent.getPath(), name).toString(),
                StorageEntry.ENTRY_TYPE.DIRECTORY,
                parent.getParentNote()
        );
        entry.setParent(parent);
        try {
            Files.createDirectories(Path.of(storagePath, entry.getPath()));
        } catch (IOException e) {
            throw new StorageException("Could not create directory: " + entry.getPath(), e);
        }
        storageEntryRepository.save(entry);
    }

    public File getEntryContent(StorageEntry entry) throws StorageException {
        if (entry.getPath() == null || entry.getPath().isEmpty()) {
            throw new StorageException("Could not find path to file: " + entry.getName(), null);
        }
        File file = new File(storagePath, entry.getPath());
        if (!file.exists()) {
            throw new StorageException("File content doesn't exists", null);
        }
        return file;
    }

    public void deleteEntry(StorageEntry entry) {
        FileSystemUtils.deleteRecursively(new File(storagePath, entry.getPath()));
        storageEntryRepository.delete(entry);
    }

    public void saveAsFile(InputStream data, String name, StorageEntry root) throws StorageException {
        try {
            Path filePath = Path.of(storagePath, root.getPath(), name);
            Files.createDirectories(filePath.getParent());
            data.transferTo(new FileOutputStream(filePath.toFile()));
            StorageEntry entry = new StorageEntry(
                    name,
                    Path.of(root.getPath(), name).toFile().getPath(),
                    StorageEntry.ENTRY_TYPE.FILE,
                    root.getParentNote()
            );
            entry.setParent(root);
            entry.setParentNote(root.getParentNote());
            root.addChild(entry);
        } catch (IOException e) {
            throw new StorageException("Could not save file: " + e.getMessage(), e);
        } catch (InvalidPathException e) {
            throw new StorageException("Invalid path: " + e.getMessage(), e);
        }
    }

    public boolean tryToSaveAsNestedFolders(MultipartFile file, StorageEntry root)
            throws StorageException {
        ZipInputStream zis;
        Tika tika = new Tika();
        try {
            zis = new ZipInputStream(file.getInputStream());
            if (!tika.detect(file.getInputStream()).equals("application/zip")) {
                return false;
            }
        } catch (IOException e) {
            throw new StorageException("Could not open input file", e);
        }

        Map<Path, StorageEntry> backlinks = new HashMap<>();
        boolean notEmpty = false;
        try {
            backlinks.put(Path.of(root.getPath()), root);
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                notEmpty = true;
                Path curpath = Path.of(storagePath, root.getPath(), ze.getName());
                if (ze.isDirectory()) {
                    Files.createDirectories(curpath);
                } else {
                    Files.createDirectories(curpath.getParent());
                    File createdFile = Files.createFile(curpath).toFile();
                    zis.transferTo(new FileOutputStream(createdFile));
                }
                Path entryPath = Path.of(root.getPath(), ze.getName());
                backlinks.put(
                        entryPath,
                        new StorageEntry(
                                curpath.toFile().getName(),
                                entryPath.toFile().getPath(),
                                ze.isDirectory() ? StorageEntry.ENTRY_TYPE.DIRECTORY : StorageEntry.ENTRY_TYPE.FILE,
                                root.getParentNote()
                        )
                );
            }
        } catch (IOException e) {
            throw new StorageException("Could not save: " + e.getMessage(), e);
        } catch (InvalidPathException e) {
            throw new StorageException("Invalid path: " + e.getMessage(), e);
        }
        addBacklinks(backlinks, root);
        return notEmpty;
    }

    private void addBacklinks(Map<Path, StorageEntry> backlinks, StorageEntry root) throws StorageException {
        for (Map.Entry<Path, StorageEntry> entry : backlinks.entrySet()) {
            Path path = entry.getKey();
            StorageEntry storageEntry = entry.getValue();
            if (root.getPath().equals(storageEntry.getPath())) {
                continue;
            }
            StorageEntry parent = backlinks.get(path.getParent());
            if (parent == null) {
                throw new StorageException(
                        "Parent directory for " + path.toFile().getAbsolutePath() + " is absent",
                        null
                );
            }
            storageEntry.setParent(parent);
            parent.addChild(storageEntry);
        }
    }
}
