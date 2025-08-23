package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.Class;
import likelion13th.asahi.onmaeul.domain.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


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

    //status OPEN 첫 페이지
    List<Class> findTop5ByStatusOrderByCreatedAtDescIdDesc(ClassStatus status);

    //status OPEN 후속페이지
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

    //class 검색하기(status OPEN)
    @Query("SELECT c FROM Class c WHERE c.status = :status AND (c.title LIKE CONCAT('%', :keyword, '%') OR c.description LIKE CONCAT('%', :keyword, '%')) ORDER BY c.createdAt DESC")
    Page<Class> findByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") ClassStatus classStatus, Pageable pageable);

    //class 검색하기(status null)
    @Query("SELECT c FROM Class c WHERE c.title LIKE CONCAT('%', :keyword, '%') OR c.description LIKE CONCAT('%', :keyword, '%') ORDER BY c.createdAt DESC")
    Page<Class> findByKeyword(String keyword, Pageable pageable);
}


