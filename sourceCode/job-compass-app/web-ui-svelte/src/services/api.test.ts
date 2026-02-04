import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import { jobApi } from './api';

// Mock axios
vi.mock('axios');
const mockedAxios = axios as any;

describe('jobApi.markAsApplied', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('should call POST /api/applications with correct payload', async () => {
        // Arrange
        const jobId = 123;
        mockedAxios.post.mockResolvedValue({ data: {} });

        // Act
        await jobApi.markAsApplied(jobId);

        // Assert
        expect(mockedAxios.post).toHaveBeenCalledTimes(1);
        expect(mockedAxios.post).toHaveBeenCalledWith('/api/applications', {
            jobId: 123,
            userEmail: 'user@jobcompass.local',
            notes: 'Auto-tracked via UI click'
        });
    });

    it('should throw error if API call fails', async () => {
        // Arrange
        mockedAxios.post.mockRejectedValue(new Error('Network error'));

        // Act & Assert
        await expect(jobApi.markAsApplied(456)).rejects.toThrow('Network error');
    });

    it('should not prevent duplicate calls (backend handles uniqueness)', async () => {
        // Arrange
        mockedAxios.post.mockResolvedValue({ data: {} });

        // Act
        await jobApi.markAsApplied(789);
        await jobApi.markAsApplied(789); // Same job

        // Assert
        expect(mockedAxios.post).toHaveBeenCalledTimes(2);
    });
});
