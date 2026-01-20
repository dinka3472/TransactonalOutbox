package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.config.OutboxProperties;
import org.example.db.OrderEntity;
import org.example.db.OutboxEntity;
import org.example.dto.CreateOrderRequest;
import org.example.repo.OrderRepository;
import org.example.repo.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final OutboxProperties outboxProperties;


    @Transactional
    public void save(CreateOrderRequest request) throws JsonProcessingException {
        OrderEntity orderEntity = OrderEntity.builder()
                .orderId(request.orderId())
                .clientId(request.clientId())
                .amount(request.amount())
                .build();
        OutboxEntity entity = OutboxEntity.builder()
                .key(request.clientId().toString())
                .createdAt(OffsetDateTime.now())
                .type("order")
                .partition(resolvePartition(request.clientId(), outboxProperties.getPartitions()))
                .payload(objectMapper.writeValueAsString(request))
                .topic("orders.events")
                .build();
        outboxRepository.save(entity);
        orderRepository.save(orderEntity);
    }

    public int resolvePartition(Integer key, int partitions) {
        return Math.abs(key.hashCode()) % partitions;
    }
}
