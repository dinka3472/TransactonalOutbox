package org.example.repo;

import jakarta.transaction.Transactional;
import org.example.db.OutboxOffset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface OutboxOffsetRepository extends JpaRepository<OutboxOffset, Long> {

    /**
     * Выбирает одну доступную запись из outbox_offsets и обновляет available_after
     *
     * @param nowPlusLockTimeout новое значение available_after
     * @return обновлённая сущность
     */
    @Query(value = """
            WITH locked AS (
                                          SELECT id
                                          FROM outbox_offsets
                                          WHERE available_after < now()
                                          ORDER BY available_after
                                          LIMIT 1
                                          FOR UPDATE SKIP LOCKED
                                      )
                                      UPDATE outbox_offsets o
                                      SET available_after = :nowPlusLockTimeout
                                      WHERE o.id = (SELECT id FROM locked)
                                      RETURNING o.*
            """, nativeQuery = true)
    Optional<OutboxOffset> lockNextOffset(@Param("nowPlusLockTimeout") OffsetDateTime nowPlusLockTimeout);

    @Modifying
    @Transactional
    @Query(value = """
            UPDATE outbox_offsets
                SET
                        available_after = now(),
                        last_processed_transaction_id = CAST(:lastProcessedTransactionId as xid8),
                        last_processed_id = :lastProcessedId
                WHERE id = :id
            """, nativeQuery = true)
    void updatePartition(@Param("lastProcessedTransactionId") String lastProcessedTransactionId,
                         @Param("lastProcessedId") long lastProcessedId,
                         @Param("id") long id);

    @Modifying
    @Query(value = """
        INSERT INTO outbox_offsets (
            last_processed_id,
            last_processed_transaction_id,
            available_after,
            partition,
            topic
        )
        VALUES (0, pg_current_xact_id(), now(), :partition, :topic)
        ON CONFLICT (topic, partition) DO NOTHING
        """, nativeQuery = true)
    void insertIfNotExists(String topic, int partition);

}



