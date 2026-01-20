package org.example.persistence.repo;

import org.example.persistence.models.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    /**
     * Получает следующую партию сообщений из outbox для обработки.
     * <p>
     * Метод выбирает сообщения, которые:
     * - принадлежат указанному топику и партиции
     * - ещё не были обработаны (с учётом последних обработанных transactionId и id)
     * - находятся в завершённых транзакциях (transaction_id < xmin текущего снапшота)
     * Сообщения возвращаются отсортированными по transaction_id, затем по id для гарантии порядка обработки.
     *
     * @param topic                      Топик Kafka для фильтрации сообщений
     * @param partition                  Партиция Kafka для фильтрации сообщений
     * @param lastProcessedTransactionId Transaction ID последней обработанной транзакции (в строковом формате, будет преобразован в xid8)
     * @param lastProcessedId            ID последнего обработанного сообщения в рамках указанной транзакции
     * @param batchSize                  Максимальное количество возвращаемых сообщений
     * @return Список сообщений для обработки, отсортированный по transaction_id и id
     *
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
