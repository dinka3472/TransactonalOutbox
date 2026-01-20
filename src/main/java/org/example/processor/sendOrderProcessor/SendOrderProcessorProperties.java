package org.example.processor.sendOrderProcessor;

import lombok.Getter;
import lombok.Setter;
import org.example.processor.defaults.properties.AbstractOutboxProcessorProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "outbox.send-order")
@Getter
@Setter
public class SendOrderProcessorProperties extends AbstractOutboxProcessorProperties {
}
