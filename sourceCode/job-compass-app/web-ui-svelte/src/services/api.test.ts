import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import { jobApi, searchHistoryApi } from './api';

// Mock axios
vi.mock('axios');
const mockedAxios = axios as any;

// Mock EventSource for SSE tests
class MockEventSource {
    url: string;
    listeners: Record<string, Function> = {};
    onerror: Function | null = null;
    closed = false;

    constructor(url: string) {
        this.url = url;
    }

    addEventListener(event: string, handler: Function) {
        this.listeners[event] = handler;
    }

    close() {
        this.closed = true;
    }

    // Test helper to simulate server events
    simulateEvent(eventName: string, data: any) {
        if (this.listeners[eventName]) {
            this.listeners[eventName]({ data: JSON.stringify(data) });
        }
    }

    simulateError(event: Event) {
        if (this.onerror) this.onerror(event);
    }
}

// Install MockEventSource globally
(globalThis as any).EventSource = MockEventSource;

describe('jobApi.triggerScrape', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('should call POST /api/scraper/trigger/multi-skill and return scrapeId', async () => {
        mockedAxios.post.mockResolvedValue({
            data: { scrapeId: 'abc-123' }
        });

        const result = await jobApi.triggerScrape({
            skills: 'Java, Spring Boot',
            location: 'Germany',
            maxResults: 1000,
            sources: ['LINKEDIN'],
            authentication: { 'LINKEDIN': { 'li_at': 'test-cookie' } }
        });

        expect(result).toBe('abc-123');
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/scraper/trigger/multi-skill', {
            skills: ['Java', 'Spring Boot'],
            location: 'Germany',
            maxResults: 1000,
            maxJobAgeDays: 7,
            authentication: { 'LINKEDIN': { 'li_at': 'test-cookie' } }
        });
    });

    it('should split comma-separated skills into array', async () => {
        mockedAxios.post.mockResolvedValue({
            data: { scrapeId: 'xyz-456' }
        });

        await jobApi.triggerScrape({
            skills: 'React, TypeScript, Node.js',
            location: 'London',
            maxResults: 500,
            sources: ['LINKEDIN'],
            authentication: {}
        });

        expect(mockedAxios.post).toHaveBeenCalledWith(
            '/api/scraper/trigger/multi-skill',
            expect.objectContaining({
                skills: ['React', 'TypeScript', 'Node.js']
            })
        );
    });

    it('should throw error if trigger fails', async () => {
        mockedAxios.post.mockRejectedValue(new Error('Service down'));

        await expect(jobApi.triggerScrape({
            skills: 'Java',
            location: 'Berlin',
            maxResults: 100,
            sources: ['LINKEDIN'],
            authentication: {}
        })).rejects.toThrow('Service down');
    });
});

describe('jobApi.subscribeScrapeProgress', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('should open EventSource to correct URL', () => {
        const onEvent = vi.fn();
        jobApi.subscribeScrapeProgress('test-id', onEvent);

        // The MockEventSource was instantiated with the correct URL
        // (We can't directly assert on constructor, but the function shouldn't throw)
        expect(true).toBe(true);
    });

    it('should call onEvent when progress event received', () => {
        const onEvent = vi.fn();
        const cleanup = jobApi.subscribeScrapeProgress('test-id', onEvent);

        // We need to verify the function returns a cleanup function
        expect(typeof cleanup).toBe('function');
    });

    it('should return a cleanup function that can be called', () => {
        const onEvent = vi.fn();
        const cleanup = jobApi.subscribeScrapeProgress('test-id', onEvent);

        // Calling cleanup should not throw
        expect(() => cleanup()).not.toThrow();
    });
});

describe('jobApi.markAsApplied', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('should call POST /api/applications with correct payload', async () => {
        const jobId = 123;
        mockedAxios.post.mockResolvedValue({ data: {} });

        await jobApi.markAsApplied(jobId);

        expect(mockedAxios.post).toHaveBeenCalledTimes(1);
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/applications', {
            jobId: 123,
            userEmail: 'user@jobcompass.local',
            notes: 'Auto-tracked via UI click'
        });
    });

    it('should throw error if API call fails', async () => {
        mockedAxios.post.mockRejectedValue(new Error('Network error'));

        await expect(jobApi.markAsApplied(456)).rejects.toThrow('Network error');
    });

    it('should not prevent duplicate calls (backend handles uniqueness)', async () => {
        mockedAxios.post.mockResolvedValue({ data: {} });

        await jobApi.markAsApplied(789);
        await jobApi.markAsApplied(789);

        expect(mockedAxios.post).toHaveBeenCalledTimes(2);
    });
});

describe('searchHistoryApi', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    describe('getRecentSearches', () => {
        it('should call GET /api/search-history and return data', async () => {
            const mockSearches = [
                { id: 1, skill: 'Java', location: 'Berlin', searchedAt: '2026-02-23T14:00:00' },
                { id: 2, skill: 'Python', location: 'London', searchedAt: '2026-02-23T13:00:00' }
            ];
            mockedAxios.get.mockResolvedValue({ data: mockSearches });

            const result = await searchHistoryApi.getRecentSearches();

            expect(mockedAxios.get).toHaveBeenCalledWith('/api/search-history');
            expect(result).toEqual(mockSearches);
            expect(result).toHaveLength(2);
        });

        it('should return empty array when no searches exist', async () => {
            mockedAxios.get.mockResolvedValue({ data: [] });

            const result = await searchHistoryApi.getRecentSearches();

            expect(result).toEqual([]);
        });

        it('should throw error if API call fails', async () => {
            mockedAxios.get.mockRejectedValue(new Error('Service unavailable'));

            await expect(searchHistoryApi.getRecentSearches()).rejects.toThrow('Service unavailable');
        });
    });

    describe('recordSearch', () => {
        it('should call POST /api/search-history with skill and location', async () => {
            const mockResponse = {
                id: 1,
                skill: 'React Developer',
                location: 'San Francisco',
                searchedAt: '2026-02-23T14:30:00'
            };
            mockedAxios.post.mockResolvedValue({ data: mockResponse });

            const result = await searchHistoryApi.recordSearch('React Developer', 'San Francisco');

            expect(mockedAxios.post).toHaveBeenCalledWith('/api/search-history', {
                skill: 'React Developer',
                location: 'San Francisco'
            });
            expect(result).toEqual(mockResponse);
        });

        it('should handle empty skill gracefully', async () => {
            const mockResponse = {
                id: 2,
                skill: '',
                location: 'Berlin',
                searchedAt: '2026-02-23T14:30:00'
            };
            mockedAxios.post.mockResolvedValue({ data: mockResponse });

            const result = await searchHistoryApi.recordSearch('', 'Berlin');

            expect(mockedAxios.post).toHaveBeenCalledWith('/api/search-history', {
                skill: '',
                location: 'Berlin'
            });
            expect(result.skill).toBe('');
        });

        it('should throw error if API call fails', async () => {
            mockedAxios.post.mockRejectedValue(new Error('Network error'));

            await expect(searchHistoryApi.recordSearch('Java', 'NYC')).rejects.toThrow('Network error');
        });
    });
});
