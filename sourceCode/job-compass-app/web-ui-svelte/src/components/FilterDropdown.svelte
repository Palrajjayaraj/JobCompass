<script lang="ts">
  export let label: string;
  export let value: string | 'all';
  export let options: { value: string | 'all'; label: string }[];
  export let id: string = 'filter-select';
  
  function handleChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    value = target.value;
  }
</script>

<div class="form-control w-full max-w-xs">
  <label class="label" for={id}>
    <span class="label-text text-gray-300">{label}</span>
  </label>
  <select
    {id}
    class="select select-bordered w-full"
    bind:value
    on:change={handleChange}
  >
    {#each options as option}
      <option value={option.value}>{option.label}</option>
    {/each}
  </select>
</div>

<style>
  select {
    background: rgba(30, 41, 59, 0.8);
    border-color: rgba(71, 85, 105, 0.5);
    color: white;
  }

  select:focus {
    border-color: #3b82f6;
    outline: none;
  }

  option {
    background: #1e293b;
    color: white;
  }
</style>
