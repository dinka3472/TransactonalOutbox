package org.example.persistence.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "outbox_offsets",
        uniqueConstraints = @UniqueConstraint(name = "ix_outbox_offsets_topic_partition", columnNames = {"topic", "partition"}))
public class OutboxOffset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_processed_id", nullable = false)
    private Long lastProcessedId;

    @Column(name = "available_after", nullable = false)
    private OffsetDateTime availableAfter;

    @Column(name = "last_processed_transaction_id", nullable = false, columnDefinition = "xid8")
    private String lastProcessedTransactionId;

    @Column(name = "partition", nullable = false)
    private Integer partition;

    @Column(name = "topic", nullable = false, length = 128)
    private String topic;

}

