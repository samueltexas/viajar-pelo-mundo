# ViajarPeloMundo

A full-stack travel and flight booking application built with
Java, Spring Boot, PostgreSQL, React and TypeScript.

## Overview

ViajarPeloMundo is a portfolio project demonstrating
full-stack software engineering, REST API development,
relational database design, automated testing,
containerization and continuous integration.

## Technology Stack

### Backend
- Java 25
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL 17
- Flyway
- Maven

### Frontend
- React
- TypeScript
- Vite
- i18next

### DevOps
- Docker and Docker Compose
- GitHub Actions
- Vercel-ready frontend configuration

## Main Features

- User registration and authentication
- Flight search
- Airport autocomplete
- Flight bookings
- Round-trip booking support
- Booking cancellation
- Multilingual interface
- Automated backend tests

## Local Development

Requirements:
- Docker
- Docker Compose

1. Clone the repository.
2. Copy `.env.example` to `.env`.
3. Configure the environment variables.
4. Start the application using Docker Compose.

```bash
docker compose up --build -d
```

Frontend:

http://localhost:8088

## Testing

Backend:

```bash
cd backend
./mvnw --batch-mode verify
```

Integration tests require a configured PostgreSQL test database.

Frontend:

```bash
cd frontend
npm ci
npm run lint
npm run build
```

## Deployment

The frontend can be deployed to Vercel.

The Spring Boot API and PostgreSQL database require
separate hosting.

See `docs/DEPLOYMENT.md`.

## Security

See `SECURITY.md`.

Do not commit `.env` files, production credentials,
database passwords or private API keys.

## Project Status

Active development.

## Author

Samuel Texa
