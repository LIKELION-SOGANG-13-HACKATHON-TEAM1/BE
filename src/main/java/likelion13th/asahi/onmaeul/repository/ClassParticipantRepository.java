package likelion13th.asahi.onmaeul.repository;

import likelion13th.asahi.onmaeul.domain.ClassParticipant;
import likelion13th.asahi.onmaeul.domain.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassParticipantRepository extends JpaRepository<ClassParticipant, Long> {

    @Query("""
        SELECT cp
        FROM ClassParticipant cp
        JOIN FETCH cp.clazz c
        JOIN FETCH c.host h
        WHERE cp.user.id = :userId
          AND c.status = :status
        """)
    List<ClassParticipant> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ClassStatus status); // 파라미터 타입을 ClassStatus로 수정

    @Query("""
      select cp
      from ClassParticipant cp
      join fetch cp.clazz c
      join fetch c.host h
      where cp.user.id = :userId
      order by c.schedule asc, c.id asc
      """)
    List<ClassParticipant> findAllWithClassByUser(@Param("userId") Long userId);

}