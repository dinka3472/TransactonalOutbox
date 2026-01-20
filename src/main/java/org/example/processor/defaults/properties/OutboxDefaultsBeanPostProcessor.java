package org.example.processor.defaults.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxDefaultsBeanPostProcessor implements BeanPostProcessor {

    private final OutboxDefaultsProperties defaults;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof AbstractOutboxProcessorProperties props) {
            props.applyDefaults(defaults);
        }
        return bean;
    }
}