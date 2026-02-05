import { render, screen, fireEvent, waitFor } from '@testing-library/svelte';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ScrapeConfig from './ScrapeConfig.svelte';
import { jobApi } from '$services/api';

// Mock the API
vi.mock('$services/api', () => ({
    jobApi: {
        triggerScrape: vi.fn()
    }
}));

describe('ScrapeConfig', () => {
    beforeEach(() => {
        vi.clearAllMocks();
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

        // Use fireEvent.submit to bypass HTML5 'required' validation and test handler logic
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

        // Use fireEvent.submit to bypass HTML5 'required' validation for remaining empty fields
        const form = screen.getByRole('button', { name: /Trigger Scrape/i }).closest('form');
        await fireEvent.submit(form!);

        await waitFor(() => {
            expect(screen.getByText(/Location field is required/i)).toBeInTheDocument();
        });
    });

    it('should call API with correct params on submit', async () => {
        (jobApi.triggerScrape as any).mockResolvedValue(undefined);

        render(ScrapeConfig);

        const skillsInput = screen.getByLabelText(/Skills/i);
        const locationInput = screen.getByLabelText(/Location/i);
        const authInput = screen.getByLabelText(/LinkedIn Authentication/i);

        await fireEvent.input(skillsInput, { target: { value: 'Java Spring Boot' } });
        await fireEvent.input(locationInput, { target: { value: 'Germany' } });
        await fireEvent.input(authInput, { target: { value: 'dummy-cookie' } });

        const submitButton = screen.getByRole('button', { name: /Trigger Scrape/i });
        await fireEvent.click(submitButton); // Valid form, click works

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

    it('should show success message on successful submit', async () => {
        (jobApi.triggerScrape as any).mockResolvedValue(undefined);

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
            expect(screen.getByText(/Scrape triggered successfully/i)).toBeInTheDocument();
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
            new Promise(resolve => setTimeout(resolve, 100))
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

        // Set initial value
        await fireEvent.input(skillsInput, { target: { value: 'Java' } });

        // Mock select
        const selectSpy = vi.spyOn(skillsInput, 'select');

        // Focus
        await fireEvent.focus(skillsInput);

        expect(selectSpy).toHaveBeenCalled();
    });

    it('should use fixed maxResults of 1000', async () => {
        (jobApi.triggerScrape as any).mockResolvedValue(undefined);

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
});
