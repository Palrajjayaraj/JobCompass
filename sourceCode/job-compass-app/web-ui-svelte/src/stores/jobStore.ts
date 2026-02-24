import { writable, derived } from 'svelte/store';
import type { Job, FilterState, SortBy } from '$types/job.types';

// Jobs data store
export const jobs = writable<Job[]>([]);
export const loading = writable<boolean>(false);
export const error = writable<string | null>(null);

// Filter state store
export const filters = writable<FilterState>({
    searchQuery: '',
    selectedSource: 'all',
    selectedLanguage: 'all',
    selectedCountry: 'all',
    sortBy: 'date',
    daysFilter: 30
});

// Derived store for filtered and sorted jobs
export const filteredJobs = derived(
    [jobs, filters],
    ([$jobs, $filters]) => {
        let result = [...$jobs];

        // Filter by search query
        if ($filters.searchQuery) {
            const query = $filters.searchQuery.toLowerCase();
            result = result.filter(job =>
                (job.title || '').toLowerCase().includes(query) ||
                (job.companyName || '').toLowerCase().includes(query) ||
                (job.description || '').toLowerCase().includes(query)
            );
        }

        // Filter by source
        if ($filters.selectedSource !== 'all') {
            result = result.filter(job => job.source === $filters.selectedSource);
        }

        // Filter by language
        if ($filters.selectedLanguage && $filters.selectedLanguage !== 'all') {
            result = result.filter(job => job.language === $filters.selectedLanguage);
        }

        // Filter by country
        if ($filters.selectedCountry && $filters.selectedCountry !== 'all') {
            const cFilter = $filters.selectedCountry.toLowerCase();
            result = result.filter(job => {
                if (!job.location) return false;
                const parts = job.location.split(',').map(p => p.trim());
                const country = parts[parts.length - 1].toLowerCase();
                return country.includes(cFilter) || job.location.toLowerCase().includes(cFilter);
            });
        }

        // Filter by days
        if ($filters.daysFilter > 0) {
            const cutoffDate = new Date();
            cutoffDate.setDate(cutoffDate.getDate() - $filters.daysFilter);
            result = result.filter(job => new Date(job.scrapedAt) >= cutoffDate);
        }

        // Sort (applied jobs go to bottom)
        if ($filters.sortBy === 'date') {
            result.sort((a, b) => {
                // Applied jobs go to bottom
                const aApplied = (a.applicationCount || 0) > 0;
                const bApplied = (b.applicationCount || 0) > 0;
                if (aApplied && !bApplied) return 1;
                if (!aApplied && bApplied) return -1;

                // Then sort by date
                return new Date(b.scrapedAt).getTime() - new Date(a.scrapedAt).getTime();
            });
        } else {
            result.sort((a, b) => {
                // Applied jobs go to bottom
                const aApplied = (a.applicationCount || 0) > 0;
                const bApplied = (b.applicationCount || 0) > 0;
                if (aApplied && !bApplied) return 1;
                if (!aApplied && bApplied) return -1;

                // Then sort alphabetically
                return a.title.localeCompare(b.title);
            });
        }

        return result;
    }
);

// Actions
export const jobStore = {
    setJobs(newJobs: Job[]) {
        jobs.set(newJobs);
    },

    setLoading(isLoading: boolean) {
        loading.set(isLoading);
    },

    setError(err: string | null) {
        error.set(err);
    },

    setSearchQuery(query: string) {
        filters.update(f => ({ ...f, searchQuery: query }));
    },

    setSourceFilter(source: FilterState['selectedSource']) {
        filters.update(f => ({ ...f, selectedSource: source }));
    },

    setLanguageFilter(language: string) {
        filters.update(f => ({ ...f, selectedLanguage: language }));
    },

    setCountryFilter(country: string) {
        filters.update(f => ({ ...f, selectedCountry: country }));
    },

    setSortBy(sortBy: SortBy) {
        filters.update(f => ({ ...f, sortBy }));
    },

    setDaysFilter(days: number) {
        filters.update(f => ({ ...f, daysFilter: days }));
    },

    markJobAsApplied(jobId: number) {
        jobs.update(currentJobs =>
            currentJobs.map(job =>
                job.id === jobId
                    ? { ...job, applicationCount: (job.applicationCount || 0) + 1 }
                    : job
            )
        );
    },

    dismissJob(jobId: number) {
        jobs.update(currentJobs =>
            currentJobs.filter(job => job.id !== jobId)
        );
    }
};
