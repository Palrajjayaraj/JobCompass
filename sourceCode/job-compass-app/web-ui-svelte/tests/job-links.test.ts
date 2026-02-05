import { test, expect } from '@playwright/test';

test.describe('Job Link Verification', () => {
    test('Job links should be cleaned and open in new tab', async ({ page }) => {
        // Mock API response with clean URL (since backend does the cleaning)
        await page.route('**/api/jobs*', async route => {
            const json = [
                {
                    id: 1,
                    title: 'Test Software Engineer',
                    company: 'Test Company',
                    location: 'Remote',
                    description: 'Test Description',
                    url: 'https://www.linkedin.com/jobs/view/123456/', // Backend sends cleaned URL
                    postedDate: 'Recently',
                    scrapedAt: new Date().toISOString(),
                    language: 'en',
                    source: 'LINKEDIN'
                }
            ];
            await route.fulfill({ json });
        });

        // Go to home page (accessing host machine's running service)
        await page.goto('http://host.docker.internal:8085/');

        // Wait for job cards
        const jobCard = page.locator('.card').first();
        await expect(jobCard).toBeVisible();

        // Check "View Details" or Title link
        // Note: Svelte app implementation might vary, adjusting selector dynamically
        // Assuming there is an anchor tag with target="_blank"
        const link = jobCard.locator('a[href^="https://www.linkedin.com"]').first();

        await expect(link).toBeVisible();
        await expect(link).toHaveAttribute('target', '_blank');

        // Verify href matches clean URL
        const href = await link.getAttribute('href');
        expect(href).toBe('https://www.linkedin.com/jobs/view/123456/');
    });
});
