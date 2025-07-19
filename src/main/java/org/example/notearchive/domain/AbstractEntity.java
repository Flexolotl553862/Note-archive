package org.example.notearchive.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.util.Date;

@Data
@MappedSuperclass
public class AbstractEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;
}
