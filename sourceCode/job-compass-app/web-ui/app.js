// API Configuration
const API_BASE_URL = 'http://localhost:8082/api';

// State
let allJobs = [];
let filteredJobs = [];

// DOM Elements
const jobsGrid = document.getElementById('jobsGrid');
const loadingState = document.getElementById('loadingState');
const errorState = document.getElementById('errorState');
const emptyState = document.getElementById('emptyState');
const searchInput = document.getElementById('searchInput');
const sourceFilter = document.getElementById('sourceFilter');
const daysFilter = document.getElementById('daysFilter');
const refreshBtn = document.getElementById('refreshBtn');
const totalJobsEl = document.getElementById('totalJobs');
const recentJobsEl = document.getElementById('recentJobs');
const modal = document.getElementById('jobModal');
const closeBtn = document.querySelector('.close-btn');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    loadJobs();
    setupEventListeners();
});

// Event Listeners
function setupEventListeners() {
    searchInput.addEventListener('input', debounce(filterJobs, 300));
    sourceFilter.addEventListener('change', filterJobs);
    daysFilter.addEventListener('change', loadJobs);
    refreshBtn.addEventListener('click', loadJobs);
    document.getElementById('triggerScrapeBtn').addEventListener('click', triggerScrape);
    closeBtn.addEventListener('click', closeModal);
    window.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });
}

