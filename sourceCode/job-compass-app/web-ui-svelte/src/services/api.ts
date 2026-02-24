import axios from 'axios';
import type { Job, ScrapeParams, SearchHistory } from '$types/job.types';

const API_BASE = '/api';

/**
 * Progress event received from SSE stream
 */
export interface ScrapeProgressEvent {
    scrapeId: string;
    status: 'STARTED' | 'SCRAPING' | 'SKILL_COMPLETED' | 'PUBLISHING' | 'COMPLETED' | 'FAILED';
    currentSkill: string;
    currentLocation: string;
    skillIndex: number;
    totalSkills: number;
    jobsFoundForSkill: number;
    totalJobsFound: number;
    duplicateJobsSkipped: number;
    errors: number;
    message: string;
    timestamp: string;
}

/**
 * API service for JobCompass backend
 */
export const jobApi = {
    /**
     * Fetch all jobs from the API
     */
    async getJobs(): Promise<Job[]> {
        const response = await axios.get<Job[]>(`${API_BASE}/jobs`);
        return response.data;
    },

    /**
     * Trigger a new scraping job. Returns the scrapeId for progress tracking.
     */
    async triggerScrape(params: ScrapeParams): Promise<string> {
        const skillsList = params.skills.split(',').map(s => s.trim()).filter(s => s.length > 0);

        const response = await axios.post<{ scrapeId: string }>(`${API_BASE}/scraper/trigger/multi-skill`, {
            skills: skillsList,
            location: params.location,
            maxResults: params.maxResults || 1000,
            maxJobAgeDays: params.maxJobAgeDays || 7,
            authentication: params.authentication
        });

        return response.data.scrapeId;
    },

    /**
     * Subscribe to SSE progress events for a scrape session.
     * Returns a cleanup function to close the connection.
     */
    subscribeScrapeProgress(
        scrapeId: string,
        onEvent: (event: ScrapeProgressEvent) => void,
        onError?: (error: Event) => void
    ): () => void {
        const eventSource = new EventSource(`${API_BASE}/scraper/progress/${scrapeId}`);

        eventSource.addEventListener('progress', (e: MessageEvent) => {
            try {
                const data: ScrapeProgressEvent = JSON.parse(e.data);
                onEvent(data);

                // Auto-close on terminal states
                if (data.status === 'COMPLETED' || data.status === 'FAILED') {
                    eventSource.close();
                }
            } catch (err) {
                console.error('Failed to parse SSE event:', err);
            }
        });

        eventSource.onerror = (e) => {
            if (onError) onError(e);
            eventSource.close();
        };

        return () => eventSource.close();
    },

    /**
     * Mark a job as applied
     */
    async markAsApplied(jobId: number): Promise<void> {
        await axios.post(`${API_BASE}/applications`, {
            jobId,
            userEmail: 'user@jobcompass.local',
            notes: 'Auto-tracked via UI click'
        });
    },

    /**
     * Dismiss a job as not relevant
     */
    async dismissJob(jobId: number): Promise<void> {
        await axios.post(`${API_BASE}/applications`, {
            jobId,
            userEmail: 'user@jobcompass.local',
            notes: 'Dismissed as not relevant',
            status: 'NOT_INTERESTED'
        });
    }
};

/**
 * API service for search history
 */
export const searchHistoryApi = {
    /**
     * Fetch the last 10 recent searches
     */
    async getRecentSearches(): Promise<SearchHistory[]> {
        const response = await axios.get<SearchHistory[]>(`${API_BASE}/search-history`);
        return response.data;
    },

    /**
     * Record a new search
     */
    async recordSearch(skill: string, location: string): Promise<SearchHistory> {
        const response = await axios.post<SearchHistory>(`${API_BASE}/search-history`, {
            skill,
            location
        });
        return response.data;
    }
};
