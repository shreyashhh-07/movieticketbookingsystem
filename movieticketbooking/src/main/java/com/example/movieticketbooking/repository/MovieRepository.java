package com.example.movieticketbooking.repository;

import com.example.movieticketbooking.entity.Movie;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    /** All three arguments are LIKE patterns (the service passes "%" when a filter is not used). */
    @Query("""
            select m from Movie m
            where lower(m.title) like :title
              and lower(m.genre) like :genre
              and lower(m.language) like :language
            order by m.title
            """)
    List<Movie> search(@Param("title") String title,
                       @Param("genre") String genre,
                       @Param("language") String language);

    /** Locks the row so two users cannot book the last seats at the same time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Movie m where m.id = :id")
    Optional<Movie> findByIdForUpdate(@Param("id") Long id);
}
