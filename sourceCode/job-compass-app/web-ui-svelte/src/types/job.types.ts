/**
 * TypeScript type definitions for JobCompass
 * Matches backend Java models
 */

export enum Source {
    LINKEDIN = 'LINKEDIN',
    GLASSDOOR = 'GLASSDOOR',
    INDEED = 'INDEED'
}

export interface Job {
    id: number;
    title: string;
    companyName: string;
    location: string;
    description?: string;
    url: string;
    source: Source;
    postedDate: string;
    scrapedAt: string;
    language?: string; // ISO 639-1 code: "en", "de", "fr", etc.
    salaryRange?: string;
    jobAgeDays?: number;
    isActive: boolean;
    applicationCount?: number;
}

export interface ScrapeParams {
    skills: string;
    location: string;
    maxResults?: number;
    maxJobAgeDays?: number;
    sources?: Source[];
    authentication?: {
        [key: string]: {
            [key: string]: string;
        };
    };
}

export interface ApiError {
    message: string;
    status: number;
}

export type SortBy = 'date' | 'title';
export type ViewMode = 'grid' | 'list';

export interface FilterState {
    searchQuery: string;
    selectedSource: Source | 'all';
    selectedLanguage: string | 'all';
    selectedCountry: string | 'all';
    sortBy: SortBy;
    daysFilter: number;
}

export interface SearchHistory {
    id: number;
    skill: string;
    location: string;
    searchedAt: string;
}
