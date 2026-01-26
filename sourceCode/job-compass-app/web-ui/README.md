# JobCompass Web UI

A beautiful, modern web interface to view and search job postings scraped by JobCompass.

## Features

✨ **Modern Design**
- Dark theme with vibrant gradients
- Glassmorphism effects
- Smooth animations and transitions
- Fully responsive layout

🔍 **Smart Search & Filtering**
- Real-time search by title, company, or location
- Filter by job source (LinkedIn, Glassdoor, Indeed)
- Filter by posting date (last 1, 3, 7, or 30 days)

💼 **Job Display**
- Beautiful card-based layout
- Source badges with custom colors
- Job age indicators
- Salary and location information
- Modal detail view with full job description

📊 **Statistics**
- Total jobs count
- Weekly jobs count
- Animated number transitions

## Quick Start

### Prerequisites

Make sure the following services are running:

1. **PostgreSQL** (port 5432)
2. **Kafka** (port 9092)
3. **Storage Service** (port 8082)

### Launch Web UI

Simply open `index.html` in your web browser:

```bash
# Option 1: Open directly
open index.html

# Option 2: Use a local server (recommended for development)
python3 -m http.server 8000
# Then visit: http://localhost:8000
```

## Usage

1. **View Jobs**: The UI automatically loads recent jobs (last 7 days) on startup
2. **Search**: Type in the search box to filter jobs by title, company, or location
3. **Filter by Source**: Select a source from the dropdown (LinkedIn, Glassdoor, Indeed)
4. **Filter by Date**: Change the date range dropdown to see jobs from different time periods
5. **View Details**: Click on any job card or "View Details" button to see full job information
6. **Apply**: Click "Apply on [Source]" button in the modal to visit the original job posting

## API Configuration

The web UI connects to the storage-service REST API. By default, it uses:

```javascript
const API_BASE_URL = 'http://localhost:8082/api';
```

If your storage-service runs on a different port, update this value in `app.js`.

## File Structure

```
web-ui/
├── index.html      # Main HTML structure
├── styles.css      # CSS styling with modern design
├── app.js          # JavaScript for API integration and interactivity
└── README.md       # This file
```

## Keyboard Shortcuts

- `Esc`: Close job detail modal
- `Ctrl+F` / `Cmd+F`: Focus search box (browser default)

## Browser Compatibility

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Troubleshooting

### Jobs not loading?

1. **Check storage-service**: Make sure it's running on port 8082
   ```bash
   curl http://localhost:8082/api/jobs
   ```

2. **Check CORS**: If running from `file://`, some browsers may block API requests. Use a local web server instead.

3. **Check Console**: Open browser DevTools (F12) and check the Console for error messages.

### No jobs displayed?

- Make sure you have scraped some jobs using the scraper-service
- Try changing the date filter to "Last 30 days"
- Check that PostgreSQL database has job records

## Future Enhancements

- [ ] Job application tracking
- [ ] Saved jobs / favorites
- [ ] Email notifications for new jobs
- [ ] Advanced filters (salary range, skills, etc.)
- [ ] Export jobs to CSV/PDF
- [ ] Dark/Light theme toggle

## Tech Stack

- **Frontend**: Vanilla HTML, CSS, JavaScript
- **API**: RESTful API from storage-service
- **Design**: Custom CSS with CSS Variables, Gradients, Animations
- **Fonts**: Inter (Google Fonts)

## License

Part of the JobCompass project.
