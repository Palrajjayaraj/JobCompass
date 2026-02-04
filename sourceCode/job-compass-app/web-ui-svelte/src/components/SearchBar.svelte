<script lang="ts">
  import { jobStore, filteredJobs } from '$stores/jobStore';
  import { debounce } from '$utils/helpers';

  let searchInput = '';
  
  const debouncedSearch = debounce((value: string) => {
    jobStore.setSearchQuery(value);
  }, 300);

  $: debouncedSearch(searchInput);
  $: jobCount = $filteredJobs.length;
</script>

<div class="form-control w-full max-w-md">
  <label class="label" for="search-input">
    <span class="label-text text-gray-300">Search Jobs</span>
    <span class="label-text-alt text-gray-500">{jobCount} jobs</span>
  </label>
  <div class="relative">
    <input
      id="search-input"
      type="text"
      placeholder="Search by title, company, or description..."
      class="input input-bordered w-full pr-10"
      bind:value={searchInput}
      on:focus={(e) => e.currentTarget.select()}
    />
    {#if searchInput}
      <button
        class="absolute right-2 top-1/2 -translate-y-1/2 btn btn-ghost btn-sm btn-circle"
        on:click={() => searchInput = ''}
        aria-label="Clear search"
      >
        ✕
      </button>
    {/if}
  </div>
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
</style>
