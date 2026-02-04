import { render, screen, fireEvent } from '@testing-library/svelte';
import { describe, it, expect } from 'vitest';
import FilterDropdown from './FilterDropdown.svelte';

const mockOptions = [
    { value: 'all', label: 'All' },
    { value: 'en', label: 'English' },
    { value: 'de', label: 'German' },
];

describe('FilterDropdown', () => {
    it('should render label', () => {
        render(FilterDropdown, {
            props: {
                label: 'Language',
                value: 'all',
                options: mockOptions,
            }
        });

        expect(screen.getByText('Language')).toBeInTheDocument();
    });

    it('should render all options', () => {
        const { container } = render(FilterDropdown, {
            props: {
                label: 'Language',
                value: 'all',
                options: mockOptions,
            }
        });

        const options = container.querySelectorAll('option');
        expect(options).toHaveLength(3);
        expect(options[0].textContent).toBe('All');
        expect(options[1].textContent).toBe('English');
        expect(options[2].textContent).toBe('German');
    });

    it('should select correct initial value', () => {
        render(FilterDropdown, {
            props: {
                label: 'Language',
                value: 'de',
                options: mockOptions,
            }
        });

        const select = screen.getByRole('combobox') as HTMLSelectElement;
        expect(select.value).toBe('de');
    });

    it('should update value on change', async () => {
        const { component } = render(FilterDropdown, {
            props: {
                label: 'Language',
                value: 'all',
                options: mockOptions,
            }
        });

        const select = screen.getByRole('combobox') as HTMLSelectElement;
        await fireEvent.change(select, { target: { value: 'en' } });

        expect(select.value).toBe('en');
    });
});
