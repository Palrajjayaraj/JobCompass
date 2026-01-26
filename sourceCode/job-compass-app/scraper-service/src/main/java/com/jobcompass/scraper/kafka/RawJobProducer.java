package com.jobcompass.scraper.kafka;

import com.jobcompass.common.events.RawJobEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer for publishing raw job events.
 * 
 * @author Palraj Jayaraj
 */
@Service
public class RawJobProducer {

    private static final Logger log = LoggerFactory.getLogger(RawJobProducer.class);
    private static final String TOPIC = "raw-jobs";
    
    private final KafkaTemplate<String, RawJobEvent> kafkaTemplate;

    public RawJobProducer(KafkaTemplate<String, RawJobEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a raw job event to Kafka
     */
    public void publishRawJob(RawJobEvent job) {
        try {
            kafkaTemplate.send(TOPIC, job.getSource().name(), job);
            log.debug("Published job to Kafka: {} - {}", job.getCompany(), job.getTitle());
        } catch (Exception e) {
            log.error("Failed to publish job to Kafka: {}", e.getMessage(), e);
        }
    }
}
