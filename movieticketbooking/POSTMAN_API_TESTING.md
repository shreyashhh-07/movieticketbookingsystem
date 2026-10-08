# API Testing with Postman

**Base URL:** `http://localhost:8080`

Quick way: import `MovieTicketBooking.postman_collection.json` and run the folders 1 → 6 in order (tokens and ids are saved automatically).
Manual requests are listed below. For protected requests add the header `Authorization: Bearer <token>` (Postman: Authorization tab → Bearer Token).
All JSON requests need `Content-Type: application/json`.

Default admin: `admin@movie.com` / `Admin@123`.

---

## 1. Register a user
- **POST** `http://localhost:8080/api/auth/register`
- Headers: `Content-Type: application/json`
- Body:
```json
{ "name": "Rahul Sharma", "email": "rahul@example.com", "password": "secret123", "phone": "9876543210" }
```
- Expected: **201 Created**
```json
{ "id": 2, "name": "Rahul Sharma", "email": "rahul@example.com", "phone": "9876543210", "roles": ["ROLE_USER"], "createdAt": "..." }
```
(no password in the response)

## 2. Login as user
- **POST** `http://localhost:8080/api/auth/login`
- Body: `{ "email": "rahul@example.com", "password": "secret123" }`
- Expected: **200 OK**
```json
{ "token": "eyJhbGciOi...", "tokenType": "Bearer", "user": { "id": 2, "email": "rahul@example.com", "roles": ["ROLE_USER"], "...": "..." } }
```
Copy the `token` → this is the **user token**.

## 3. Login as admin
- **POST** `http://localhost:8080/api/auth/login`
- Body: `{ "email": "admin@movie.com", "password": "Admin@123" }`
- Expected: **200 OK**, `user.roles` = `["ROLE_ADMIN", "ROLE_USER"]`. Copy the token → **admin token**.

## 4. Create movie (admin)
- **POST** `http://localhost:8080/api/movies`
- Headers: `Authorization: Bearer <admin token>`, `Content-Type: application/json`
- Body:
```json
{ "title": "Interstellar", "genre": "Sci-Fi", "language": "English", "duration": 169, "rating": 8.7,
  "ticketPrice": 300.00, "description": "A team travels through a wormhole.", "releaseDate": "2014-11-07", "availableSeats": 80 }
```
- Expected: **201 Created** with the movie including its generated `id` (use it below as `{movieId}`).

## 5. Get movies (public – no token)
- **GET** `http://localhost:8080/api/movies` → **200 OK**, array of movies
- Filter: `GET /api/movies?genre=Sci-Fi` and `GET /api/movies?language=Hindi`
- Search: `GET /api/movies/search?title=incep`

## 6. Get movie by ID (public)
- **GET** `http://localhost:8080/api/movies/{movieId}` → **200 OK**
- Unknown id: `GET /api/movies/999999` → **404 Not Found**
```json
{ "timestamp": "...", "status": 404, "error": "Not Found", "message": "Movie with id 999999 not found", "path": "/api/movies/999999" }
```

## 7. Update movie (admin)
- **PUT** `http://localhost:8080/api/movies/{movieId}`
- Headers: `Authorization: Bearer <admin token>`
- Body: same fields as create, e.g. `"title": "Interstellar (IMAX)", "ticketPrice": 350.00, "availableSeats": 90`
- Expected: **200 OK** with the updated movie.

## 8. Delete movie (admin)
- **DELETE** `http://localhost:8080/api/movies/{movieId}`
- Headers: `Authorization: Bearer <admin token>`
- Expected: **204 No Content** (body empty).
- If the movie already has bookings: **409 Conflict** – "Movie with id N has bookings and cannot be deleted".

