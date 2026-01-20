package org.example.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.exception.KafkaSendException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaSendServiceImpl implements KafkaSendService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final Duration BATCH_ACK_TIMEOUT = Duration.ofSeconds(60);

    @Override
    @Transactional(transactionManager = "kafkaTransactionManager")
    public void sendBatchOrThrow(List<ProducerRecord<String, String>> records) {
        if (records == null || records.isEmpty()) {
            log.debug("Skip Kafka send: batch is empty");
            return;
        }

        int size = records.size();
        String topic = records.get(0).topic();
        Integer partition = records.get(0).partition();

        log.info("Sending Kafka batch transactionally. topic={} partition={} size={}", topic, partition, size);

        try {
            List<CompletableFuture<SendResult<String, String>>> futures = new ArrayList<>(size);

            for (ProducerRecord<String, String> record : records) {
                CompletableFuture<SendResult<String, String>> sendResultCF =
                        kafkaTemplate.send(record)
                                .whenComplete((r, e) -> {
                                    if (e != null) {
                                        log.error("Kafka send failed: {}", r.getProducerRecord(), e);
                                    }
                                });
                futures.add(sendResultCF);
            }

            CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            all.get(BATCH_ACK_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

            log.info("Kafka batch sent successfully. topic={} partition={} size={}", topic, partition, size);

        } catch (Exception e) {
            log.error("Kafka batch send failed. topic={} partition={} size={}. Error={}", topic, partition, size, e, e);

            throw new KafkaSendException("Failed to send Kafka batch transactionally", e);
        }
    }
}
