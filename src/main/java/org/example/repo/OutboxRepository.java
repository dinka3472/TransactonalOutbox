package org.example.repo;

import jakarta.transaction.Transactional;
import org.example.db.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    /**
     * Получает batch сообщений для обработки по topic и partition
     */
    @Query(value = """
    SELECT *
    FROM outbox_messages
    WHERE topic = :topic
      AND partition = :partition
      AND (
            transaction_id  > CAST(:lastProcessedTransactionId AS xid8) 
         OR (transaction_id = CAST(:lastProcessedTransactionId AS xid8) AND id > :lastProcessedId)
      )
      AND transaction_id < pg_snapshot_xmin(pg_current_snapshot())
    ORDER BY transaction_id, id
    LIMIT :batchSize
    """, nativeQuery = true)
    List<OutboxEntity> findNextBatch(
            @Param("topic") String topic,
            @Param("partition") int partition,
            @Param("lastProcessedTransactionId") String lastProcessedTransactionId,
            @Param("lastProcessedId") Long lastProcessedId,
            @Param("batchSize") int batchSize
    );

}
