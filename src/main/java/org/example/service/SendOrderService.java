package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.db.OutboxEntity;
import org.example.db.OutboxOffset;
import org.example.repo.OutboxOffsetRepository;
import org.example.repo.OutboxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendOrderService {

    private final OutboxOffsetRepository offsetRepository;
    private final OutboxRepository messageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final int BATCH_SIZE = 50;

    private static final Duration OFFSET_LOCK_TIMEOUT = Duration.ofSeconds(30);

    private static final Duration KAFKA_SEND_TIMEOUT = Duration.ofSeconds(60);

    // оставила, хотя пока не используется (чтобы логика не "ломалась", просто не трогаем TODO)
    private static final int LOCKED_DELAY_SEC = 5;
    private static final int NO_MESSAGES_DELAY_SEC = 10;

    /**
     * Возвращает количество успешно отправленных сообщений.
     */
    public int processOutboxMessages() {
        OffsetDateTime availableUntil = OffsetDateTime.now().plus(OFFSET_LOCK_TIMEOUT);

        Optional<OutboxOffset> lockedOffsetOpt = offsetRepository.lockNextOffset(availableUntil);
        if (lockedOffsetOpt.isEmpty()) {
            return 0;
        }

        OutboxOffset lockedOffset = lockedOffsetOpt.get();

        List<OutboxEntity> messages = messageRepository.findNextBatch(
                lockedOffset.getTopic(),
                lockedOffset.getPartition(),
                lockedOffset.getLastProcessedTransactionId(),
                lockedOffset.getLastProcessedId(),
                BATCH_SIZE
        );

        if (messages.isEmpty()) {
            // TODO: тут можно выставлять available_after с NO_MESSAGES_DELAY_SEC, только может не с такой настройкой шедулинга(с засыпанием потока при 0 обработанных сообщений) надо подумать
            return 0;
        }

        sendBatchToKafka(messages);

        OutboxEntity lastMessage = messages.get(messages.size() - 1);
        offsetRepository.updatePartition(
                lastMessage.getTransactionId(),
                lastMessage.getId(),
                lockedOffset.getId()
        );

        return messages.size();
    }

    private void sendBatchToKafka(List<OutboxEntity> messages) {
        for (OutboxEntity msg : messages) {
            try {
                kafkaTemplate
                        .send(
                                msg.getTopic(),
                                msg.getPartition(),
                                System.currentTimeMillis(),
                                msg.getKey(),
                                msg.getPayload()
                        )
                        .get(KAFKA_SEND_TIMEOUT.toSeconds(), TimeUnit.SECONDS);

            } catch (Exception e) {
                log.error(
                        "Failed to send outbox message to Kafka. topic={}, partition={}, messageId={}, txId={}",
                        msg.getTopic(), msg.getPartition(), msg.getId(), msg.getTransactionId(), e
                );
                throw new RuntimeException(e);
            }
        }
    }
}
