package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassRepository extends JpaRepository<Class, Long> {

    @Query("""
        SELECT c
        FROM Class c
        JOIN FETCH c.host h
        WHERE c.hostId = :hostId
          AND (:status IS NULL OR c.status = :status)
        ORDER BY c.schedule DESC
        """)
    List<Class> findAllByHostIdAndStatus(@Param("hostId") Long hostId, @Param("status") String status);
}


