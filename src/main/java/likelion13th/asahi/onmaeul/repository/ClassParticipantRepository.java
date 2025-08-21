package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.ClassParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassParticipantRepository extends JpaRepository<ClassParticipant, Long> {

    @Query("""
        SELECT cp
        FROM ClassParticipant cp
        JOIN FETCH cp.classes cls
        JOIN FETCH cls.host h
        WHERE cp.user.id = :userId
          AND (:status IS NULL OR cp.status = :status)
        ORDER BY cp.matchedAt DESC
        """)
List<ClassParticipant> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}