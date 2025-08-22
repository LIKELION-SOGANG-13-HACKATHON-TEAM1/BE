package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.ClassStatus;
import likelion13th.asahi.onmaeul.domain.HelpRequest;
import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import likelion13th.asahi.onmaeul.domain.Class;

import java.time.OffsetDateTime;
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

    //status PENDING 첫 페이지
    List<Class> findTop5ByStatusOrderByCreatedAtDescIdDesc(ClassStatus status);

    //status PENDING 후속페이지
    @Query("""
    SELECT c
    FROM Class c
    WHERE c.status = :status
      AND (
           c.createdAt < :createdAt
        OR (c.createdAt = :createdAt AND c.id < :id)
      )
    ORDER BY c.createdAt DESC, c.id DESC
""")
    List<Class>findNextPageByStatus(@Param("status")ClassStatus status, @Param("createdAt") OffsetDateTime createdAt, @Param("id")Long id, Pageable pageable);

    //status null 첫 페이지
    List<Class> findTop5ByOrderByCreatedAtDescIdDesc();

    //status null 후속 페이지
    @Query("SELECT c FROM Class c WHERE c.createdAt < :createdAt OR (c.createdAt = :createdAt AND c.id < :id) ORDER BY c.createdAt DESC, c.id DESC")
    List<Class> findNextPage(@Param("createdAt") OffsetDateTime createdAt, @Param("id") Long id, Pageable pageable);
}
}


