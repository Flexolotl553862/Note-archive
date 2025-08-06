package org.example.notearchive.service;

import lombok.RequiredArgsConstructor;
import org.example.notearchive.domain.Link;
import org.example.notearchive.domain.StorageEntry;
import org.example.notearchive.domain.User;
import org.example.notearchive.repository.StorageEntryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageEntryService {
    private final StorageEntryRepository storageEntryRepository;

    public void createEntry(StorageEntry root, String name, StorageEntry.ENTRY_TYPE type) {
        StorageEntry entry = new StorageEntry(
                name,
                type,
                root.getParentNote()
        );
        entry.setParent(root);
        entry.setParentNote(root.getParentNote());
        root.addChild(entry);
        storageEntryRepository.save(root);
    }

    public void deleteEntry(StorageEntry entry) {
        storageEntryRepository.delete(entry);
    }

    public boolean canChangeEntry(StorageEntry entry, Authentication authentication) {
        if (entry.getLock() && authentication.getPrincipal() instanceof User user) {
            return user.getId().equals(entry.getParentNote().getAuthor().getId());
        }
        return !entry.getLock() &&
                (entry.getParentNote().getEditors().stream().anyMatch(u -> u.equals(authentication.getPrincipal()))
                        || authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    public boolean canOpenEntry(StorageEntry entry, Authentication authentication) {
        if (entry.getLock() && authentication.getPrincipal() instanceof User user) {
            return user.getId().equals(entry.getParentNote().getAuthor().getId());
        }
        return authentication.getPrincipal() instanceof User;
    }

    public List<StorageEntry> getAccessibleChildren(StorageEntry entry, Link link) {
        return entry.getChildren().stream().filter(
                e -> !e.getLock() && e.getParentNote().getEditors().stream().anyMatch(
                        u -> u.getId().equals(link.getAuthor().getId())
                )
        ).toList();
    }

    private void setLockRecursively(StorageEntry entry, Boolean lock) {
        entry.setLock(lock);
        for (StorageEntry child : entry.getChildren()) {
            setLockRecursively(child, lock);
        }
    }

    public void setLock(StorageEntry entry, Boolean lock) {
        setLockRecursively(entry, lock);
        storageEntryRepository.save(entry);
    }
}
