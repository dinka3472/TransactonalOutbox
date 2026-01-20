package org.example.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;

public interface KafkaSendService {

    void sendBatchOrThrow(List<ProducerRecord<String, String>> records);
}
