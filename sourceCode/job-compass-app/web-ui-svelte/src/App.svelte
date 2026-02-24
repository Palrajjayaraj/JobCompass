<script lang="ts">
  import { onMount } from "svelte";
  import { jobStore, filters, jobs, filteredJobs } from "$stores/jobStore";
  import { jobApi } from "$services/api";
  import { Source } from "$types/job.types";
  import type { ViewMode } from "$types/job.types";

  // Components
  import SearchBar from "./components/SearchBar.svelte";
  import FilterDropdown from "./components/FilterDropdown.svelte";
  import JobList from "./components/JobList.svelte";
  import ScrapeConfig from "./components/ScrapeConfig.svelte";

  let viewMode: ViewMode = "grid";
  let showScrapeConfig = false;

  // Country options (derived from unique countries in jobs)
  $: countryOptions = [
    { value: "all", label: "All Countries" },
    ...Array.from(
      new Set(
        $jobs
          .map((j) => {
            if (!j.location) return "";
            const parts = j.location.split(",").map((p) => p.trim());
            return parts[parts.length - 1];
          })
          .filter((c) => c !== ""),
      ),
    )
      .sort()
      .map((c) => ({ value: c, label: c })),
  ];

  // Keep language options just in case filtering is still desired, replacing the displayed filter though
  let languageOptions = [
    { value: "all", label: "All Languages" },
    { value: "en", label: "English" },
    { value: "de", label: "German" },
  ];

  const sourceOptions = [
    { value: "all", label: "All Sources" },
    { value: Source.LINKEDIN, label: "LinkedIn" },
    { value: Source.GLASSDOOR, label: "Glassdoor" },
    { value: Source.INDEED, label: "Indeed" },
  ];

  const sortOptions = [
    { value: "date", label: "Latest First" },
    { value: "title", label: "Alphabetical" },
  ];

  // Load jobs on mount
  onMount(async () => {
    jobStore.setLoading(true);
    try {
      const jobs = await jobApi.getJobs();
      jobStore.setJobs(jobs);
    } catch (err: any) {
      jobStore.setError(err.message || "Failed to load jobs");
    } finally {
      jobStore.setLoading(false);
    }
  });

  // Reactive bindings for filters
  $: {
    if ($filters.selectedLanguage) {
      jobStore.setLanguageFilter($filters.selectedLanguage);
    }
  }

  $: {
    if ($filters.selectedCountry) {
      jobStore.setCountryFilter($filters.selectedCountry);
    }
  }

  $: {
    if ($filters.selectedSource) {
      jobStore.setSourceFilter($filters.selectedSource);
    }
  }

  $: {
    if ($filters.sortBy) {
      jobStore.setSortBy($filters.sortBy);
    }
  }

  function openTopJobs() {
    const topJobs = $filteredJobs.slice(0, 10);
    if (topJobs.length === 0) return;

    let blocked = false;
    // Open in new tabs
    topJobs.forEach((job) => {
      const win = window.open(job.url, "_blank");
      if (!win) {
        blocked = true;
      }
    });

    if (blocked) {
      alert(
        "⚠️ Popups Blocked! \n\nPlease allow popups for this site (check the icon in your address bar) to open all 10 jobs simultaneously.",
      );
    }
  }
</script>

<svelte:head>
  <title>JobCompass - Discover Your Next Opportunity</title>
</svelte:head>

<div class="min-h-screen bg-gradient-to-br from-slate-900 to-slate-800 p-6">
  <div class="max-w-7xl mx-auto">
    <!-- Header -->
    <header class="mb-8">
      <h1
        class="text-5xl font-bold text-white mb-2 bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-purple-600"
      >
        🧭 JobCompass
      </h1>
      <p class="text-gray-400 text-lg">
        Discover your next opportunity with language-labeled job listings
      </p>
    </header>

    <!-- Top Actions -->
    <div class="flex justify-between items-center mb-6">
      <div class="flex gap-2">
        <button
          class="btn btn-outline btn-sm"
          class:btn-active={viewMode === "grid"}
          on:click={() => (viewMode = "grid")}
          aria-label="Grid view"
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
              d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"
            />
          </svg>
        </button>
        <button
          class="btn btn-outline btn-sm"
          class:btn-active={viewMode === "list"}
          on:click={() => (viewMode = "list")}
          aria-label="List view"
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
              d="M4 6h16M4 12h16M4 18h16"
            />
          </svg>
        </button>

        <button
          class="btn btn-accent btn-sm ml-2"
          on:click={openTopJobs}
          disabled={$filteredJobs.length === 0}
          title="Open first 10 jobs in new tabs"
        >
          Open Top 10 🚀
        </button>
      </div>

      <button
        class="btn btn-primary btn-sm"
        on:click={() => (showScrapeConfig = !showScrapeConfig)}
      >
        {showScrapeConfig ? "Hide" : "Trigger"} Scrape
      </button>
    </div>

    <!-- Stats Bar -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <div class="stat bg-base-200 shadow rounded-lg p-4">
        <div class="stat-title text-gray-400">Total Jobs</div>
        <div class="stat-value text-primary">{$jobs.length}</div>
        <div class="stat-desc">Found in database</div>
      </div>

      <div class="stat bg-base-200 shadow rounded-lg p-4">
        <div class="stat-title text-gray-400">Visible</div>
        <div class="stat-value text-secondary">{$filteredJobs.length}</div>
        <div class="stat-desc text-secondary">Matching filters</div>
      </div>

      <div class="stat bg-base-200 shadow rounded-lg p-4">
        <div class="stat-title text-gray-400">Applied</div>
        <div class="stat-value text-accent">
          {$jobs.filter((j) => (j.applicationCount || 0) > 0).length}
        </div>
        <div class="stat-desc">Applications sent</div>
      </div>
    </div>

    <!-- Scrape Config (Collapsible) -->
    {#if showScrapeConfig}
      <div class="mb-6 animate-fadeIn">
        <ScrapeConfig />
      </div>
    {/if}

    <!-- Filters Bar -->
    <div class="bg-base-200 rounded-lg p-6 mb-6 shadow-xl">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <SearchBar />

        <FilterDropdown
          label="Country"
          bind:value={$filters.selectedCountry}
          options={countryOptions}
          id="country-filter"
        />

        <FilterDropdown
          label="Source"
          bind:value={$filters.selectedSource}
          options={sourceOptions}
          id="source-filter"
        />

        <FilterDropdown
          label="Sort By"
          bind:value={$filters.sortBy}
          options={sortOptions}
          id="sort-filter"
        />
      </div>
    </div>

    <!-- Job List -->
    <JobList {viewMode} />
  </div>
</div>

<style>
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .animate-fadeIn {
    animation: fadeIn 0.3s ease-out;
  }
</style>
