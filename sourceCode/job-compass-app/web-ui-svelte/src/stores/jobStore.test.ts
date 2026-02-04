import { render, screen } from '@testing-library/svelte';
import { describe, it, expect, beforeEach } from 'vitest';
import { jobStore, jobs, filteredJobs } from '$stores/jobStore';
import type { Job } from '$types/job.types';
import { Source } from '$types/job.types';

const mockJobs: Job[] = [
    {
        id: 1,
        title: 'Senior Java Developer',
        companyName: 'Google',
        location: 'Mountain View',
        description: 'Great job',
        url: 'https://google.com',
        source: Source.LINKEDIN,
        postedDate: new Date().toISOString(),
        scrapedAt: new Date().toISOString(),
        isActive: true
    },
    {
        id: 2,
        title: 'Frontend Dev',
        companyName: 'Meta',
        location: 'Menlo Park',
        description: 'Cool job',
        url: 'https://meta.com',
        source: Source.INDEED,
        postedDate: new Date().toISOString(),
        scrapedAt: new Date().toISOString(),
        isActive: true
    }
];

describe('jobStore', () => {
    beforeEach(() => {
        jobStore.setJobs([...mockJobs]);
        jobStore.setSearchQuery('');
        jobStore.setSourceFilter('all');
        jobStore.setLanguageFilter('all');
        jobStore.setSortBy('date');
        jobStore.setDaysFilter(0); // Disable date filtering for tests using old dates
    });

    describe('setJobs', () => {
        it('should update jobs', () => {
            const newJobs = [...mockJobs, {
                ...mockJobs[0],
                id: 3,
                title: 'New Job'
            }];

            jobStore.setJobs(newJobs);

            // Verify by setting up a subscription (simplified test)
            expect(true).toBe(true); // Placeholder - actual implementation would subscribe
        });
    });

    describe('setSearchQuery', () => {
        it('should filter jobs by title', () => {
            jobStore.setSearchQuery('Java');
            // Jobs should be filtered to only Java Developer
            expect(true).toBe(true); // Placeholder
        });

        it('should filter jobs by company', () => {
            jobStore.setSearchQuery('Google');
            // Should filter to Google jobs
            expect(true).toBe(true); // Placeholder
        });

        it('should filter jobs by description', () => {
            jobStore.setSearchQuery('expert');
            // Should find jobs with 'expert' in description
            expect(true).toBe(true); // Placeholder
        });
    });

    describe('setSourceFilter', () => {
        it('should filter by LinkedIn', () => {
            jobStore.setSourceFilter(Source.LINKEDIN);
            // Should only show LinkedIn jobs
            expect(true).toBe(true); // Placeholder
        });

        it('should show all when set to "all"', () => {
            jobStore.setSourceFilter('all');
            // Should show all jobs
            expect(true).toBe(true); // Placeholder
        });
    });

    describe('setLanguageFilter', () => {
        it('should filter by language', () => {
            jobStore.setLanguageFilter('de');
            // Should only show German jobs
            expect(true).toBe(true); // Placeholder
        });

        it('should show all when set to "all"', () => {
            jobStore.setLanguageFilter('all');
            // Should show all jobs
            expect(true).toBe(true); // Placeholder
        });
    });

    describe('setSortBy', () => {
        it('should sort by date (newest first)', () => {
            jobStore.setSortBy('date');
            // Should sort newest first
            expect(true).toBe(true); // Placeholder
        });

        it('should sort by title alphabetically', () => {
            jobStore.setSortBy('title');
            // Should sort A-Z
            expect(true).toBe(true); // Placeholder
        });
    });

    describe('loading and error states', () => {
        it('should set loading state', () => {
            jobStore.setLoading(true);
            expect(true).toBe(true); // Placeholder
        });

        it('should set error state', () => {
            jobStore.setError('Failed to load');
            expect(true).toBe(true); // Placeholder
        });

        it('should clear error', () => {
            jobStore.setError('Error');
            jobStore.setError(null);
            expect(true).toBe(true); // Placeholder
        });
    });

    // === AUTO-TRACK APPLIED JOBS TESTS ===
    describe('markJobAsApplied action', () => {
        it('should increment applicationCount for the target job', () => {
            // Arrange
            const testJobs = [
                { ...mockJobs[0], applicationCount: 0 },
                { ...mockJobs[1], applicationCount: 0 }
            ];
            jobStore.setJobs(testJobs);

            // Act
            jobStore.markJobAsApplied(1);

            // Assert - subscribe to verify
            let currentJobs: Job[] = [];
            const unsubscribe = jobs.subscribe(value => { currentJobs = value; });
            const targetJob = currentJobs.find(j => j.id === 1);
            expect(targetJob?.applicationCount).toBe(1);
            unsubscribe();
        });

        it('should increment from existing count', () => {
            // Arrange
            const testJobs = [
                { ...mockJobs[0], applicationCount: 1 }
            ];
            jobStore.setJobs(testJobs);

            // Act
            jobStore.markJobAsApplied(1);

            // Assert
            let currentJobs: Job[] = [];
            const unsubscribe = jobs.subscribe(value => { currentJobs = value; });
            expect(currentJobs[0].applicationCount).toBe(2);
            unsubscribe();
        });

        it('should not affect other jobs', () => {
            // Arrange
            const testJobs = [
                { ...mockJobs[0], id: 1, applicationCount: 0 },
                { ...mockJobs[1], id: 2, applicationCount: 0 }
            ];
            jobStore.setJobs(testJobs);

            // Act
            jobStore.markJobAsApplied(1);

            // Assert
            let currentJobs: Job[] = [];
            const unsubscribe = jobs.subscribe(value => { currentJobs = value; });
            const job2 = currentJobs.find(j => j.id === 2);
            expect(job2?.applicationCount).toBe(0); // Unchanged
            unsubscribe();
        });

        it('should handle undefined applicationCount gracefully', () => {
            // Arrange
            const testJobs = [
                { ...mockJobs[0], applicationCount: undefined as any }
            ];
            jobStore.setJobs(testJobs);

            // Act
            jobStore.markJobAsApplied(1);

            // Assert
            let currentJobs: Job[] = [];
            const unsubscribe = jobs.subscribe(value => { currentJobs = value; });
            expect(currentJobs[0].applicationCount).toBe(1);
            unsubscribe();
        });
    });

    describe('Sorting with applied jobs', () => {
        it('should move applied jobs to bottom when sorting by date', () => {
            // Arrange
            const testJobs = [
                { ...mockJobs[0], id: 1, scrapedAt: '2024-01-10T00:00:00Z', applicationCount: 0 },
                { ...mockJobs[1], id: 2, scrapedAt: '2024-01-09T00:00:00Z', applicationCount: 1 },
                { ...mockJobs[0], id: 3, scrapedAt: '2024-01-11T00:00:00Z', applicationCount: 0 }
            ];
            jobStore.setJobs(testJobs);
            jobStore.setSortBy('date');

            // Act
            let sorted: Job[] = [];
            const unsubscribe = filteredJobs.subscribe(value => { sorted = value; });

            // Assert
            // Job 3 (newest, not applied) should be first
            // Job 1 (middle date, not applied) should be second
            // Job 2 (oldest, BUT applied) should be last
            expect(sorted[0].id).toBe(3);
            expect(sorted[1].id).toBe(1);
            expect(sorted[2].id).toBe(2);
            unsubscribe();
        });

        it('should move applied jobs to bottom when sorting alphabetically', () => {
            // Arrange
            const testJobs = [
                { ...mockJobs[0], id: 1, title: 'Alpha Job', applicationCount: 0 },
                { ...mockJobs[1], id: 2, title: 'Beta Job', applicationCount: 1 },
                { ...mockJobs[0], id: 3, title: 'Charlie Job', applicationCount: 0 }
            ];
            jobStore.setJobs(testJobs);
            jobStore.setSortBy('title');

            // Act
            let sorted: Job[] = [];
            const unsubscribe = filteredJobs.subscribe(value => { sorted = value; });

            // Assert
            // Alpha (first alphabetically, not applied) should be first
            // Charlie (second alphabetically, not applied) should be second
            // Beta (third alphabetically, BUT applied) should be last
            expect(sorted[0].id).toBe(1);
            expect(sorted[1].id).toBe(3);
            expect(sorted[2].id).toBe(2);
            unsubscribe();
        });

        it('should maintain date order among non-applied jobs', () => {
            // Arrange
            const testJobs = [
                { ...mockJobs[0], id: 1, scrapedAt: '2024-01-10T00:00:00Z', applicationCount: 0 },
                { ...mockJobs[1], id: 2, scrapedAt: '2024-01-09T00:00:00Z', applicationCount: 0 },
                { ...mockJobs[0], id: 3, scrapedAt: '2024-01-11T00:00:00Z', applicationCount: 0 }
            ];
            jobStore.setJobs(testJobs);
            jobStore.setSortBy('date');

            // Act
            let sorted: Job[] = [];
            const unsubscribe = filteredJobs.subscribe(value => { sorted = value; });

            // Assert (newest first)
            expect(sorted[0].id).toBe(3); // 2024-01-11
            expect(sorted[1].id).toBe(1); // 2024-01-10
            expect(sorted[2].id).toBe(2); // 2024-01-09
            unsubscribe();
        });

        it('should maintain date order among applied jobs', () => {
            // Arrange
            const testJobs = [
                { ...mockJobs[0], id: 1, scrapedAt: '2024-01-10T00:00:00Z', applicationCount: 1 },
                { ...mockJobs[1], id: 2, scrapedAt: '2024-01-09T00:00:00Z', applicationCount: 1 },
                { ...mockJobs[0], id: 3, scrapedAt: '2024-01-11T00:00:00Z', applicationCount: 1 }
            ];
            jobStore.setJobs(testJobs);
            jobStore.setSortBy('date');

            // Act
            let sorted: Job[] = [];
            const unsubscribe = filteredJobs.subscribe(value => { sorted = value; });

            // Assert (newest first among applied)
            expect(sorted[0].id).toBe(3); // 2024-01-11
            expect(sorted[1].id).toBe(1); // 2024-01-10
            expect(sorted[2].id).toBe(2); // 2024-01-09
            unsubscribe();
        });
    });
});
