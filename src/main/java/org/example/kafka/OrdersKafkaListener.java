package org.example.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.usecasses.dto.CreateOrderRequest;
import org.example.usecasses.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
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
    public void listenString(ConsumerRecord<String, String> rec) {
        try {
            log.info("Received order event from partition: {}, offset: {}, {}", rec.partition(), rec.offset(), rec.value());
//TODO проверить на идемпотентность
            CreateOrderRequest request = objectMapper.readValue(rec.value(), CreateOrderRequest.class);
            orderService.createOrderAndOutboxEntity(request);
            log.info("OrderId: {}. Order Created Successfully", request.orderId());
        } catch (Exception e) {
            log.error(e.getMessage(), e); //TODO сделать обработку ошибок и ручной коммит
        }
    }
}
