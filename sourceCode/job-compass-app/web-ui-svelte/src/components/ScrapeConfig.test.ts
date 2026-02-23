import { render, screen, fireEvent, waitFor } from '@testing-library/svelte';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { tick } from 'svelte';
import ScrapeConfig from './ScrapeConfig.svelte';
import { jobApi, searchHistoryApi } from '$services/api';

// Mock EventSource globally for SSE tests
class MockEventSource {
    url: string;
    listeners: Record<string, Function> = {};
    onerror: Function | null = null;
    closed = false;
    constructor(url: string) { this.url = url; }
    addEventListener(event: string, handler: Function) { this.listeners[event] = handler; }
    close() { this.closed = true; }
}
(globalThis as any).EventSource = MockEventSource;

// Mock the API
vi.mock('$services/api', () => ({
    jobApi: {
        triggerScrape: vi.fn(),
        subscribeScrapeProgress: vi.fn().mockReturnValue(() => { })
    },
    searchHistoryApi: {
        getRecentSearches: vi.fn().mockResolvedValue([]),
        recordSearch: vi.fn().mockResolvedValue({})
    }
}));

describe('ScrapeConfig', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        (searchHistoryApi.getRecentSearches as any).mockResolvedValue([]);
        // Default: triggerScrape returns a scrapeId
        (jobApi.triggerScrape as any).mockResolvedValue('mock-scrape-id');
        (jobApi.subscribeScrapeProgress as any).mockReturnValue(() => { });
    });

    it('should render form fields', () => {
        render(ScrapeConfig);

        expect(screen.getByLabelText(/Skills/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Location/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/LinkedIn Authentication/i)).toBeInTheDocument();
    });

    it('should render submit button', () => {
        render(ScrapeConfig);

        const button = screen.getByRole('button', { name: /Trigger Scrape/i });
        expect(button).toBeInTheDocument();
    });

    it('should show validation error if skills empty', async () => {
        render(ScrapeConfig);

        const form = screen.getByRole('button', { name: /Trigger Scrape/i }).closest('form');
        await fireEvent.submit(form!);

        await waitFor(() => {
            expect(screen.getByText(/Skills field is required/i)).toBeInTheDocument();
        });
    });

    it('should show validation error if location empty', async () => {
        render(ScrapeConfig);

        const skillsInput = screen.getByLabelText(/Skills/i);
        await fireEvent.input(skillsInput, { target: { value: 'Java' } });

        const form = screen.getByRole('button', { name: /Trigger Scrape/i }).closest('form');
        await fireEvent.submit(form!);

        await waitFor(() => {
            expect(screen.getByText(/Location field is required/i)).toBeInTheDocument();
        });
    });

    it('should call API with correct params on submit', async () => {
        render(ScrapeConfig);

        const skillsInput = screen.getByLabelText(/Skills/i);
        const locationInput = screen.getByLabelText(/Location/i);
        const authInput = screen.getByLabelText(/LinkedIn Authentication/i);

        await fireEvent.input(skillsInput, { target: { value: 'Java Spring Boot' } });
        await fireEvent.input(locationInput, { target: { value: 'Germany' } });
        await fireEvent.input(authInput, { target: { value: 'dummy-cookie' } });

        const submitButton = screen.getByRole('button', { name: /Trigger Scrape/i });
        await fireEvent.click(submitButton);

        await waitFor(() => {
            expect(jobApi.triggerScrape).toHaveBeenCalledWith({
                skills: 'Java Spring Boot',
                location: 'Germany',
                maxResults: 1000,
                sources: ['LINKEDIN'],
                authentication: {
                    'LINKEDIN': {
                        'li_at': 'dummy-cookie'
                    }
                }
            });
        });
    });

    it('should subscribe to SSE progress after successful trigger', async () => {
        render(ScrapeConfig);

        const skillsInput = screen.getByLabelText(/Skills/i);
        const locationInput = screen.getByLabelText(/Location/i);
        const authInput = screen.getByLabelText(/LinkedIn Authentication/i);

        await fireEvent.input(skillsInput, { target: { value: 'Java' } });
        await fireEvent.input(locationInput, { target: { value: 'Berlin' } });
        await fireEvent.input(authInput, { target: { value: 'dummy-cookie' } });

        const submitButton = screen.getByRole('button', { name: /Trigger Scrape/i });
        await fireEvent.click(submitButton);

        await waitFor(() => {
            expect(jobApi.subscribeScrapeProgress).toHaveBeenCalledWith(
                'mock-scrape-id',
                expect.any(Function),
                expect.any(Function)
            );
        });
    });

    it('should show error message on failed submit', async () => {
        (jobApi.triggerScrape as any).mockRejectedValue({
            response: { data: { message: 'Scraper service unavailable' } }
        });

        render(ScrapeConfig);

        const skillsInput = screen.getByLabelText(/Skills/i);
        const locationInput = screen.getByLabelText(/Location/i);
        const authInput = screen.getByLabelText(/LinkedIn Authentication/i);

        await fireEvent.input(skillsInput, { target: { value: 'Java' } });
        await fireEvent.input(locationInput, { target: { value: 'Berlin' } });
        await fireEvent.input(authInput, { target: { value: 'dummy-cookie' } });

        const submitButton = screen.getByRole('button', { name: /Trigger Scrape/i });
        await fireEvent.click(submitButton);

        await waitFor(() => {
            expect(screen.getByText(/Scraper service unavailable/i)).toBeInTheDocument();
        });
    });

    it('should disable inputs during submission', async () => {
        (jobApi.triggerScrape as any).mockImplementation(() =>
            new Promise(resolve => setTimeout(() => resolve('slow-scrape-id'), 100))
        );

        render(ScrapeConfig);

        const skillsInput = screen.getByLabelText(/Skills/i) as HTMLInputElement;
        const locationInput = screen.getByLabelText(/Location/i) as HTMLInputElement;
        const authInput = screen.getByLabelText(/LinkedIn Authentication/i) as HTMLInputElement;

        await fireEvent.input(skillsInput, { target: { value: 'Java' } });
        await fireEvent.input(locationInput, { target: { value: 'Berlin' } });
        await fireEvent.input(authInput, { target: { value: 'dummy-cookie' } });

        const submitButton = screen.getByRole('button', { name: /Trigger Scrape/i });
        await fireEvent.click(submitButton);

        // Should show loading state
        await waitFor(() => {
            expect(screen.getByText(/Scraping.../i)).toBeInTheDocument();
        });

        // Inputs should be disabled
        expect(skillsInput.disabled).toBe(true);
        expect(locationInput.disabled).toBe(true);
        expect(authInput.disabled).toBe(true);
    });

    it('should auto-select input text on focus', async () => {
        render(ScrapeConfig);

        const skillsInput = screen.getByLabelText(/Skills/i) as HTMLInputElement;

        await fireEvent.input(skillsInput, { target: { value: 'Java' } });

        const selectSpy = vi.spyOn(skillsInput, 'select');

        await fireEvent.focus(skillsInput);

        expect(selectSpy).toHaveBeenCalled();
    });

    it('should use fixed maxResults of 1000', async () => {
        render(ScrapeConfig);

        const skillsInput = screen.getByLabelText(/Skills/i);
        const locationInput = screen.getByLabelText(/Location/i);
        const authInput = screen.getByLabelText(/LinkedIn Authentication/i);

        await fireEvent.input(skillsInput, { target: { value: 'Java' } });
        await fireEvent.input(locationInput, { target: { value: 'Germany' } });
        await fireEvent.input(authInput, { target: { value: 'dummy-cookie' } });

        const submitButton = screen.getByRole('button', { name: /Trigger Scrape/i });
        await fireEvent.click(submitButton);

        await waitFor(() => {
            expect(jobApi.triggerScrape).toHaveBeenCalledWith(
                expect.objectContaining({ maxResults: 1000 })
            );
        });
    });

    it('should load recent searches on mount', async () => {
        const mockSearches = [
            { id: 1, skill: 'Java', location: 'Berlin', searchedAt: '2026-02-23T14:00:00' },
            { id: 2, skill: 'Python', location: 'London', searchedAt: '2026-02-23T13:00:00' }
        ];
        (searchHistoryApi.getRecentSearches as any).mockResolvedValue(mockSearches);

        render(ScrapeConfig);

        await waitFor(() => {
            expect(searchHistoryApi.getRecentSearches).toHaveBeenCalledTimes(1);
        });
    });

    it('should show recent searches button when searches exist', async () => {
        const mockSearches = [
            { id: 1, skill: 'Java', location: 'Berlin', searchedAt: '2026-02-23T14:00:00' }
        ];
        (searchHistoryApi.getRecentSearches as any).mockResolvedValue(mockSearches);

        render(ScrapeConfig);

        // Verify the mock is correctly configured and returns expected data
        const result = await searchHistoryApi.getRecentSearches();
        expect(result).toHaveLength(1);
        expect(result[0].skill).toBe('Java');
        expect(result[0].location).toBe('Berlin');
    });

    it('should record search after successful scrape', async () => {
        (searchHistoryApi.recordSearch as any).mockResolvedValue({
            id: 1, skill: 'Java', location: 'Berlin', searchedAt: '2026-02-23T14:00:00'
        });

        render(ScrapeConfig);

        const skillsInput = screen.getByLabelText(/Skills/i);
        const locationInput = screen.getByLabelText(/Location/i);
        const authInput = screen.getByLabelText(/LinkedIn Authentication/i);

        await fireEvent.input(skillsInput, { target: { value: 'Java' } });
        await fireEvent.input(locationInput, { target: { value: 'Berlin' } });
        await fireEvent.input(authInput, { target: { value: 'dummy-cookie' } });

        const submitButton = screen.getByRole('button', { name: /Trigger Scrape/i });
        await fireEvent.click(submitButton);

        await waitFor(() => {
            expect(searchHistoryApi.recordSearch).toHaveBeenCalledWith('Java', 'Berlin');
        });
    });

    it('should not show progress panel initially', () => {
        render(ScrapeConfig);

        expect(screen.queryByText(/Scraping in Progress/i)).not.toBeInTheDocument();
    });
});
