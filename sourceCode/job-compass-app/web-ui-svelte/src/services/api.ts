import axios from 'axios';
import type { Job, ScrapeParams } from '$types/job.types';

const API_BASE = '/api';

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
     * Trigger a new scraping job
     */
    async triggerScrape(params: ScrapeParams): Promise<void> {
        // Convert comma-separated string to array and trim
        const skillsList = params.skills.split(',').map(s => s.trim()).filter(s => s.length > 0);

        await axios.post(`${API_BASE}/scraper/trigger/multi-skill`, {
            skills: skillsList,
            location: params.location,
            maxResults: params.maxResults || 1000,
            maxJobAgeDays: params.maxJobAgeDays || 7,
            authentication: params.authentication
        });
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
