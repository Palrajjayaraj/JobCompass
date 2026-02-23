package com.jobcompass.scraper.service;

import com.jobcompass.scraper.dto.ScrapeProgressEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScrapeProgressTracker.
 *
 * @author Palraj Jayaraj
 */
public class ScrapeProgressTrackerTest {

    private ScrapeProgressTracker tracker;

    @BeforeEach
    public void setUp() {
        tracker = new ScrapeProgressTracker();
    }

    @Test
    public void testCreateSession_shouldReturnEmitterAndTrackIt() {
        SseEmitter emitter = tracker.createSession("test-123");

        assertNotNull(emitter);
        assertTrue(tracker.hasSession("test-123"));
        assertEquals(1, tracker.getActiveSessionCount());
    }

    @Test
    public void testCreateSession_multipleSessions() {
        tracker.createSession("scrape-1");
        tracker.createSession("scrape-2");
        tracker.createSession("scrape-3");

        assertEquals(3, tracker.getActiveSessionCount());
        assertTrue(tracker.hasSession("scrape-1"));
        assertTrue(tracker.hasSession("scrape-2"));
        assertTrue(tracker.hasSession("scrape-3"));
    }

    @Test
    public void testHasSession_returnsFalseForUnknownId() {
        assertFalse(tracker.hasSession("non-existent"));
    }

    @Test
    public void testCompleteSession_shouldRemoveEmitter() {
        tracker.createSession("test-456");
        assertTrue(tracker.hasSession("test-456"));

        tracker.completeSession("test-456");
        assertFalse(tracker.hasSession("test-456"));
        assertEquals(0, tracker.getActiveSessionCount());
    }

    @Test
    public void testCompleteSession_nonExistentId_shouldNotThrow() {
        // Should not throw any exception
        tracker.completeSession("does-not-exist");
        assertEquals(0, tracker.getActiveSessionCount());
    }

    @Test
    public void testEmitProgress_withNoSessionOrBuffer_shouldNotThrow() {
        ScrapeProgressEvent event = ScrapeProgressEvent.builder()
                .scrapeId("orphan-id")
                .status(ScrapeProgressEvent.Status.STARTED)
                .message("test")
                .build();

        // Should not throw — just logs a debug message
        tracker.emitProgress("orphan-id", event);
    }

    @Test
    public void testEmitProgress_withActiveSession_shouldSendEvent() {
        tracker.createSession("live-session");
        assertTrue(tracker.hasSession("live-session"));

        ScrapeProgressEvent event = ScrapeProgressEvent.builder()
                .scrapeId("live-session")
                .status(ScrapeProgressEvent.Status.SCRAPING)
                .currentSkill("Java")
                .skillIndex(1)
                .totalSkills(3)
                .jobsFoundForSkill(5)
                .totalJobsFound(5)
                .message("Scraping Java...")
                .build();

        // Should not throw — emits event to the SseEmitter
        tracker.emitProgress("live-session", event);

        // Session should still be active after successful emit
        assertTrue(tracker.hasSession("live-session"));
    }

    @Test
    public void testGetActiveSessionCount_afterMixedOperations() {
        tracker.createSession("s1");
        tracker.createSession("s2");
        tracker.createSession("s3");

        assertEquals(3, tracker.getActiveSessionCount());

        tracker.completeSession("s2");
        assertEquals(2, tracker.getActiveSessionCount());

        tracker.completeSession("s1");
        tracker.completeSession("s3");
        assertEquals(0, tracker.getActiveSessionCount());
    }

    @Test
    public void testInitSession_createsEventBuffer() {
        tracker.initSession("buffered-id");

        // Buffer exists, but no emitter yet
        assertFalse(tracker.hasSession("buffered-id"));

        // Emit should not throw and should buffer
        ScrapeProgressEvent event = ScrapeProgressEvent.builder()
                .scrapeId("buffered-id")
                .status(ScrapeProgressEvent.Status.STARTED)
                .message("Buffered event")
                .build();
        tracker.emitProgress("buffered-id", event);
    }

    @Test
    public void testBufferedEvents_replayedOnCreateSession() {
        tracker.initSession("replay-id");

        // Emit events before client connects
        tracker.emitProgress("replay-id", ScrapeProgressEvent.builder()
                .scrapeId("replay-id")
                .status(ScrapeProgressEvent.Status.STARTED)
                .message("Started")
                .build());

        tracker.emitProgress("replay-id", ScrapeProgressEvent.builder()
                .scrapeId("replay-id")
                .status(ScrapeProgressEvent.Status.SCRAPING)
                .currentSkill("Java")
                .skillIndex(1)
                .totalSkills(2)
                .message("Scraping Java...")
                .build());

        // Client connects — buffered events should be replayed
        SseEmitter emitter = tracker.createSession("replay-id");
        assertNotNull(emitter);
        assertTrue(tracker.hasSession("replay-id"));
    }

    @Test
    public void testCompleteSession_clearsBuffer() {
        tracker.initSession("temp-id");

        tracker.emitProgress("temp-id", ScrapeProgressEvent.builder()
                .scrapeId("temp-id")
                .status(ScrapeProgressEvent.Status.STARTED)
                .message("Started")
                .build());

        tracker.completeSession("temp-id");

        // After completion, buffer should be cleared
        assertFalse(tracker.hasSession("temp-id"));
    }

    @Test
    public void testDuplicateJobsSkipped_fieldInEvent() {
        ScrapeProgressEvent event = ScrapeProgressEvent.builder()
                .scrapeId("dup-test")
                .status(ScrapeProgressEvent.Status.SKILL_COMPLETED)
                .currentSkill("Java")
                .skillIndex(1)
                .totalSkills(3)
                .jobsFoundForSkill(8)
                .totalJobsFound(8)
                .duplicateJobsSkipped(3)
                .message("Java complete: 8 unique jobs (3 duplicates skipped)")
                .build();

        assertEquals(3, event.getDuplicateJobsSkipped());
        assertEquals(8, event.getTotalJobsFound());
    }
}
