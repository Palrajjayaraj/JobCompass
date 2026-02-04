/**
 * Format a date string to relative time (e.g., "2 days ago")
 */
export function formatRelativeTime(dateString: string): string {
    if (!dateString) return 'Unknown date';

    // Fix for backend timestamps without timezone (assume UTC)
    // If it has 'T' (time component) but no timezone char (Z, +, -) at the end
    let processedDateString = dateString;
    if (dateString.includes('T') && !dateString.endsWith('Z') && !dateString.includes('+') && !/(-\d{2}:\d{2}|-\d{4})$/.test(dateString)) {
        processedDateString += 'Z';
    }

    const date = new Date(processedDateString);
    if (isNaN(date.getTime())) return 'Unknown date';

    const now = new Date();
    const diffInSeconds = (now.getTime() - date.getTime()) / 1000;

    if (diffInSeconds < 60) return 'Just now';

    const intervals: Record<string, number> = {
        year: 31536000,
        month: 2592000,
        week: 604800,
        day: 86400,
        hour: 3600,
        minute: 60
    };

    for (const [unit, secondsInUnit] of Object.entries(intervals)) {
        if (Math.abs(diffInSeconds) >= secondsInUnit) {
            const value = Math.floor(diffInSeconds / secondsInUnit);
            return new Intl.RelativeTimeFormat('en', { numeric: 'auto' }).format(-value, unit as Intl.RelativeTimeFormatUnit);
        }
    }

    return 'Just now';
}

/**
 * Get language display name from code
 */
export function getLanguageName(code: string | undefined): string {
    const languages: Record<string, string> = {
        en: 'English',
        de: 'German',
        fr: 'French',
        es: 'Spanish',
        it: 'Italian',
        nl: 'Dutch',
        pt: 'Portuguese',
        unknown: 'Unknown'
    };

    return languages[code || 'unknown'] || code || 'Unknown';
}

/**
 * Get badge color for language
 */
export function getLanguageBadgeColor(code: string | undefined): string {
    const colors: Record<string, string> = {
        en: 'badge-primary',
        de: 'badge-secondary',
        fr: 'badge-accent',
        es: 'badge-info',
        it: 'badge-success',
        nl: 'badge-warning',
        pt: 'badge-error',
        unknown: 'badge-ghost'
    };

    return colors[code || 'unknown'] || 'badge-ghost';
}

/**
 * Get badge color for source
 */
export function getSourceBadgeColor(source: string): string {
    const colors: Record<string, string> = {
        LINKEDIN: 'badge-info',
        GLASSDOOR: 'badge-success',
        INDEED: 'badge-warning'
    };

    return colors[source] || 'badge-neutral';
}

/**
 * Format source for display
 */
export function formatSource(source: string | undefined): string {
    if (!source) return 'Unknown';

    const normalized = source.toLowerCase();
    if (normalized === 'unknown' || normalized === 'undefined' || normalized === 'null') {
        return 'Unknown';
    }

    if (source === 'LINKEDIN') return 'LinkedIn'; // Exact match
    if (normalized === 'linkedin') return 'LinkedIn';
    if (normalized === 'glassdoor') return 'Glassdoor';
    if (normalized === 'indeed') return 'Indeed';

    return normalized.charAt(0).toUpperCase() + normalized.slice(1);
}

/**
 * Debounce function for search input
 */
export function debounce<T extends (...args: any[]) => any>(
    func: T,
    wait: number
): (...args: Parameters<T>) => void {
    let timeout: ReturnType<typeof setTimeout>;

    return function (...args: Parameters<T>) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func(...args), wait);
    };
}