// Trigger Manual Scrape
async function triggerScrape() {
    const btn = document.getElementById('triggerScrapeBtn');
    const originalText = btn.innerHTML;
    btn.innerHTML = '<span>⏳</span> Scraping...';
    btn.disabled = true;

    try {
        // Call Scraper Service directly (exposed on 8082 via Docker)
        // Note: In local dev without Docker custom networking, this hits localhost:8082
        const response = await fetch('http://localhost:8082/api/scraper/trigger/linkedin?maxResults=10&maxJobAgeDays=1', {
            method: 'POST'
        });

        if (response.ok) {
            alert('Scraping started successfully! Jobs will appear shortly.');
            // Reload jobs after a short delay to see new results
            setTimeout(loadJobs, 2000);
        } else {
            alert('Failed to trigger scraping.');
        }
    } catch (error) {
        console.error('Error triggering scrape:', error);
        alert('Error triggering scrape. Ensure Scraper Service is running on port 8082.');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

// Load Jobs from API
async function loadJobs() {
    showLoading();

    const days = daysFilter.value;
    const endpoint = `${API_BASE_URL}/jobs/recent?days=${days}`;

    try {
        const response = await fetch(endpoint);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        allJobs = await response.json();
        filteredJobs = [...allJobs];

        updateStats();
        filterJobs();
        showJobs();

    } catch (error) {
        console.error('Error loading jobs:', error);
        showError(error.message);
    }
}

// Filter Jobs
function filterJobs() {
    const searchTerm = searchInput.value.toLowerCase().trim();
    const sourceValue = sourceFilter.value;

    filteredJobs = allJobs.filter(job => {
        const matchesSearch = !searchTerm ||
            job.title?.toLowerCase().includes(searchTerm) ||
            job.companyName?.toLowerCase().includes(searchTerm) ||
            job.location?.toLowerCase().includes(searchTerm);

        const matchesSource = !sourceValue || job.source?.name === sourceValue;

        return matchesSearch && matchesSource;
    });

    renderJobs();
}

// Render Jobs
function renderJobs() {
    if (filteredJobs.length === 0) {
        showEmpty();
        return;
    }

    jobsGrid.innerHTML = filteredJobs.map(job => createJobCard(job)).join('');
    showJobs();
}

// Create Job Card HTML
function createJobCard(job) {
    const source = job.source?.name || 'Unknown';
    const sourceClass = `source-${source.toLowerCase()}`;
    const jobAge = job.jobAgeDays ? `${job.jobAgeDays}d ago` : 'Recently';
    const salary = job.salaryRange || 'Not specified';
    const location = job.location || 'Not specified';

    return `
        <div class="job-card" onclick="showJobDetail(${job.id})">
            <div class="job-header">
                <span class="job-source ${sourceClass}">${source}</span>
                <span class="job-age">${jobAge}</span>
            </div>
            
            <h3 class="job-title">${escapeHtml(job.title)}</h3>
            <div class="job-company">
                <span>🏢</span>
                <span>${escapeHtml(job.companyName || 'Company')}</span>
            </div>
            
            <div class="job-details">
                <div class="job-detail">
                    <span>📍</span>
                    <span>${escapeHtml(location)}</span>
                </div>
                <div class="job-detail">
                    <span>💰</span>
                    <span>${escapeHtml(salary)}</span>
                </div>
            </div>
            
            <div class="job-footer">
                <button class="view-details-btn" onclick="event.stopPropagation(); showJobDetail(${job.id})">
                    View Details →
                </button>
            </div>
        </div>
    `;
}

// Show Job Detail Modal
async function showJobDetail(jobId) {
    try {
        const response = await fetch(`${API_BASE_URL}/jobs/${jobId}`);
        if (!response.ok) throw new Error('Failed to load job details');

        const job = await response.json();
        const skills = job.skills?.length > 0 ?
            job.skills.map(s => `<span class="skill-tag">${escapeHtml(s)}</span>`).join('') :
            '<span class="text-muted">No skills listed</span>';

        const source = job.source?.name || 'Unknown';
        const sourceClass = `source-${source.toLowerCase()}`;
        const jobAge = job.jobAgeDays ? `Posted ${job.jobAgeDays} days ago` : 'Recently posted';
        const salary = job.salaryRange || 'Not specified';
        const location = job.location || 'Not specified';
        const description = job.description || 'No description available';

        document.getElementById('jobDetailContent').innerHTML = `
            <div class="job-detail-header">
                <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 1.5rem;">
                    <div>
                        <span class="job-source ${sourceClass}" style="display: inline-block; margin-bottom: 1rem;">${source}</span>
                        <h2 style="font-size: 2rem; margin-bottom: 0.5rem;">${escapeHtml(job.title)}</h2>
                        <div style="font-size: 1.25rem; color: var(--accent-purple); font-weight: 600; display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.5rem;">
                            <span>🏢</span>
                            <span>${escapeHtml(job.companyName || 'Company')}</span>
                        </div>
                        <div style="color: var(--text-secondary); margin-bottom: 1rem;">${jobAge}</div>
                    </div>
                </div>
                
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 2rem;">
                    <div style="background: var(--card-bg); padding: 1rem; border-radius: 12px; border: 1px solid var(--card-border);">
                        <div style="color: var(--text-muted); font-size: 0.875rem; margin-bottom: 0.25rem;">Location</div>
                        <div style="font-weight: 600;">📍 ${escapeHtml(location)}</div>
                    </div>
                    <div style="background: var(--card-bg); padding: 1rem; border-radius: 12px; border: 1px solid var(--card-border);">
                        <div style="color: var(--text-muted); font-size: 0.875rem; margin-bottom: 0.25rem;">Salary</div>
                        <div style="font-weight: 600;">💰 ${escapeHtml(salary)}</div>
                    </div>
                </div>
                
                <div style="margin-bottom: 2rem;">
                    <h3 style="font-size: 1.25rem; margin-bottom: 1rem;">Required Skills</h3>
                    <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
                        ${skills}
                    </div>
                </div>
                
                ${description !== 'No description available' ? `
                    <div style="margin-bottom: 2rem;">
                        <h3 style="font-size: 1.25rem; margin-bottom: 1rem;">Job Description</h3>
                        <div style="color: var(--text-secondary); line-height: 1.8;">
                            ${escapeHtml(description)}
                        </div>
                    </div>
                ` : ''}
                
                <div style="margin-top: 2rem; padding-top: 2rem; border-top: 1px solid var(--card-border);">
                    <a href="${escapeHtml(job.url)}" target="_blank" rel="noopener noreferrer" 
                       style="display: inline-block; padding: 1rem 2rem; background: var(--primary-gradient); border-radius: 12px; 
                              color: white; text-decoration: none; font-weight: 600; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);"
                       onmouseover="this.style.transform='translateY(-2px)'; this.style.boxShadow='0 6px 25px rgba(102, 126, 234, 0.4)'"
                       onmouseout="this.style.transform='translateY(0)'; this.style.boxShadow='0 4px 15px rgba(102, 126, 234, 0.3)'">
                        Apply on ${source} →
                    </a>
                </div>
            </div>
            
            <style>
                .skill-tag {
                    display: inline-block;
                    padding: 0.5rem 1rem;
                    background: var(--primary-gradient);
                    border-radius: 20px;
                    font-size: 0.875rem;
                    font-weight: 600;
                }
                
                .text-muted {
                    color: var(--text-muted);
                }
            </style>
        `;

        modal.style.display = 'block';
    } catch (error) {
        console.error('Error loading job details:', error);
        alert('Failed to load job details. Please try again.');
    }
}

// Close Modal
function closeModal() {
    modal.style.display = 'none';
}

// Update Stats
function updateStats() {
    totalJobsEl.textContent = allJobs.length;

    // Count jobs from last 7 days
    const weekAgo = new Date();
    weekAgo.setDate(weekAgo.getDate() - 7);
    const recentCount = allJobs.filter(job => {
        if (!job.postedDate) return false;
        const postedDate = new Date(job.postedDate);
        return postedDate >= weekAgo;
    }).length;

    recentJobsEl.textContent = recentCount;

    // Animate numbers
    animateValue(totalJobsEl, 0, allJobs.length, 1000);
    animateValue(recentJobsEl, 0, recentCount, 1000);
}

// Animate Number
function animateValue(element, start, end, duration) {
    const range = end - start;
    const increment = range / (duration / 16);
    let current = start;

    const timer = setInterval(() => {
        current += increment;
        if (current >= end) {
            element.textContent = end;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(current);
        }
    }, 16);
}

// State Management
function showLoading() {
    loadingState.style.display = 'block';
    errorState.style.display = 'none';
    emptyState.style.display = 'none';
    jobsGrid.style.display = 'none';
}

function showError(message) {
    document.getElementById('errorMessage').textContent = message || 'Unable to load jobs';
    loadingState.style.display = 'none';
    errorState.style.display = 'block';
    emptyState.style.display = 'none';
    jobsGrid.style.display = 'none';
}

function showEmpty() {
    loadingState.style.display = 'none';
    errorState.style.display = 'none';
    emptyState.style.display = 'block';
    jobsGrid.style.display = 'none';
}

function showJobs() {
    loadingState.style.display = 'none';
    errorState.style.display = 'none';
    emptyState.style.display = 'none';
    jobsGrid.style.display = 'grid';
}

// Utility Functions
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
