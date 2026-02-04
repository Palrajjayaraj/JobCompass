import { render, screen } from '@testing-library/svelte';
import { describe, it, expect } from 'vitest';
import JobCard from './JobCard.svelte';
import type { Job } from '$types/job.types';
import { Source } from '$types/job.types';

const mockJob: Job = {
    id: 1,
    title: 'Senior Java Developer',
    companyName: 'Google',
    location: 'Berlin, Germany',
    description: 'We are looking for an experienced Java developer...',
    url: 'https://example.com/job/1',
    source: Source.LINKEDIN,
    postedDate: '2026-02-01',
    scrapedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(), // 2 days ago
    language: 'de',
    isActive: true
};

describe('JobCard', () => {
    it('should render job title', () => {
        render(JobCard, { props: { job: mockJob } });
        expect(screen.getByText('Senior Java Developer')).toBeInTheDocument();
    });

    it('should render company name', () => {
        render(JobCard, { props: { job: mockJob } });
        expect(screen.getByText('Google')).toBeInTheDocument();
    });

    it('should render location', () => {
        render(JobCard, { props: { job: mockJob } });
        expect(screen.getByText(/Berlin, Germany/)).toBeInTheDocument();
    });

    it('should display language badge with correct text', () => {
        render(JobCard, { props: { job: mockJob } });
        expect(screen.getByText('German')).toBeInTheDocument();
    });

    it('should display source badge', () => {
        render(JobCard, { props: { job: mockJob } });
        // Component formats source to Title Case (e.g. LinkedIn)
        expect(screen.getByText('LinkedIn')).toBeInTheDocument();
    });

    it('should format posted date as relative time', () => {
        render(JobCard, { props: { job: mockJob } });
        // Target specifically the Posted badge via title attribute or loose text match
        // There are two time badges (Posted and Scraped), so we need to be specific
        const postedBadge = screen.getByTitle(/Posted:/);
        expect(postedBadge).toBeInTheDocument();
        expect(postedBadge).toHaveTextContent(/days ago/);
    });

    it('should handle missing language gracefully', () => {
        const jobWithoutLang = { ...mockJob, language: undefined };
        render(JobCard, { props: { job: jobWithoutLang } });
        // Should not render any language badge
        expect(screen.queryByText('German')).not.toBeInTheDocument();
    });

    it('should handle missing description gracefully', () => {
        const jobWithoutDesc = { ...mockJob, description: undefined };
        render(JobCard, { props: { job: jobWithoutDesc } });
        // Should still render the card
        expect(screen.getByText('Senior Java Developer')).toBeInTheDocument();
    });

    it('should render job URL as a link', () => {
        render(JobCard, { props: { job: mockJob } });
        // Target the link by its accessible name (the job title)
        const link = screen.getByRole('link', { name: 'Senior Java Developer' });
        expect(link).toHaveAttribute('href', 'https://example.com/job/1');
        expect(link).toHaveAttribute('target', '_blank');
    });

    it('should apply different badge colors for different languages', () => {
        const { container: container1 } = render(JobCard, { props: { job: { ...mockJob, language: 'en' } } });
        const englishBadge = container1.querySelector('.badge');

        const { container: container2 } = render(JobCard, { props: { job: { ...mockJob, language: 'de' } } });
        const germanBadge = container2.querySelector('.badge');

        // They should have different classes
        expect(englishBadge?.className).not.toEqual(germanBadge?.className);
    });
});
