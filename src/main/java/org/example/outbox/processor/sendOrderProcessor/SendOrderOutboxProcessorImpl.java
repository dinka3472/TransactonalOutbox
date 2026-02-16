package org.example.outbox.processor.sendOrderProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.KafkaSendService;
import org.example.persistence.repo.OutboxOffsetRepository;
import org.example.persistence.repo.OutboxRepository;
import org.example.outbox.processor.OutboxProcessor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendOrderOutboxProcessorImpl implements OutboxProcessor {
    private final OutboxOffsetRepository offsetRepository;
    private final OutboxRepository messageRepository;
    private final KafkaSendService kafkaTransactionalSender;
    private final SendOrderProcessorProperties properties;

    /**
     * Возвращает количество успешно обработанных сообщений.
     */
    public int processOutboxMessages() {
        //TODO чот не нравится передавать в методы проперти, надо чтобы контекст через билдер создавался
        // и сразу в него все нужное передавать. Да и вообще юзать лучше обычную цепочку походу

        return SendOrderProcessingContext.start(offsetRepository, messageRepository, kafkaTransactionalSender)
                .lockOffset(properties.getLockTimeout())
                .loadMessages(properties.getBatchSize())
                .prepareKafkaRecords()
                .sendToKafka()
                .unlockPartitionAndUpdateOffset()
                .getProcessedCount();
    }
}
