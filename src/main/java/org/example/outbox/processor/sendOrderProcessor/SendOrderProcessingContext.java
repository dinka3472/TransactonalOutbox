package org.example.outbox.processor.sendOrderProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.kafka.KafkaSendService;
import org.example.persistence.models.OutboxEntity;
import org.example.persistence.models.OutboxOffset;
import org.example.persistence.repo.OutboxOffsetRepository;
import org.example.persistence.repo.OutboxRepository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class SendOrderProcessingContext {

    private final OutboxOffsetRepository offsetRepository;
    private final OutboxRepository messageRepository;
    private final KafkaSendService kafkaSendService;

    private OutboxOffset lockedOffset;
    private List<OutboxEntity> messages = List.of();
    private List<ProducerRecord<String, String>> records = List.of();

    /**
     * Инициализирует контекст обработки outbox-сообщений.
     * Контекст используется для одного цикла обработки.
     */
    public static SendOrderProcessingContext start(
            OutboxOffsetRepository offsetRepository,
            OutboxRepository messageRepository,
            KafkaSendService kafkaSendService
    ) {
        return new SendOrderProcessingContext(offsetRepository, messageRepository, kafkaSendService);
    }

    /**
     * Захватывает следующий доступный offset для обработки
     * и блокирует его до указанного времени.
     */
    public SendOrderProcessingContext lockOffset(Duration offsetLockTimeout) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime lockUntil = now.plusSeconds(offsetLockTimeout.toSeconds());

        log.debug("Trying to lock next offset. now={}, lockUntil={}, timeoutSec={}",
                now, lockUntil, offsetLockTimeout.toSeconds());

        this.lockedOffset = offsetRepository.lockNextOffset(lockUntil).orElse(null);

        if (this.lockedOffset != null) {
            log.info("Offset locked successfully. id={}, partition={}, topic={}, lockedUntil={}",
                    lockedOffset.getId(),
                    lockedOffset.getPartition(),
                    lockedOffset.getTopic(),
                    lockUntil);
        } else {
            log.debug("No available offset to lock at {}", now);
        }

        return this;
    }

    /**
     * Загружает следующую пачку сообщений outbox
     * для залоченного оффсета
     *
     * @param batchSize Размер батча сообщений для обработки
     */
    public SendOrderProcessingContext loadMessages(int batchSize) {
        if (lockedOffset == null) {
            log.debug("Skip loading messages: offset is not locked yet");
            this.messages = java.util.List.of();
            return this;
        }

        String topic = lockedOffset.getTopic();
        int partition = lockedOffset.getPartition();
        String lastTxId = lockedOffset.getLastProcessedTransactionId();
        long lastId = lockedOffset.getLastProcessedId();

        log.debug("Loading outbox messages. topic={}, partition={}, lastTxId={}, lastId={}, batchSize={}",
                topic, partition, lastTxId, lastId, batchSize);

        this.messages = messageRepository.findNextBatch(
                topic,
                partition,
                lastTxId,
                lastId,
                batchSize
        );

        if (messages.isEmpty()) {
            log.debug("No outbox messages found. topic={}, partition={}, lastTxId={}, lastId={}",
                    topic, partition, lastTxId, lastId);
        } else {
            var first = messages.get(0);
            var last = messages.get(messages.size() - 1);

            log.info("Loaded outbox batch. topic={}, partition={}, size={}, range=[{}..{}]",
                    topic, partition, messages.size(),
                    first.getId(), last.getId());
        }
        return this;
    }

    /**
     * Преобразует outbox-сообщения в {@link ProducerRecord}
     * для последующей отправки в Kafka.
     */
    public SendOrderProcessingContext prepareKafkaRecords() {
        if (messages == null || messages.isEmpty()) {
            log.debug("No outbox messages to convert into Kafka records");
            this.records = List.of();
            return this;
        }

        this.records = messages.stream()
                .map(outbox -> new ProducerRecord<>(
                        outbox.getTopic(),
                        outbox.getVirtualPartition(),
                        System.currentTimeMillis(),
                        outbox.getKey(),
                        outbox.getPayload()
                ))
                .toList();

        log.info("Prepared {} Kafka records for topic={} partition={}",
                records.size(),
                lockedOffset.getTopic(),
                lockedOffset.getPartition());
        return this;
    }

    /**
     * Отправляет подготовленные сообщения в Kafka транзакционно.
     */
    public SendOrderProcessingContext sendToKafka() {
        if (records.isEmpty()) {
            log.debug("No outbox messages to send");
            return this;
        }
        kafkaSendService.sendBatchOrThrow(records);
        records.forEach(record -> {
            log.info("Successful send message. {}", record.value());
        });
        return this;
    }

    /**
     * Обновляет offset до последнего успешно отправленного сообщения.
     * Вызывается только после успешной отправки в Kafka.
     */
    public SendOrderProcessingContext unlockPartitionAndUpdateOffset() {
        if (lockedOffset == null || messages.isEmpty()) {
            return this;
        }

        OutboxEntity last = messages.get(messages.size() - 1);
        offsetRepository.unlockPartitionAndUpdateOffset(
                last.getTransactionId(),
                last.getId(),
                lockedOffset.getId()
        );
        return this;
    }

    /**
     * Возвращает количество обработанных сообщений.
     */
    public int getProcessedCount() {
        return messages.size();
    }
}


