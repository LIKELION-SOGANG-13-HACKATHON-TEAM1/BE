package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    /** 어르신이 요청한(=받은) 매칭 전체 -> 페이징 안쓸 때!! (최신순) */
    @Query("""
        SELECT m
        FROM Match m
        JOIN m.helpRequest hr
        WHERE hr.requester.id = :seniorId
        ORDER BY m.id DESC
        """)
    List<Match> findAllForSenior(@Param("seniorId") Long seniorId);

    /** 어르신이 요청한(=받은) 매칭 슬라이스 – id 커서 기반 (최신 → 과거) */
    @Query("""
        SELECT m
        FROM Match m
        JOIN m.helpRequest hr
        WHERE hr.requester.id = :seniorId
          AND (:cursorId IS NULL OR m.id < :cursorId)
        ORDER BY m.id DESC
        """)
    List<Match> findSliceForSenior(
            @Param("seniorId") Long seniorId,
            @Param("cursorId") Long cursorId,
            Pageable pageable   // size +1 로 호출해서 has_next 판단
    );

    @Query("""
        SELECT m
        FROM Match m
        WHERE m.id = :matchId
        AND m.responser.id = :seniorId
        """)
    Optional<Match> findDetailForSenior(
            @Param("matchId") Long matchId,
            @Param("seniorId") Long seniorId
    );
}
