package likelion13th.asahi.onmaeul.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="ClassParticipant")
@Getter
@NoArgsConstructor(access=AccessLevel.PROTECTED )
public class ClassParticipant {

    @EmbeddedId
    private ClassParticipantId id;

    //FK->Class(id)
    @ManyToOne(fetch=FetchType.LAZY)
    @MapsId("classId")
    @JoinColumn(name="class_id",nullable=false)
    private Class clazz; //변수명 class 불가능하기에 clazz로 선언

    //FK->User(id)
    @ManyToOne(fetch= FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name ="user_id",nullable=false)
    private User user;

    public static ClassParticipant create(User user, Class clazz) {
        //개발 하면서 제약 추가
        ClassParticipant cp = new ClassParticipant();
        cp.user = user;
        cp.clazz = clazz;
        cp.id = new ClassParticipantId(user.getId(), clazz.getId());
        return cp;
    }
}