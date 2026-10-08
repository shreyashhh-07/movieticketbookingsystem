# Viva Preparation – Short Answers

1. **What is an Entity?** A Java class mapped to a database table with `@Entity`. Each object is one row. Example: `Movie` → `movies`.
2. **Why User, Movie and Booking?** A user books tickets for a movie. User and Movie are the real-world things; Booking connects them and stores seats, price and status.
3. **User ↔ Booking?** One-to-many: one user has many bookings, each booking belongs to one user.
4. **Movie ↔ Booking?** One-to-many: one movie has many bookings, each booking is for one movie.
5. **Why `@ManyToOne`?** Many bookings point to one user/movie. `Booking` is the "many" side, so it holds the foreign key.
6. **Where is `@OneToMany` used?** `User.bookings` and `Movie.bookings` with `mappedBy`; it is the inverse (read-only) side, no extra column.
7. **What is a foreign key?** A column that refers to the primary key of another table: `bookings.user_id → users.id`, `bookings.movie_id → movies.id`. It prevents bookings for non-existing users/movies.
8. **How does JPA map the relationship?** `@ManyToOne @JoinColumn(name="user_id")` tells Hibernate to create/use the `user_id` column and load the `User` row when needed.
9. **What does `JpaRepository` do?** Gives ready-made CRUD methods (`save`, `findById`, `findAll`, `delete`) and generates SQL for methods like `findByEmail`. We write only an interface.
10. **Why a Service layer?** Business rules (price calculation, seat check, ownership) live in one place, are reusable and testable; controllers stay thin.
11. **Why DTOs?** Control what the API accepts and returns: no password leak, no infinite JSON loops, client cannot set fields like `totalAmount`; the API can stay stable if entities change.
12. **How does validation work?** Annotations such as `@NotBlank`, `@Min` on DTOs + `@Valid` in the controller. On failure Spring throws `MethodArgumentNotValidException`, which `GlobalExceptionHandler` turns into a 400 JSON response with field messages.
13. **How does exception handling work?** Services throw custom exceptions (`ResourceNotFoundException`, ...). `@RestControllerAdvice` catches them and returns a uniform JSON error with the right status code.
14. **How does Spring Security work here?** Login checks the email/password (BCrypt hash comparison) and returns a JWT. For each later request `JwtAuthenticationFilter` reads the token, loads the user and roles, and puts them in the security context. `SecurityConfig` then checks the URL rules.
15. **How does role-based authorization work?** Roles are stored in the DB (`ROLE_USER`, `ROLE_ADMIN`). `SecurityConfig` uses `hasRole("ADMIN")` for `/api/admin/**` and movie writes → 403 for normal users. Ownership (own bookings only) is checked in the service.

**Extra questions you may get**
- *Why BCrypt?* One-way, salted hash; even the DB admin cannot read passwords.
- *Why 401 vs 403?* 401 = not logged in / bad token; 403 = logged in but not allowed.
- *Why is CSRF disabled?* The API is stateless with Bearer tokens (no cookies), so CSRF attacks do not apply.
- *Why does DELETE booking only cancel?* Booking history is kept; seats are returned to the movie.
- *Why lock the movie row?* So two users cannot book the same last seats simultaneously (`PESSIMISTIC_WRITE`).
