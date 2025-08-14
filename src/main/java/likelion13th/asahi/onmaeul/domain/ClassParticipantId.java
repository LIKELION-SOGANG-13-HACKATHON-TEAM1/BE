package likelion13th.asahi.onmaeul.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ClassParticipantId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long classId;
}