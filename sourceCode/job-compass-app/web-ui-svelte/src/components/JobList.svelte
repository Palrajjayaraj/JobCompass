<script lang="ts">
  import { filteredJobs, jobStore } from '$stores/jobStore';
  import JobCard from './JobCard.svelte';
  import { Source } from '$types/job.types';
  import type { ViewMode } from '$types/job.types';
  
  export let viewMode: ViewMode = 'grid';
  
  $: isEmpty = $filteredJobs.length === 0;
  $: gridClass = viewMode === 'grid' 
    ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6' 
    : 'flex flex-col gap-4';
</script>

<div class="w-full">
  {#if isEmpty}
    <div class="text-center py-20">
      <div class="text-6xl mb-4">🔍</div>
      <h3 class="text-2xl font-bold text-gray-300 mb-2">No jobs found</h3>
      <p class="text-gray-500">Try adjusting your filters or trigger a new scrape</p>
    </div>
  {:else}
    <div class={gridClass}>
      {#each $filteredJobs as job (job.id)}
        <JobCard {job} {viewMode} />
      {/each}
    </div>
  {/if}
</div>
