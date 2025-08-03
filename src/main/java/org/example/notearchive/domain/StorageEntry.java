package org.example.notearchive.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @NotBlank
    private String path;

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

    public StorageEntry(String name, String path, ENTRY_TYPE type, Note parentNote) {
        this.name = name;
        this.path = path;
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
}
