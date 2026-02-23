<script lang="ts">
  import { jobApi, searchHistoryApi } from "$services/api";
  import type { ScrapeProgressEvent } from "$services/api";
  import type { ScrapeParams, SearchHistory } from "$types/job.types";
  import { Source } from "$types/job.types";
  import { onMount, onDestroy } from "svelte";

  let skills = "";
  let location = "";
  let linkedinLiAt = "";
  let loading = false;
  let error = "";
  let success = "";
  let recentSearches: SearchHistory[] = [];
  let showRecentSearches = false;

  // Progress tracking
  let progress: ScrapeProgressEvent | null = null;
  let cleanupSSE: (() => void) | null = null;

  onMount(async () => {
    await loadRecentSearches();
  });

  onDestroy(() => {
    if (cleanupSSE) cleanupSSE();
  });

  async function loadRecentSearches() {
    try {
      recentSearches = await searchHistoryApi.getRecentSearches();
    } catch (err) {
      console.error("Failed to load recent searches:", err);
    }
  }

  function selectRecentSearch(search: SearchHistory) {
    skills = search.skill;
    location = search.location;
    showRecentSearches = false;
  }

  function getProgressPercent(): number {
    if (!progress) return 0;
    if (progress.status === "COMPLETED") return 100;
    if (progress.status === "FAILED") return 100;
    if (progress.totalSkills === 0) return 0;
    let base = ((progress.skillIndex - 1) / progress.totalSkills) * 100;
    if (progress.status === "SKILL_COMPLETED") {
      base = (progress.skillIndex / progress.totalSkills) * 100;
    }
    if (progress.status === "PUBLISHING") return 95;
    return Math.min(base + 5, 95);
  }

  function getStatusIcon(): string {
    if (!progress) return "";
    switch (progress.status) {
      case "STARTED":
        return "🚀";
      case "SCRAPING":
        return "🔍";
      case "SKILL_COMPLETED":
        return "✅";
      case "PUBLISHING":
        return "📤";
      case "COMPLETED":
        return "🎉";
      case "FAILED":
        return "❌";
      default:
        return "⏳";
    }
  }

  async function handleSubmit(event: Event) {
    event.preventDefault();

    error = "";
    success = "";
    progress = null;

    if (!skills.trim()) {
      error = "Skills field is required";
      return;
    }
    if (!location.trim()) {
      error = "Location field is required";
      return;
    }
    if (!linkedinLiAt.trim()) {
      error = "LinkedIn authentication (li_at cookie) is required";
      return;
    }

    loading = true;
    try {
      const params: ScrapeParams = {
        skills: skills.trim(),
        location: location.trim(),
        maxResults: 1000,
        sources: [Source.LINKEDIN],
        authentication: {
          LINKEDIN: { li_at: linkedinLiAt.trim() },
        },
      };

      const scrapeId = await jobApi.triggerScrape(params);

      // Save search to history
      searchHistoryApi
        .recordSearch(skills.trim(), location.trim())
        .then(() => loadRecentSearches())
        .catch((err) => console.error("Failed to save search history:", err));

      // Subscribe to SSE progress
      if (cleanupSSE) cleanupSSE();
      cleanupSSE = jobApi.subscribeScrapeProgress(
        scrapeId,
        (event) => {
          progress = event;

          if (event.status === "COMPLETED") {
            loading = false;
            success = event.message;
            setTimeout(() => window.location.reload(), 5000);
          } else if (event.status === "FAILED") {
            loading = false;
            error = event.message;
          }
        },
        () => {
          // SSE error — fall back gracefully
          if (loading) {
            loading = false;
            success =
              "Scrape triggered! Connection to progress stream lost, but scraping continues in background.";
          }
        },
      );
    } catch (err: any) {
      error =
        err.response?.data?.message ||
        "Failed to trigger scrape. Please try again.";
      loading = false;
    }
  }

  function handleFocus(event: FocusEvent) {
    const input = event.target as HTMLInputElement;
    input.select();
  }
</script>

