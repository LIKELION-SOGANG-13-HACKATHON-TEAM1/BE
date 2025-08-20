package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
        SELECT r
        FROM Review r
        JOIN r.match m
        WHERE m.responser.id = :juniorId
        """)
    List<Review> findAllByJuniorId(@Param("juniorId") Long juniorId); // responser ID로 리뷰 목록을 조회하는 메소드
}
