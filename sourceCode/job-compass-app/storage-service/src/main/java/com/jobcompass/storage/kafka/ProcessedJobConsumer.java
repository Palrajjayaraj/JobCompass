package com.jobcompass.storage.kafka;

import com.jobcompass.common.events.ProcessedJobEvent;
import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for ProcessedJobEvent messages.
 * Listens to the processed-jobs topic and persists jobs to the database.
 * 
 * @author Palrajjayaraj
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessedJobConsumer {

    private final JobService jobService;

    /**
     * Consume ProcessedJobEvent messages from Kafka.
     * Saves or updates jobs in the database.
     * 
     * @param event the processed job event
     * @param partition the Kafka partition
     * @param offset the message offset
     */
    @KafkaListener(
        topics = "${kafka.topics.processed-jobs:processed-jobs}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
        @Payload ProcessedJobEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received ProcessedJobEvent from partition {} offset {}: {} at {}", 
            partition, offset, event.getTitle(), event.getCompany());

        try {
            Job savedJob = jobService.saveOrUpdateJob(event);
            log.info("Successfully saved job: {} (ID: {})", savedJob.getTitle(), savedJob.getId());
        } catch (Exception e) {
            log.error("Failed to save job from event: {}", event, e);
            // TODO: Implement Dead Letter Queue (DLQ) for failed messages
            // For now, log the error and continue processing
        }
    }
}
