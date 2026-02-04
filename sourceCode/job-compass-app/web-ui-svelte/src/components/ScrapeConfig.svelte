<script lang="ts">
  import { jobApi } from '$services/api';
  import type { ScrapeParams } from '$types/job.types';
  import { Source } from '$types/job.types';
  
  let skills = '';
  let location = '';
  let linkedinLiAt = ''; // LinkedIn li_at cookie
  let loading = false;
  let error = '';
  let success = '';
  
  async function handleSubmit(event: Event) {
    event.preventDefault();
    
    // Validation
    error = '';
    success = '';
    
    if (!skills.trim()) {
      error = 'Skills field is required';
      return;
    }
    
    if (!location.trim()) {
      error = 'Location field is required';
      return;
    }

    if (!linkedinLiAt.trim()) {
      error = 'LinkedIn authentication (li_at cookie) is required';
      return;
    }
    
    // Submit
    loading = true;
    try {
      const params: ScrapeParams = {
        skills: skills.trim(),
        location: location.trim(),
        maxResults: 1000, // Fixed at 1000
        sources: [Source.LINKEDIN],
        authentication: {
          'LINKEDIN': {
            'li_at': linkedinLiAt.trim()
          }
        }
      };
      
      await jobApi.triggerScrape(params);
      success = `Scrape triggered successfully! Scraping up to 1000 jobs for "${skills}" in "${location}". Jobs will appear shortly.`;
      
      // Auto-refresh jobs after 5 seconds
      setTimeout(() => {
        window.location.reload();
      }, 5000);
      
    } catch (err: any) {
      error = err.response?.data?.message || 'Failed to trigger scrape. Please try again.';
    } finally {
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
        <span class="label-text text-gray-300">LinkedIn Authentication (li_at) *</span>
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
    
    <!-- Error Message -->
    {#if error}
      <div class="alert alert-error">
        <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span>{error}</span>
      </div>
    {/if}
    
    <!-- Success Message -->
    {#if success}
      <div class="alert alert-success">
        <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span>{success}</span>
      </div>
    {/if}
    
    <!-- Submit Button -->
    <button
      type="submit"
      class="btn btn-primary w-full"
      disabled={loading}
    >
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
</style>
