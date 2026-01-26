package com.jobcompass.storage.kafka;

import com.jobcompass.common.events.ProcessedJobEvent;
import com.jobcompass.common.model.Source;
import com.jobcompass.storage.entity.Job;
import com.jobcompass.storage.service.JobService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProcessedJobConsumer.
 * 
 * @author Palrajjayaraj
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessedJobConsumerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private ProcessedJobConsumer consumer;

    private ProcessedJobEvent testEvent;

    @Before
    public void setUp() {
        testEvent = ProcessedJobEvent.builder()
                .source(Source.of("LinkedIn"))
                .title("Senior Java Developer")
                .company("Google")  // Note: field name is company, not companyName in builder usually
                .location("San Francisco, CA")
                .salary("$120k-$150k")
                .url("https://linkedin.com/jobs/123")
                .jobAgeInDays(2)
                .build();
    }

    @Test
    public void testConsume_Success() {
        when(jobService.saveOrUpdateJob(any(ProcessedJobEvent.class))).thenReturn(Job.builder().id(1L).title("Job").build());

        consumer.consume(testEvent, 0, 100L);

        verify(jobService, times(1)).saveOrUpdateJob(testEvent);
    }

    @Test
    public void testConsume_ErrorHandling() {
        when(jobService.saveOrUpdateJob(any(ProcessedJobEvent.class))).thenThrow(new RuntimeException("Database error"));

        // Should not throw exception - just log it
        consumer.consume(testEvent, 0, 100L);

        verify(jobService, times(1)).saveOrUpdateJob(testEvent);
    }
}
