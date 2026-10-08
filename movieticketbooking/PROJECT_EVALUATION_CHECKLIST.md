# Project Evaluation Checklist

Every requirement is mapped to the real files that implement it (package `com.example.movieticketbooking`).

| # | Requirement | Where it is implemented | How to demonstrate |
|---|---|---|---|
| 1 | **Project concept & requirements** | `README.md` (overview, problem statement, objectives, features); working booking flow in `service/BookingService` | Walk through README sections 1–4, then run the Postman collection |
| 2 | **Spring Boot implementation** | `MovieticketbookingApplication` (`@SpringBootApplication`), `pom.xml` (Spring Boot parent 3.5, starters), `application.properties` | `./mvnw spring-boot:run` |
| 3 | **CRUD operations** | Movie: `MovieController` + `MovieService` (create, read, search, update, delete). Booking: `BookingController` + `BookingService` (create, read, update seats, cancel). User: `UserController` + `UserService` (register via `AuthController`, read, update, delete) | Postman folders 2, 3, 4 |
| 4 | **Database integration** | MySQL settings in `application.properties` (env-variable based), Spring Data JPA repositories in `repository/`, `ddl-auto=update` creates tables | Start app → tables appear in MySQL Workbench |
| 5 | **Entities & relationships** | `entity/User`, `Movie`, `Booking`, `Role`. `Booking` → `@ManyToOne User` and `@ManyToOne Movie`; `User`/`Movie` → `@OneToMany(mappedBy)`; `User` ↔ `Role` → `@ManyToMany` + `@JoinTable`. Details in `DATABASE_DESIGN.md` | Show `bookings` table with `user_id`, `movie_id` foreign keys |
| 6 | **REST APIs** | `controller/` (5 controllers); clean URLs `/api/movies`, `/api/bookings`, `/api/users`, `/api/auth`, `/api/admin`; `ResponseEntity` with 200/201/204/400/401/403/404/409 | README section 10, Postman |
| 7 | **Layered architecture** | Controller → Service → Repository → Database. Controllers never use repositories; business rules live in services; constructor injection everywhere | Open `BookingController` → `BookingService` → `BookingRepository` |
| 8 | **Validation & exception handling** | Validation annotations in `dto/*Request`; `@Valid` in controllers; `exception/GlobalExceptionHandler` (`@RestControllerAdvice`) + custom exceptions; JSON body `ApiError` | Postman folder 6 (400, 404, 409) |
| 9 | **Authentication & authorization** | `config/SecurityConfig` (URL rules, stateless, BCrypt bean), `security/JwtService`, `security/JwtAuthenticationFilter`, `security/CustomUserDetailsService`, `security/SecurityErrorHandler` (JSON 401/403), roles seeded in `config/DataInitializer`, ownership checks in `BookingService`/`UserService` | Login as user and admin, call `/api/admin/users` with both (403 vs 200) |
| 10 | **Testing** | JUnit/MockMvc integration tests (`AuthControllerTest`, `MovieControllerTest`, `BookingControllerTest`), Mockito unit test (`BookingServiceTest`), context test; `POSTMAN_API_TESTING.md` and `MovieTicketBooking.postman_collection.json` | `./mvnw test`; import the Postman collection |
| 11 | **Code quality** | Meaningful names, one package per layer, DTOs (records) instead of exposing entities, no passwords in responses, SLF4J logging (no `System.out`), no unused dependencies (Lombok, actuator, devtools removed), config via environment variables | Browse the package tree |

## Business rules (beyond plain CRUD)
- Total amount = `numberOfSeats × ticketPrice`, calculated in `BookingService.calculateTotal`; the client cannot send price or total.
- Seats are checked and reduced when booking, adjusted when updating, restored when cancelling.
- Movie row is locked while seats change (`MovieRepository.findByIdForUpdate`) to avoid overbooking.
- Users can only see/change their own bookings and profile; admins can see everything.

## Test coverage summary
| Area | Test class |
|---|---|
| Application context, seeded roles/movies | `MovieticketbookingApplicationTests` |
| Register, duplicate email, validation, login, wrong password, 401, 403, admin list, profile | `AuthControllerTest` |
| Movie create / get / update / delete, filter, search, validation, malformed JSON, 401, 403, 404 | `MovieControllerTest` |
| Booking create (server-side total), get, list, other user 403, update, cancel, validation, not enough seats, unknown movie, movie-with-bookings 409, admin list | `BookingControllerTest` |
| Price calculation and seat rule (mocked repositories) | `BookingServiceTest` |
