# PetConnect – Frontend API Requirements

The frontend (Vue 3 + TypeScript) is scaffolded and already wired to the endpoints that
exist today. Below is exactly what it calls now, and what it needs next, with request/response
shapes so you can build directly against this.

All responses (except `POST /pets/add`, which returns `Pet` directly) use the existing
`InnerRespond<T>` envelope:
```json
{ "state": true, "message": "string", "data": { ... } }
```

## Already wired up (working against your current code)
- `POST /users/signup` — `SignupRequest` → `InnerRespond<User>`
- `POST /users/login` — query params `username`, `password` → `InnerRespond<User>`
- `POST /users/changePassword` — `{ username, oldPassword, newPassword }` → `InnerRespond<User>`
- `GET /users/getUserByUsername/{username}` → `InnerRespond<User>`
- `GET /users/getUserByEmail/{email}` → `InnerRespond<User>`
- `POST /pets/add` — `AddPetRequest` → `Pet`

## Needed next, in priority order

### 1. Auth: return a token from login
`POST /users/login` currently returns the `User` object but no token, so the frontend can't
authenticate later requests (favorites, messaging, editing a listing). Could just be a JWT
added to the existing response:
```json
{ "state": true, "message": "Login successful", "data": { "user": {...}, "token": "eyJ..." } }
```
Frontend will send it back as `Authorization: Bearer <token>` on every request (already wired
in `src/api/client.ts`).

### 2. Browse/search pets — `GET /pets/search`
Query params (all optional): `species`, `breed`, `minAge`, `maxAge`, `sex`, `city`, `state`,
`page`, `pageSize`.
```json
{
  "state": true,
  "message": "ok",
  "data": [
    {
      "petId": 1,
      "name": "Biscuit",
      "species": "Dog",
      "breed": "Golden Retriever",
      "sex": "Male",
      "age": 2,
      "listingStatus": "AVAILABLE",
      "owner": { "userId": 4, "username": "jyang", "fullname": "Jesse Yang" },
      "address": { "city": "San Jose", "state": "CA" },
      "photos": [{ "photoUrl": "https://...", "displayOrder": 1 }]
    }
  ]
}
```
This is the Home/Browse page — highest priority, everything else depends on it existing.

### 3. Pet detail — `GET /pets/{id}`
Same `Pet` shape as above but with the full `medicalRecord` object included.

### 4. Favorites
- `POST /favorites/{petId}` — save a pet (auth required)
- `DELETE /favorites/{petId}` — unsave a pet (auth required)
- `GET /favorites` — list the current user's saved pets → `InnerRespond<Pet[]>`

### 5. Editing/removing a listing
- `PUT /pets/{petId}` — same body shape as `AddPetRequest`, owner-only
- `DELETE /pets/{petId}` — owner-only
- `GET /pets/owner/{userId}` — "my listings" → `InnerRespond<Pet[]>`

### 6. Messaging (can come last — lowest priority for MVP)
- `GET /conversations` — conversations for the current user
- `POST /conversations` — `{ petId, message }` → starts a new conversation
- `GET /conversations/{id}/messages`
- `POST /conversations/{id}/messages` — `{ messageText }`

---

Frontend code already calls all of #2–#5 in `src/api/pets.ts` (they just 404 until you build
them, and the UI falls back to sample data so pages stay reviewable in the meantime).
Reply here or drop the response shape in Slack if you want to change any field names — happy
to adjust the TypeScript types to match instead of the other way around.
