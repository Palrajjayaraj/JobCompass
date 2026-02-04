import { describe, it, expect } from 'vitest';
import { formatSource, formatRelativeTime } from './helpers';

describe('formatSource', () => {
    it('formats known sources correctly', () => {
        expect(formatSource('LINKEDIN')).toBe('LinkedIn');
        expect(formatSource('GLASSDOOR')).toBe('Glassdoor');
        expect(formatSource('INDEED')).toBe('Indeed');
    });

    it('formats lowercase sources correctly', () => {
        expect(formatSource('linkedin')).toBe('LinkedIn');
        expect(formatSource('glassdoor')).toBe('Glassdoor');
    });

    it('capitalizes other sources', () => {
        expect(formatSource('SOME_OTHER_SOURCE')).toBe('Some_other_source');
        expect(formatSource('google')).toBe('Google');
    });

    it('handles undefined/null/empty', () => {
        expect(formatSource(undefined)).toBe('Unknown');
        expect(formatSource(null as any)).toBe('Unknown');
        expect(formatSource('')).toBe('Unknown');
    });

    it('handles string "undefined" or "null"', () => {
        expect(formatSource('undefined')).toBe('Unknown');
        expect(formatSource('null')).toBe('Unknown');
        expect(formatSource('UNKNOWN')).toBe('Unknown');
    });
});

describe('formatRelativeTime', () => {
    it('formats recent time correctly', () => {
        const now = new Date();
        expect(formatRelativeTime(now.toISOString())).toBe('Just now');
    });

    it('handles invalid dates', () => {
        expect(formatRelativeTime('invalid-date')).toBe('Unknown date');
        expect(formatRelativeTime(null as any)).toBe('Unknown date');
        expect(formatRelativeTime(undefined as any)).toBe('Unknown date');
    });

    it('treats backend timestamps (no Z) as UTC', () => {
        // Mock "now" to be 2026-02-04T12:00:00Z
        const mockNow = new Date('2026-02-04T12:00:00Z');
        vi.useFakeTimers();
        vi.setSystemTime(mockNow);

        try {
            // Input: 11:00 (no Z). 
            // If treated as local (browser timezone), it depends on env.
            // But our fix interprets it as UTC 11:00.
            // Diff should be exactly 1 hour.
            const backendTimestamp = '2026-02-04T11:00:00';
            expect(formatRelativeTime(backendTimestamp)).toBe('1 hour ago');

            // Input: 11:59 (no Z) -> 1 minute ago
            const recentTimestamp = '2026-02-04T11:59:00';
            expect(formatRelativeTime(recentTimestamp)).toBe('1 minute ago');
        } finally {
            vi.useRealTimers();
        }
    });
});
