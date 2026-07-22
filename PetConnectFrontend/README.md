# PetConnect Frontend

Vue 3 + TypeScript + Vite frontend for PetConnect.

## Setup

```bash
npm install
cp .env.example .env.local   # point VITE_API_BASE_URL at Jesse's backend (default: localhost:8080)
npm run dev
```

## What's built

- **Routing** (`src/router`): `/`, `/login`, `/signup`, `/pets/:id`, `/pets/new`, `/favorites`, `/profile`
- **Auth store** (Pinia, `src/stores/auth.ts`): login/signup/logout, persists to localStorage
- **API layer** (`src/api`): typed wrappers around every backend endpoint, both the ones that
  exist today and the ones described in `API_CONTRACT.md`
- **Views**: Home/browse with search + species filter, pet detail, login, signup, add-a-listing
  form, favorites, profile (with change password)
- Pages that depend on endpoints not built yet (browse, pet detail, favorites) fall back to
  sample data automatically so they're still reviewable -- you'll see a small banner when that's
  happening.

## Talking to the backend

- Signup, login, change password, and add-pet are already wired to the real Spring Boot
  endpoints (`PetController` / `UserController`).
- Everything else is written against the shape described in `API_CONTRACT.md` -- once those
  endpoints exist on the backend, delete the try/catch fallbacks in the views and it should
  just work.

## Folder structure

```
src/
  api/        axios client + typed API calls
  assets/     global CSS / design tokens
  components/ NavBar, PetCard
  router/     route definitions
  stores/     Pinia auth store
  types/      TS interfaces mirroring the ER diagram + backend DTOs
  views/      page-level components
```
