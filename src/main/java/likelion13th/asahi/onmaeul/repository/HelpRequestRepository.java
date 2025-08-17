package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.HelpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {
    List<HelpRequest> findTop5ByStatusOrderByCreatedAtDescIdDesc(String status);
    List<HelpRequest> findTop5ByStatusAndIdLessThanOrderByCreatedAtDescIdDesc(String status, Long lastId);
}
