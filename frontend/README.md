# ViajarPeloMundo — Frontend

The frontend of ViajarPeloMundo, a full-stack flight search and booking portfolio application.

## Technology Stack

- React
- TypeScript
- Vite
- i18next
- CSS
- Nginx (production container)

## Features

- Responsive travel interface
- Airport search and autocomplete
- Flight search
- User registration and sign-in
- One-way and round-trip bookings
- Booking management and cancellation
- Simulated payments (no real charges)
- Multilingual interface: English, Portuguese and Polish

## Requirements

- Node.js 24
- npm
- Running ViajarPeloMundo backend API

## Local Development

From the `frontend` directory:

```bash
npm ci
npm run dev
```

Vite will display the local development URL in the terminal.

## Production Build

```bash
npm run lint
npm run build
```

The production build is generated in `dist/`.

## Configuration

The frontend communicates with the Spring Boot REST API.

Review the API configuration in:

`src/lib/api.ts`

and the Vite configuration in:

`vite.config.ts`

before deploying to a new environment.

Never place private credentials, database passwords or server-side secrets in frontend environment variables.

## Docker

The production frontend uses a multi-stage Docker build and Nginx.

See:

- `Dockerfile`
- `nginx.conf`
- `../docker-compose.prod.yml`

## Continuous Integration

GitHub Actions runs dependency installation, linting and production builds.

Workflow:

`../.github/workflows/frontend-ci.yml`

## Security

Authentication and authorization are enforced by the backend.

Payments are simulations and do not process real financial transactions.

See `../SECURITY.md`.

## Project

See the [main README](../README.md) for the complete architecture, backend setup and deployment information.
