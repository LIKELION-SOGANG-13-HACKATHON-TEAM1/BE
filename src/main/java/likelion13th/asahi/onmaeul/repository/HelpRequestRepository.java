package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.HelpRequest;
import likelion13th.asahi.onmaeul.domain.HelpRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import java.util.List;

public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {
    //첫페이지 가져오기
    List<HelpRequest> findTop5ByStatusOrderByCreatedAtDescIdDesc(HelpRequestStatus status);

    //다음 페이지 가져오기
    @Query("""
    SELECT h
    FROM HelpRequest h
    WHERE h.status = :status
      AND (
           h.createdAt < :createdAt
        OR (h.createdAt = :createdAt AND h.id < :id)
      )
    ORDER BY h.createdAt DESC, h.id DESC
""")
    List<HelpRequest>findNextPageByStatus(@Param("status")HelpRequestStatus status, @Param("createdAt") OffsetDateTime createdAt, @Param("id")Long id, Pageable pageable);
}
