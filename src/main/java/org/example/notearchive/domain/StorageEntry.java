package org.example.notearchive.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "entry")
public class StorageEntry extends AbstractEntity {
    public enum ENTRY_TYPE {
        FILE,
        DIRECTORY
    }

    @NotBlank
    private String name;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StorageEntry> children;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "parent_id")
    private StorageEntry parent;

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    Date lastModified;

    @NotNull
    private ENTRY_TYPE type;

    private Boolean lock;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Note parentNote;

    public boolean isDirectory() {
        return type == ENTRY_TYPE.DIRECTORY;
    }

    public StorageEntry(String name, ENTRY_TYPE type, Note parentNote) {
        this.name = name;
        this.type = type;
        this.lock = false;
        this.parentNote = parentNote;
    }

    public void addChild(StorageEntry child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    private void getPath(StorageEntry entry, List<StorageEntry> path) {
        path.add(entry);
        if (entry.getParent() != null) {
            getPath(entry.getParent(), path);
        } else {
            Collections.reverse(path);
        }
    }

    public List<StorageEntry> getPathAsEntryList() {
        List<StorageEntry> path = new ArrayList<>();
        getPath(this, path);
        return path;
    }

    public String getPath() {
        return getPathAsEntryList().stream().map(StorageEntry::getName).collect(Collectors.joining("/"));
    }
}
