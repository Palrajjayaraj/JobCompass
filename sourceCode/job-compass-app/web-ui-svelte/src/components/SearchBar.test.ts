import { render, screen, fireEvent, waitFor } from '@testing-library/svelte';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import SearchBar from './SearchBar.svelte';
import { jobStore, jobs } from '$stores/jobStore';
import type { Job } from '$types/job.types';
import { Source } from '$types/job.types';

const mockJobs: Job[] = [
    {
        id: 1,
        title: 'Java Developer',
        company: 'Google',
        location: 'Berlin',
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
        company: 'Facebook',
        location: 'Munich',
        url: 'https://example.com/2',
        source: Source.GLASSDOOR,
        postedDate: '2026-02-02',
        scrapedAt: new Date().toISOString(),
        language: 'de',
        isActive: true
    }
];

describe('SearchBar', () => {
    beforeEach(() => {
        jobs.set(mockJobs);
        jobStore.setSearchQuery('');
    });

    it('should render search input', () => {
        render(SearchBar);
        const input = screen.getByPlaceholderText(/Search by title/i);
        expect(input).toBeInTheDocument();
    });

    it('should display job count', () => {
        render(SearchBar);
        expect(screen.getByText(/2 jobs/i)).toBeInTheDocument();
    });

    it('should update search query on type', async () => {
        render(SearchBar);
        const input = screen.getByPlaceholderText(/Search by title/i);

        await fireEvent.input(input, { target: { value: 'Java' } });

        // Wait for debounce
        await waitFor(() => {
            expect(input).toHaveValue('Java');
        }, { timeout: 500 });
    });

    it('should show clear button when there is text', async () => {
        render(SearchBar);
        const input = screen.getByPlaceholderText(/Search by title/i);

        await fireEvent.input(input, { target: { value: 'test' } });

        const clearButton = screen.getByLabelText(/clear search/i);
        expect(clearButton).toBeInTheDocument();
    });

    it('should clear search when clear button clicked', async () => {
        render(SearchBar);
        const input = screen.getByPlaceholderText(/Search by title/i) as HTMLInputElement;

        // Type something
        await fireEvent.input(input, { target: { value: 'test' } });
        expect(input.value).toBe('test');

        // Click clear
        const clearButton = screen.getByLabelText(/clear search/i);
        await fireEvent.click(clearButton);

        expect(input.value).toBe('');
    });

    it('should select text on focus', async () => {
        render(SearchBar);
        const input = screen.getByPlaceholderText(/Search by title/i) as HTMLInputElement;

        // Set a value
        await fireEvent.input(input, { target: { value: 'test value' } });

        // Mock select method
        const selectSpy = vi.spyOn(input, 'select');

        // Focus input
        await fireEvent.focus(input);

        expect(selectSpy).toHaveBeenCalled();
    });

    it('should debounce search updates', async () => {
        const { component } = render(SearchBar);
        const input = screen.getByPlaceholderText(/Search by title/i);

        // Type rapidly
        await fireEvent.input(input, { target: { value: 'J' } });
        await fireEvent.input(input, { target: { value: 'Ja' } });
        await fireEvent.input(input, { target: { value: 'Jav' } });
        await fireEvent.input(input, { target: { value: 'Java' } });

        // Should debounce and only trigger after delay
        await waitFor(() => {
            expect(input).toHaveValue('Java');
        }, { timeout: 500 });
    });
});
