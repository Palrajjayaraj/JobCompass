package com.jobcompass.scraper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobcompass.scraper.dto.ScrapeProgressEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe tracker for active scrape sessions.
 * Manages SSE emitters and broadcasts progress events to connected clients.
 * Buffers events that arrive before a client connects, replaying them on
 * connection.
 *
 * @author Palraj Jayaraj
 */
@Service
@Slf4j
public class ScrapeProgressTracker {

    private static final long SSE_TIMEOUT = 600_000L; // 10 minutes

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ScrapeProgressEvent>> eventBuffers = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ScrapeProgressTracker() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Initialize a buffer for events before client connects.
     *
     * @param scrapeId unique identifier for this scrape session
     */
    public void initSession(String scrapeId) {
        eventBuffers.put(scrapeId, new CopyOnWriteArrayList<>());
        log.info("Initialized event buffer for scrapeId: {}", scrapeId);
    }

    /**
     * Create a new SSE emitter for a scrape session.
     * If buffered events exist, replays them immediately.
     *
     * @param scrapeId unique identifier for this scrape session
     * @return the SSE emitter to return from the controller
     */
    public SseEmitter createSession(String scrapeId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> {
            log.debug("SSE session completed: {}", scrapeId);
            emitters.remove(scrapeId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE session timed out: {}", scrapeId);
            emitters.remove(scrapeId);
        });
        emitter.onError(e -> {
            log.debug("SSE session error for {}: {}", scrapeId, e.getMessage());
            emitters.remove(scrapeId);
        });

        emitters.put(scrapeId, emitter);
        log.info("Created SSE session: {}", scrapeId);

        // Replay any buffered events
        List<ScrapeProgressEvent> buffered = eventBuffers.get(scrapeId);
        if (buffered != null && !buffered.isEmpty()) {
            log.info("Replaying {} buffered events for scrapeId: {}", buffered.size(), scrapeId);
            for (ScrapeProgressEvent event : buffered) {
                sendEvent(emitter, scrapeId, event);
            }
            buffered.clear();
        }

        return emitter;
    }

    /**
     * Emit a progress event to the connected client.
     * If no client is connected yet, buffers the event for later replay.
     *
     * @param scrapeId the scrape session ID
     * @param event    the progress event to send
     */
    public void emitProgress(String scrapeId, ScrapeProgressEvent event) {
        SseEmitter emitter = emitters.get(scrapeId);
        if (emitter == null) {
            // Buffer the event for later replay when client connects
            List<ScrapeProgressEvent> buffer = eventBuffers.get(scrapeId);
            if (buffer != null) {
                buffer.add(event);
                log.debug("Buffered event for scrapeId: {} (status: {})", scrapeId, event.getStatus());
            } else {
                log.debug("No SSE listener or buffer for scrapeId: {}", scrapeId);
            }
            return;
        }

        sendEvent(emitter, scrapeId, event);
    }

    /**
     * Send an event to a specific emitter.
     */
    private void sendEvent(SseEmitter emitter, String scrapeId, ScrapeProgressEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            emitter.send(SseEmitter.event()
                    .name("progress")
                    .data(json));
        } catch (IOException e) {
            log.warn("Failed to send SSE event for {}: {}", scrapeId, e.getMessage());
            emitters.remove(scrapeId);
        }
    }

    /**
     * Complete the SSE stream for a scrape session.
     *
     * @param scrapeId the scrape session ID
     */
    public void completeSession(String scrapeId) {
        SseEmitter emitter = emitters.get(scrapeId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("Error completing SSE session {}: {}", scrapeId, e.getMessage());
            }
            emitters.remove(scrapeId);
            log.info("Completed SSE session: {}", scrapeId);
        }
        eventBuffers.remove(scrapeId);
    }

    /**
     * Check if a session exists for the given scrape ID.
     *
     * @param scrapeId the scrape session ID
     * @return true if an active SSE emitter exists
     */
    public boolean hasSession(String scrapeId) {
        return emitters.containsKey(scrapeId);
    }

    /**
     * Get the count of active sessions (for monitoring).
     *
     * @return number of active SSE sessions
     */
    public int getActiveSessionCount() {
        return emitters.size();
    }
}