## 9. Create booking (user)
- **POST** `http://localhost:8080/api/bookings`
- Headers: `Authorization: Bearer <user token>`, `Content-Type: application/json`
- Body: `{ "movieId": {movieId}, "numberOfSeats": 3 }`
- Expected: **201 Created**
```json
{ "id": 1, "userId": 2, "userName": "Rahul Sharma", "movieId": 4, "movieTitle": "Interstellar",
  "numberOfSeats": 3, "ticketPrice": 300.00, "totalAmount": 900.00, "status": "CONFIRMED", "bookingTime": "..." }
```
`totalAmount` = 3 × 300 is calculated by the server; any `totalAmount` sent by the client is ignored.
Check `GET /api/movies/{movieId}` → `availableSeats` is now 77.

## 10. Get booking(s) (user)
- **GET** `http://localhost:8080/api/bookings` → **200 OK**, your bookings (newest first)
- **GET** `http://localhost:8080/api/bookings/{bookingId}` → **200 OK**
- Another user's booking → **403 Forbidden**; unknown id → **404 Not Found**.

## 11. Update booking (user)
- **PUT** `http://localhost:8080/api/bookings/{bookingId}`
- Headers: `Authorization: Bearer <user token>`
- Body: `{ "numberOfSeats": 4 }`
- Expected: **200 OK**, `numberOfSeats` = 4, `totalAmount` = 1200.00, movie seats adjusted.

## 12. Cancel booking (user)
- **DELETE** `http://localhost:8080/api/bookings/{bookingId}`
- Headers: `Authorization: Bearer <user token>`
- Expected: **204 No Content**. A following `GET /api/bookings/{bookingId}` shows `"status": "CANCELLED"` and the seats are returned to the movie.
- Cancelling again → **400 Bad Request**.

## 13. Profile and admin views
- **GET** `/api/users/me` (user token) → **200 OK**
- **PUT** `/api/users/me` (user token), body `{ "name": "Rahul S.", "phone": "9123456780" }` → **200 OK**
- **GET** `/api/admin/users` (admin token) → **200 OK**, list of users (no passwords)
- **GET** `/api/admin/bookings` (admin token) → **200 OK**, all bookings

---

## Unauthorized / forbidden examples

| Request | Expected |
|---|---|
| `GET /api/bookings` with **no** Authorization header | **401 Unauthorized** |
| `GET /api/bookings` with `Authorization: Bearer abc.def.ghi` | **401 Unauthorized** |
| `POST /api/auth/login` with a wrong password | **401 Unauthorized** – "Invalid email or password" |
| `POST /api/movies` with the **user** token | **403 Forbidden** |
| `DELETE /api/movies/1` with the **user** token | **403 Forbidden** |
| `GET /api/admin/users` with the **user** token | **403 Forbidden** |
| `GET /api/bookings/{id}` of another user's booking | **403 Forbidden** |

401 body example:
```json
{ "timestamp": "...", "status": 401, "error": "Unauthorized",
  "message": "Authentication required: send a valid 'Authorization: Bearer <token>' header", "path": "/api/bookings" }
```

## Validation failure examples

**Invalid movie** – `POST /api/movies` (admin token)
```json
{ "title": "", "genre": "Drama", "language": "English", "duration": 0, "rating": 11, "ticketPrice": -5, "availableSeats": -1 }
```
→ **400 Bad Request**
```json
{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "Validation failed", "path": "/api/movies",
  "validationErrors": {
    "title": "Title is required",
    "duration": "Duration must be greater than 0",
    "rating": "Rating must be at most 10",
    "ticketPrice": "Ticket price must be greater than 0",
    "availableSeats": "Available seats cannot be negative" } }
```

**Invalid registration** – `{ "name": "", "email": "bad", "password": "1" }` → **400** with `validationErrors` for `name`, `email`, `password`.

**Booking with 0 seats** – `{ "movieId": 1, "numberOfSeats": 0 }` → **400**, `validationErrors.numberOfSeats`.

**More seats than available** – `{ "movieId": 1, "numberOfSeats": 10 }` on a movie with 2 seats left → **400**, message "Only 2 seat(s) available for '...'".

**Duplicate email** – registering `rahul@example.com` twice → **409 Conflict**.

**Booking an unknown movie** – `{ "movieId": 999999, "numberOfSeats": 1 }` → **404 Not Found**.

**Malformed JSON** – body `{ this is not json` → **400 Bad Request**.
