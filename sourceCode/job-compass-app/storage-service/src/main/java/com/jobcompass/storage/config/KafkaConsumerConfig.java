package com.jobcompass.storage.config;

import com.jobcompass.common.events.ProcessedJobEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for Kafka consumer.
 * Configures deserialization of ProcessedJobEvent messages.
 * 
 * @author Palrajjayaraj
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Create consumer configuration properties.
     * 
     * @return configuration properties map
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProcessedJobEvent.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.jobcompass.common.events");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return props;
    }

    /**
     * Create consumer factory for ProcessedJobEvent.
     * 
     * @return consumer factory
     */
    @Bean
    public ConsumerFactory<String, ProcessedJobEvent> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * Create listener container factory for Kafka consumers.
     * 
     * @return listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProcessedJobEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProcessedJobEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
