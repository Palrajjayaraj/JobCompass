package com.jobcompass.storage.kafka;

import com.jobcompass.common.events.RawJobEvent;
import com.jobcompass.common.model.Source;
import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.service.JobService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RawJobConsumer.
 * Tests Kafka message consumption and processing of raw job events.
 * 
 * @author Palrajjayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class RawJobConsumerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private RawJobConsumer rawJobConsumer;

    private RawJobEvent testEvent;
    private Job testJob;

    @Before
    public void setUp() {
        // Create test RawJobEvent
        testEvent = RawJobEvent.builder()
                .source(Source.of("LINKEDIN"))
                .title("Senior Java Developer")
                .company("Google")
                .location("San Francisco, CA")
                .description("Exciting opportunity for a Senior Java Developer...")
                .url("https://linkedin.com/jobs/12345")
                .postedDate("2 days ago")
                .scrapedAt(LocalDateTime.now())
                .build();

        // Create test Job entity
        testJob = Job.builder()
                .id(1L)
                .title("Senior Java Developer")
                .url("https://linkedin.com/jobs/12345")
                .build();
    }

    /**
     * Test successful consumption of RawJobEvent from Kafka.
     * Verifies that the consumer calls JobService.saveRawJob().
     */
    @Test
    public void testConsumeRawJobEvent_Success() {
        // Arrange
        when(jobService.saveRawJob(any(RawJobEvent.class))).thenReturn(testJob);

        // Act
        rawJobConsumer.consume(testEvent, 0, 100L);

        // Assert
        verify(jobService, times(1)).saveRawJob(testEvent);
    }

    /**
     * Test consumption with null event.
     * Consumer should handle gracefully without throwing exception.
     */
    @Test
    public void testConsumeRawJobEvent_NullEvent() {
        // Act & Assert - should not throw exception
        try {
            rawJobConsumer.consume(null, 0, 100L);
        } catch (Exception e) {
            // Expected to handle null gracefully or throw
        }

        // Verify saveRawJob was not called with null
        verify(jobService, never()).saveRawJob(null);
    }

    /**
     * Test consumption when JobService throws exception.
     * Consumer should log error and continue (not re-throw).
     */
    @Test
    public void testConsumeRawJobEvent_ServiceException() {
        // Arrange
        when(jobService.saveRawJob(any(RawJobEvent.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act - should not throw exception (catches and logs)
        rawJobConsumer.consume(testEvent, 0, 100L);

        // Assert
        verify(jobService, times(1)).saveRawJob(testEvent);
    }

    /**
     * Test consumption with minimal RawJobEvent data.
     * Consumer should handle events with only required fields.
     */
    @Test
    public void testConsumeRawJobEvent_MinimalData() {
        // Arrange
        RawJobEvent minimalEvent = RawJobEvent.builder()
                .title("Developer")
                .url("https://example.com/job")
                .build();

        when(jobService.saveRawJob(any(RawJobEvent.class))).thenReturn(testJob);

        // Act
        rawJobConsumer.consume(minimalEvent, 0, 100L);

        // Assert
        verify(jobService, times(1)).saveRawJob(minimalEvent);
    }

    /**
     * Test consumption from different Kafka partitions.
     * Consumer should process events from any partition.
     */
    @Test
    public void testConsumeRawJobEvent_DifferentPartitions() {
        // Arrange
        when(jobService.saveRawJob(any(RawJobEvent.class))).thenReturn(testJob);

        // Act - consume from partition 0, 1, 2
        rawJobConsumer.consume(testEvent, 0, 100L);
        rawJobConsumer.consume(testEvent, 1, 101L);
        rawJobConsumer.consume(testEvent, 2, 102L);

        // Assert
        verify(jobService, times(3)).saveRawJob(testEvent);
    }

    /**
     * Test consumption with different event sources.
     * Consumer should handle jobs from all sources (LinkedIn, Glassdoor, Indeed).
     */
    @Test
    public void testConsumeRawJobEvent_DifferentSources() {
        // Arrange
        when(jobService.saveRawJob(any(RawJobEvent.class))).thenReturn(testJob);

        RawJobEvent linkedInEvent = RawJobEvent.builder()
                .source(Source.of("LINKEDIN"))
                .title("Job 1")
                .url("https://linkedin.com/1")
                .build();

        RawJobEvent glassdoorEvent = RawJobEvent.builder()
                .source(Source.of("GLASSDOOR"))
                .title("Job 2")
                .url("https://glassdoor.com/2")
                .build();

        RawJobEvent indeedEvent = RawJobEvent.builder()
                .source(Source.of("INDEED"))
                .title("Job 3")
                .url("https://indeed.com/3")
                .build();

        // Act
        rawJobConsumer.consume(linkedInEvent, 0, 100L);
        rawJobConsumer.consume(glassdoorEvent, 0, 101L);
        rawJobConsumer.consume(indeedEvent, 0, 102L);

        // Assert
        verify(jobService, times(1)).saveRawJob(linkedInEvent);
        verify(jobService, times(1)).saveRawJob(glassdoorEvent);
        verify(jobService, times(1)).saveRawJob(indeedEvent);
    }
}
