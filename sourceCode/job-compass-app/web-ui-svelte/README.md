# JobCompass UI - Svelte + TypeScript

Modern, type-safe UI for JobCompass built with Svelte, TypeScript, and comprehensive testing.

## 🚀 Quick Start

```bash
# Install dependencies
npm install

# Development server (with HMR)
npm run dev

# Run tests
npm test

# Run tests with UI
npm run test:ui

# Coverage report
npm run test:coverage

# Build for production
npm run build

# Preview production build
npm run preview
```

## 🏗️ Architecture

- **Svelte 4** - Component framework
- **TypeScript** - Type safety (strict mode)
- **Vite** - Build tool & dev server
- **Vitest** - Testing framework
- **Tailwind CSS** - Utility-first styling
- **DaisyUI** - Component library

## 📁 Project Structure

```
src/
├── components/       # Reusable UI components
│   ├── JobCard.svelte
│   ├── SearchBar.svelte
│   ├── FilterDropdown.svelte
│   ├── JobList.svelte
│   └── ScrapeConfig.svelte
├── stores/           # Svelte stores (state management)
├── services/         # API layer
├── types/            # TypeScript definitions
└── utils/            # Helper functions
```

## 🧪 Testing

- **Test Coverage Target**: ≥80%
- **Testing Library**: @testing-library/svelte
- **API Mocking**: MSW (Mock Service Worker)

Run tests:
```bash
npm test              # Run all tests
npm run test:ui       # Interactive test UI
npm run test:coverage # Coverage report
```

## 🐳 Docker

Build and run:
```bash
docker build -t jobcompass-ui .
docker run -p 8085:8085 jobcompass-ui
```

Or with docker-compose:
```bash
docker-compose up web-ui
```

## 🎨 Features

- ✅ Language-labeled job cards
- ✅ Advanced filtering (search, language, source, date)
- ✅ Grid/List view toggle
- ✅ Real-time job scraping
- ✅ Responsive design
- ✅ Dark theme
- ✅ Auto-select input on focus

## 📦 Dependencies

See [package.json](./package.json) for complete list.

## 🔧 Configuration Files

- `vite.config.ts` - Build configuration + API proxy
- `vitest.config.ts` - Test configuration
- `tsconfig.json` - TypeScript configuration
- `tailwind.config.js` - Styling configuration
- `nginx.conf` - Production web server config

## 🌐 API Endpoints

- `GET /api/jobs` - Fetch all jobs
- `POST /api/scrape/trigger` - Trigger new scrape

## 🐛 Troubleshooting

### Tests failing?
```bash
npm run test -- --reporter=verbose
```

### Build errors?
```bash
rm -rf node_modules dist
npm install
npm run build
```

### Port already in use?
Change port in `vite.config.ts`:
```typescript
server: { port: 3000 }
```

## 📝 License

MIT