<div class="card bg-base-200 shadow-xl p-6">
  <h2 class="text-2xl font-bold text-white mb-6">🔍 Trigger New Scrape</h2>

  <form on:submit={handleSubmit} class="space-y-4">
    <!-- Skills Input -->
    <div class="form-control">
      <label class="label" for="skills">
        <span class="label-text text-gray-300">Skills *</span>
      </label>
      <input
        id="skills"
        type="text"
        placeholder="e.g., Java, Spring Boot"
        class="input input-bordered w-full"
        bind:value={skills}
        on:focus={handleFocus}
        disabled={loading}
        required
      />
    </div>

    <!-- Location Input -->
    <div class="form-control">
      <label class="label" for="location">
        <span class="label-text text-gray-300">Location *</span>
      </label>
      <input
        id="location"
        type="text"
        placeholder="e.g., Germany, United States"
        class="input input-bordered w-full"
        bind:value={location}
        on:focus={handleFocus}
        disabled={loading}
        required
      />
    </div>

    <!-- LinkedIn Authentication -->
    <div class="form-control">
      <label class="label" for="linkedinAuth">
        <span class="label-text text-gray-300"
          >LinkedIn Authentication (li_at) *</span
        >
      </label>
      <input
        id="linkedinAuth"
        type="password"
        placeholder="Paste your LinkedIn li_at cookie value"
        class="input input-bordered w-full font-mono text-sm"
        bind:value={linkedinLiAt}
        on:focus={handleFocus}
        disabled={loading}
        required
      />
      <label class="label">
        <span class="label-text-alt text-gray-400">
          ℹ️ Find this in your browser cookies when logged into LinkedIn
        </span>
      </label>
    </div>

    <!-- Recent Searches -->
    {#if recentSearches.length > 0}
      <div class="form-control">
        <button
          type="button"
          class="btn btn-outline btn-sm btn-info w-full"
          on:click={() => (showRecentSearches = !showRecentSearches)}
          disabled={loading}
        >
          {showRecentSearches ? "▲ Hide" : "▼ Show"} Recent Searches ({recentSearches.length})
        </button>

        {#if showRecentSearches}
          <div class="recent-searches-list mt-2 rounded-lg overflow-hidden">
            {#each recentSearches as search}
              <button
                type="button"
                class="recent-search-item w-full text-left px-4 py-3 flex justify-between items-center"
                on:click={() => selectRecentSearch(search)}
              >
                <div>
                  <span class="text-blue-300 font-medium">{search.skill}</span>
                  <span class="text-gray-400 mx-2">in</span>
                  <span class="text-green-300">{search.location}</span>
                </div>
                <span class="text-xs text-gray-500">
                  {new Date(search.searchedAt).toLocaleDateString()}
                </span>
              </button>
            {/each}
          </div>
        {/if}
      </div>
    {/if}

    <!-- Live Progress Panel -->
    {#if progress}
      <div class="progress-panel rounded-lg p-4">
        <div class="flex items-center justify-between mb-2">
          <span class="text-sm font-semibold text-white">
            {getStatusIcon()}
            {progress.status === "COMPLETED"
              ? "Scrape Complete"
              : progress.status === "FAILED"
                ? "Scrape Failed"
                : "Scraping in Progress"}
          </span>
        </div>

        <!-- Progress Bar -->
        <div class="progress-bar-bg rounded-full h-3 mb-3">
          <div
            class="progress-bar-fill h-3 rounded-full transition-all duration-500"
            class:progress-complete={progress.status === "COMPLETED"}
            class:progress-failed={progress.status === "FAILED"}
            style="width: {getProgressPercent()}%"
          ></div>
        </div>

        <!-- Stats Grid -->
        <div class="grid grid-cols-3 gap-2 mb-3 text-center">
          <div class="stat-card rounded-lg p-2">
            <div class="text-lg font-bold text-cyan-400">
              {progress.totalJobsFound}
            </div>
            <div class="text-xs text-gray-400">Unique Jobs</div>
          </div>
          <div class="stat-card rounded-lg p-2">
            <div class="text-lg font-bold text-yellow-400">
              {progress.duplicateJobsSkipped || 0}
            </div>
            <div class="text-xs text-gray-400">Duplicates</div>
          </div>
          <div class="stat-card rounded-lg p-2">
            <div
              class="text-lg font-bold"
              class:text-red-400={progress.errors > 0}
              class:text-green-400={progress.errors === 0}
            >
              {progress.errors}
            </div>
            <div class="text-xs text-gray-400">Errors</div>
          </div>
        </div>

        <!-- Skill Progress -->
        {#if progress.totalSkills > 0 && progress.status !== "COMPLETED" && progress.status !== "FAILED"}
          <div
            class="flex justify-between items-center text-xs text-gray-400 mb-1"
          >
            <span>
              Skill {progress.skillIndex}/{progress.totalSkills}
              {#if progress.currentSkill}
                — <span class="text-blue-300">{progress.currentSkill}</span>
              {/if}
            </span>
            <span>{Math.round(getProgressPercent())}%</span>
          </div>
        {/if}

        <!-- Per-Skill Job Count -->
        {#if progress.jobsFoundForSkill > 0 && progress.status === "SKILL_COMPLETED"}
          <div class="text-xs text-emerald-400 mb-1">
            ✓ Found {progress.jobsFoundForSkill} unique jobs for "{progress.currentSkill}"
          </div>
        {/if}

        <!-- Status Message -->
        <p
          class="text-xs text-gray-300 mt-1"
          class:animate-pulse-subtle={progress.status !== "COMPLETED" &&
            progress.status !== "FAILED"}
        >
          {progress.message}
        </p>
      </div>
    {/if}

    <!-- Error Message -->
    {#if error}
      <div class="alert alert-error">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="stroke-current shrink-0 h-6 w-6"
          fill="none"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <span>{error}</span>
      </div>
    {/if}

    <!-- Success Message -->
    {#if success}
      <div class="alert alert-success">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          class="stroke-current shrink-0 h-6 w-6"
          fill="none"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <span>{success}</span>
      </div>
    {/if}

    <!-- Submit Button -->
    <button type="submit" class="btn btn-primary w-full" disabled={loading}>
      {#if loading}
        <span class="loading loading-spinner"></span>
        Scraping...
      {:else}
        Trigger Scrape (Max: 1000 jobs)
      {/if}
    </button>
  </form>
</div>

<style>
  input {
    background: rgba(30, 41, 59, 0.8);
    border-color: rgba(71, 85, 105, 0.5);
    color: white;
  }

  input:focus {
    border-color: #3b82f6;
    outline: none;
  }

  input::placeholder {
    color: #64748b;
  }

  input:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .recent-searches-list {
    background: rgba(15, 23, 42, 0.9);
    border: 1px solid rgba(71, 85, 105, 0.3);
  }

  .recent-search-item {
    background: transparent;
    border: none;
    border-bottom: 1px solid rgba(71, 85, 105, 0.2);
    cursor: pointer;
    transition: background-color 0.2s;
    color: inherit;
    font-size: inherit;
  }

  .recent-search-item:hover {
    background: rgba(30, 41, 59, 0.8);
  }

  .recent-search-item:last-child {
    border-bottom: none;
  }

  .progress-panel {
    background: rgba(15, 23, 42, 0.95);
    border: 1px solid rgba(59, 130, 246, 0.3);
    box-shadow: 0 0 15px rgba(59, 130, 246, 0.1);
  }

  .progress-bar-bg {
    background: rgba(30, 41, 59, 0.8);
  }

  .progress-bar-fill {
    background: linear-gradient(90deg, #3b82f6, #06b6d4);
    box-shadow: 0 0 8px rgba(59, 130, 246, 0.4);
  }

  .stat-card {
    background: rgba(30, 41, 59, 0.6);
    border: 1px solid rgba(71, 85, 105, 0.3);
  }

  .progress-complete {
    background: linear-gradient(90deg, #10b981, #34d399) !important;
    box-shadow: 0 0 8px rgba(16, 185, 129, 0.4) !important;
  }

  .progress-failed {
    background: linear-gradient(90deg, #ef4444, #f87171) !important;
    box-shadow: 0 0 8px rgba(239, 68, 68, 0.4) !important;
  }

  .animate-pulse-subtle {
    animation: pulse-subtle 2s ease-in-out infinite;
  }

  @keyframes pulse-subtle {
    0%,
    100% {
      opacity: 1;
    }
    50% {
      opacity: 0.7;
    }
  }
</style>
