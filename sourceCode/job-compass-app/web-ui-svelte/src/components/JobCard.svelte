<script lang="ts">
  import {
    formatRelativeTime,
    formatSource,
    getLanguageName,
    getLanguageBadgeColor,
    getSourceBadgeColor,
  } from "../utils/helpers";
  import type { Job, ViewMode } from "../types/job.types";
  import { jobApi } from "$services/api";
  import { jobStore } from "$stores/jobStore";

  export let job: Job;
  export let viewMode: ViewMode = "grid";

  async function handleJobClick(event: MouseEvent) {
    // Don't prevent default - let window.open happen
    // But fire tracking in parallel if not already applied
    if ((job.applicationCount || 0) === 0) {
      try {
        await jobApi.markAsApplied(job.id);
        jobStore.markJobAsApplied(job.id);
      } catch (err) {
        console.error("Failed to track application:", err);
        // Non-blocking - user still sees job
      }
    }
  }

  async function handleDismiss(event: MouseEvent) {
    event.preventDefault();
    event.stopPropagation();

    try {
      await jobApi.dismissJob(job.id);
      jobStore.dismissJob(job.id);
    } catch (err) {
      console.error("Failed to dismiss job:", err);
    }
  }

  $: relativeTime = formatRelativeTime(job.scrapedAt);
  $: postedTime = formatRelativeTime(job.postedDate);
  $: languageName = getLanguageName(job.language);
  $: languageBadgeColor = getLanguageBadgeColor(job.language);
  $: sourceBadgeColor = getSourceBadgeColor(job.source);
  $: sourceName = formatSource(job.source);
  $: isUnknownLanguage =
    !job.language || job.language.toLowerCase() === "unknown";

  // Fallbacks for core fields
  $: title = job.title || "Unknown Title";
  $: company = job.companyName || "Unknown Company";
  $: location = job.location || "Unknown Location";
</script>

<div
  class="card bg-base-200 shadow-xl hover:shadow-2xl transition-all duration-200 group {viewMode ===
  'list'
    ? 'flex-row items-center p-3'
    : ''}"
>
  {#if viewMode === "list"}
    <!-- Compact List View -->
    <div class="flex-1 min-w-0 pr-4">
      <div class="flex items-center gap-2">
        <div class="flex items-baseline gap-2 text-sm flex-1 min-w-0">
          <a
            href={job.url}
            target="_blank"
            rel="noopener noreferrer"
            class="font-bold text-white hover:text-primary truncate max-w-[40%]"
            on:click={handleJobClick}
          >
            {title}
          </a>
          <span class="text-gray-400 truncate">• {company}</span>
          <span class="text-gray-500 text-xs truncate">• {location}</span>
        </div>
        <button
          on:click={handleDismiss}
          class="btn btn-ghost btn-xs btn-circle opacity-0 group-hover:opacity-100 transition-opacity"
          title="Dismiss this job"
          aria-label="Dismiss job"
        >
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
            />
          </svg>
        </button>
      </div>
      <div class="flex flex-wrap gap-2 mt-1">
        {#if (job.applicationCount || 0) > 0}
          <span class="badge badge-success badge-xs gap-1" title="Applied">
            <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
              <path
                d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
              />
            </svg>
            Applied
          </span>
        {/if}
        {#if !isUnknownLanguage}
          <span
            class="badge {languageBadgeColor} badge-xs"
            title="Language: {languageName}">{languageName}</span
          >
        {/if}
        <span
          class="badge {sourceBadgeColor} badge-xs"
          title="Source: {sourceName}">{sourceName}</span
        >
        <span
          class="badge badge-info badge-xs text-xs"
          title="Posted: {postedTime}">📅 {postedTime}</span
        >
        <span
          class="badge badge-outline badge-xs text-xs"
          title="Scraped: {relativeTime}">🕒 {relativeTime}</span
        >
      </div>
    </div>
    <div>
      <a
        href={job.url}
        target="_blank"
        rel="noopener noreferrer"
        class="btn btn-ghost btn-sm btn-circle"
        title="View Job"
        on:click={handleJobClick}
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
          />
        </svg>
      </a>
    </div>
  {:else}
    <!-- Grid View -->
    <div class="card-body p-6">
      <div class="flex justify-between items-start mb-3">
        <h2
          class="card-title text-xl font-bold text-white group-hover:text-primary transition-colors flex-1 line-clamp-2 min-h-[3.5rem]"
        >
          <a
            href={job.url}
            target="_blank"
            rel="noopener noreferrer"
            class="hover:underline"
            on:click={handleJobClick}
          >
            {title}
          </a>
        </h2>
        <button
          on:click={handleDismiss}
          class="btn btn-ghost btn-xs btn-circle opacity-0 group-hover:opacity-100 transition-opacity"
          title="Dismiss this job"
          aria-label="Dismiss job"
        >
          <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
            />
          </svg>
        </button>
      </div>

      <div class="space-y-1 mb-4">
        <p class="text-lg font-semibold text-gray-300">{company}</p>
        <p class="text-sm text-gray-400">📍 {location}</p>
      </div>

      <div class="flex flex-wrap gap-2 mb-3">
        {#if (job.applicationCount || 0) > 0}
          <span class="badge badge-success badge-sm gap-1" title="Applied">
            <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path
                d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
              />
            </svg>
            Applied
          </span>
        {/if}
        {#if !isUnknownLanguage}
          <span
            class="badge {languageBadgeColor} badge-sm"
            title="Language: {languageName}">{languageName}</span
          >
        {/if}
        <span
          class="badge {sourceBadgeColor} badge-sm"
          title="Source: {sourceName}">{sourceName}</span
        >
        <span class="badge badge-info badge-sm" title="Posted: {postedTime}"
          >📅 {postedTime}</span
        >
        <span
          class="badge badge-outline badge-sm"
          title="Scraped: {relativeTime}">🕒 {relativeTime}</span
        >
      </div>

      <div class="card-actions justify-end mt-2">
        <a
          href={job.url}
          target="_blank"
          rel="noopener noreferrer"
          class="btn btn-primary btn-sm"
          on:click={handleJobClick}
        >
          View Job →
        </a>
      </div>
    </div>
  {/if}
</div>
