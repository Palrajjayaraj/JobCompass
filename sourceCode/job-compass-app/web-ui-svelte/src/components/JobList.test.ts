import { tick } from 'svelte';
import { render, screen } from '@testing-library/svelte';
import { describe, it, expect, beforeEach } from 'vitest';
import JobList from './JobList.svelte';
import { jobs, jobStore } from '$stores/jobStore';
import type { Job } from '$types/job.types';
import { Source } from '$types/job.types';

const mockJobs: Job[] = [
    {
        id: 1,
        title: 'Java Developer',
        companyName: 'Google',
        location: 'Berlin',
        description: 'Java developer role',
        url: 'https://example.com/1',
        source: Source.LINKEDIN,
        postedDate: '2026-02-01',
        scrapedAt: new Date().toISOString(),
        language: 'en',
        isActive: true
    },
    {
        id: 2,
        title: 'Python Engineer',
        companyName: 'Facebook',
        location: 'Munich',
        description: 'Python engineer role',
        url: 'https://example.com/2',
        source: Source.GLASSDOOR,
        postedDate: '2026-02-02',
        scrapedAt: new Date().toISOString(),
        language: 'de',
        isActive: true
    },
    {
        id: 3,
        title: 'React Developer',
        companyName: 'Amazon',
        location: 'Hamburg',
        description: 'React developer role',
        url: 'https://example.com/3',
        source: Source.INDEED,
        postedDate: '2026-02-03',
        scrapedAt: new Date().toISOString(),
        language: 'en',
        isActive: true
    }
];

describe('JobList', () => {
    beforeEach(() => {
        jobs.set(mockJobs);
        jobStore.setSearchQuery('');
        jobStore.setSourceFilter('all');
        jobStore.setLanguageFilter('all');
        jobStore.setDaysFilter(0);
    });

    it('should render all jobs in grid mode', () => {
        render(JobList, { props: { viewMode: 'grid' } });

        expect(screen.getByText('Java Developer')).toBeInTheDocument();
        expect(screen.getByText('Python Engineer')).toBeInTheDocument();
        expect(screen.getByText('React Developer')).toBeInTheDocument();
    });

    it('should render all jobs in list mode', () => {
        render(JobList, { props: { viewMode: 'list' } });

        expect(screen.getByText('Java Developer')).toBeInTheDocument();
        expect(screen.getByText('Python Engineer')).toBeInTheDocument();
        expect(screen.getByText('React Developer')).toBeInTheDocument();
    });

    it('should show empty state when no jobs', () => {
        jobs.set([]);

        render(JobList, { props: { viewMode: 'grid' } });

        expect(screen.getByText(/No jobs found/i)).toBeInTheDocument();
        expect(screen.getByText(/Try adjusting your filters/i)).toBeInTheDocument();
    });

    it('should apply grid layout class in grid mode', () => {
        const { container } = render(JobList, { props: { viewMode: 'grid' } });

        const gridContainer = container.querySelector('.grid');
        expect(gridContainer).toBeTruthy();
    });

    it('should apply list layout class in list mode', () => {
        const { container } = render(JobList, { props: { viewMode: 'list' } });

        const listContainer = container.querySelector('.flex-col');
        expect(listContainer).toBeTruthy();
    });

    it('should show filtered jobs when search applied', async () => {
        render(JobList, { props: { viewMode: 'grid' } });

        jobStore.setSearchQuery('Java');
        await tick();

        expect(screen.getByText('Java Developer')).toBeInTheDocument();
        expect(screen.queryByText('Python Engineer')).not.toBeInTheDocument();
    });

    it('should update when jobs change', async () => {
        render(JobList, { props: { viewMode: 'grid' } });

        expect(screen.getByText('Java Developer')).toBeInTheDocument();

        // Update jobs
        jobs.set([mockJobs[0]]); // Only first job
        await tick();

        expect(screen.getByText('Java Developer')).toBeInTheDocument();
        expect(screen.queryByText('Python Engineer')).not.toBeInTheDocument();
    });
});
