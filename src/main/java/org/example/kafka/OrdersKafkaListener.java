package org.example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.usecasses.dto.CreateOrderRequest;
import org.example.usecasses.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdersKafkaListener {
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${spring.kafka.topics.order-listener}",
            containerFactory = "stringListenerContainerFactory"
    )
    public void listenString(ConsumerRecord<String, String> record) {
        try {
            log.info("Received order event from partition: {}, offset: {}, {}",
                    record.partition(), record.offset(), record.value());

            CreateOrderRequest request = objectMapper.readValue(record.value(), CreateOrderRequest.class);
            orderService.createOrderAndOutboxEntity(request);
            log.info("OrderId: {}. Order Created Successfully", request.orderId());
        } catch (Exception e) {
            log.error(e.getMessage(), e); //TODO сделать обработку ошибок и ручной коммит
        }
    }
}
