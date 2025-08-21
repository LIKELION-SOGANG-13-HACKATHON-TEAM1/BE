package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
        SELECT r
        FROM Review r
        JOIN r.match m
        WHERE m.responser.id = :juniorId
        """)
    List<Review> findAllByJuniorId(@Param("juniorId") Long juniorId); // responser ID로 리뷰 목록을 조회하는 메소드

    @Query("""
        SELECT r
        FROM Review r
        JOIN FETCH r.match m
        JOIN FETCH m.helpRequest hr
        JOIN FETCH hr.requester
        LEFT JOIN FETCH m.review
        WHERE r.id = :reviewId
          AND m.responser.id = :juniorId
        """)
    Optional<Review> findReviewDetail(@Param("reviewId") Long reviewId, @Param("juniorId") Long juniorId);
}

