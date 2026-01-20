package org.example.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.example.processor.sendOrderProcessor.SendOrderProcessorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic ordersTopic(SendOrderProcessorProperties properties) {
        return TopicBuilder.name(properties.getTopic())
                .partitions(properties.getPartitions())
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic ordersDlqTopic(SendOrderProcessorProperties properties) {
        return TopicBuilder.name("orders.dlq.v1")
                .partitions(properties.getPartitions())
                .replicas(3)
                .build();
    }
}
