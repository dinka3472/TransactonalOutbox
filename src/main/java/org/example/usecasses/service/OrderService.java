package org.example.usecasses.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.persistence.models.OrderEntity;
import org.example.persistence.models.OutboxEntity;
import org.example.persistence.repo.OrderRepository;
import org.example.persistence.repo.OutboxRepository;
import org.example.processor.sendOrderProcessor.SendOrderProcessorProperties;
import org.example.usecasses.constants.MessageType;
import org.example.usecasses.dto.CreateOrderRequest;
import org.example.usecasses.util.PartitionResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final SendOrderProcessorProperties sendOrderProcessorProperties;
    private final PartitionResolver partitionResolver;


    @Transactional
    public void createOrderAndOutboxEntity(CreateOrderRequest request) throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(request);
        int partition = partitionResolver.forKey(request.clientId(), sendOrderProcessorProperties.getPartitions());

        OrderEntity orderEntity = OrderEntity.builder()
                .orderId(request.orderId())
                .clientId(request.clientId())
                .amount(request.amount())
                .build();
        orderRepository.save(orderEntity);

        OutboxEntity outboxEntity = OutboxEntity.builder()
                .key(request.clientId().toString())
                .createdAt(OffsetDateTime.now())
                .type(MessageType.ORDERS.name())
                .partition(partition)
                .payload(payload)
                .topic(sendOrderProcessorProperties.getTopic())
                .build();

        outboxRepository.save(outboxEntity);
    }
}
