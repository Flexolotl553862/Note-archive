package org.example.notearchive.filestorage;

import org.apache.tika.Tika;
import org.example.notearchive.domain.Note;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.exception.StorageException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public class SystemStorage implements FileStorage {

    private final String storagePath;

    public SystemStorage() {
        this.storagePath = System.getenv("STORAGE_PATH");
    }

    @Override
    public void createDirectory(String name, StorageEntry parent) throws StorageException {
        try {
            Files.createDirectories(Path.of(storagePath, parent.getPath(), name));
        } catch (IOException e) {
            throw new StorageException("Could not create directory: " + name, e);
        }
    }

    @Override
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

    @Override
    public byte[] getEntryContentAsZip(StorageEntry entry) throws StorageException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ZipOutputStream zos = new ZipOutputStream(baos);
            getZip(entry, zos, new StringBuilder());
            zos.finish();
        } catch (IOException e) {
            throw new StorageException("Could not find zip file: " + entry.getPath(), e);
        }
        return baos.toByteArray();
    }

    private void getZip(StorageEntry entry, ZipOutputStream zos, StringBuilder path) throws IOException, StorageException {
        int length = 0;
        if (!path.isEmpty()) {
            path.append("/");
            length++;
        }
        path.append(entry.getName());
        length += entry.getName().length();
        if (!entry.isDirectory()) {
            zos.putNextEntry(new ZipEntry(path.toString()));
            try (FileInputStream fis = new FileInputStream(getEntryContent(entry))) {
                fis.transferTo(zos);
            }
        } else {
            zos.putNextEntry(new ZipEntry(path + "/"));
        }
        zos.closeEntry();
        for (StorageEntry child : entry.getChildren()) {
            getZip(child, zos, path);
        }
        path.delete(path.length() - length, path.length());
    }

    @Override
    public void deleteEntryContent(StorageEntry entry) {
        FileSystemUtils.deleteRecursively(new File(storagePath, entry.getPath()));
    }

    @Override
    public void deleteNoteContent(Note note) {
        FileSystemUtils.deleteRecursively(new File(storagePath, note.getContent().getPath()));
    }

    @Override
    public void saveAsFile(InputStream data, String name, StorageEntry root) throws StorageException {
        try {
            Path filePath = Path.of(storagePath, root.getPath(), name);
            Files.createDirectories(filePath.getParent());
            data.transferTo(new FileOutputStream(filePath.toFile()));
        } catch (IOException e) {
            throw new StorageException("Could not save file: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveAsFile(MultipartFile data, StorageEntry root) throws StorageException {
        try {
            Path filePath = Path.of(storagePath, root.getPath(), data.getOriginalFilename());
            Files.createDirectories(filePath.getParent());
            data.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new StorageException("Could not save file: " + e.getMessage(), e);
        }
    }

    @Override
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

    @Override
    public ResponseEntity<Resource> getEntryContentForResponse(
            StorageEntry entry,
            String dispositionType,
            RedirectAttributes redirectAttributes
    ) {
        Tika tika = new Tika();
        try {
            Resource resource;
            HttpHeaders headers = new HttpHeaders();
            String name = entry.getName() + ".zip";
            if (entry.isDirectory()) {
                resource = new ByteArrayResource(getEntryContentAsZip(entry));
                headers.setContentType(MediaType.parseMediaType("application/zip"));
            } else {
                File file = getEntryContent(entry);
                resource = new FileSystemResource(file);
                headers.setContentType(MediaType.parseMediaType(tika.detect(file)));
                name = file.getName();
            }
            headers.setContentDisposition(ContentDisposition
                    .builder(dispositionType)
                    .filename(name, StandardCharsets.UTF_8)
                    .build()
            );
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (StorageException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Could not identify file extension"
            );
        }
        return ResponseEntity
                .status(302)
                .header(HttpHeaders.LOCATION, "/not/found")
                .build();
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
