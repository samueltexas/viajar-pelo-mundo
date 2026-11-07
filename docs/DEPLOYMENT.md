# Deployment Guide

## Architecture

Frontend: React + TypeScript + Vite
Backend: Java 25 + Spring Boot
Database: PostgreSQL 17

## Local Environment

Docker Compose runs the application locally.

The frontend uses relative API paths:

/api/v1

Nginx forwards these requests to Spring Boot.

## Vercel Frontend

Import the GitHub repository into Vercel.

Configuration:

- Framework: Vite
- Root Directory: frontend
- Build Command: npm run build
- Output Directory: dist

Environment variable:

VITE_API_BASE_URL=https://your-api-domain.example

The variable must point to the public backend origin,
without the /api/v1 suffix.

Do not configure a placeholder API address for
a production deployment.

## Backend Hosting

The Java backend must be deployed separately.

Production configuration must include:

- Database connection settings
- Secure credentials
- HTTPS
- Allowed frontend origins
- Database migrations

## Important

A frontend deployment alone does not provide
a working booking API.

The backend and PostgreSQL must also be available
for full application functionality.
