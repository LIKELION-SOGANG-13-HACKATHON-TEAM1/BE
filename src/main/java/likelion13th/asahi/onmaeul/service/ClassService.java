package likelion13th.asahi.onmaeul.service;

import likelion13th.asahi.onmaeul.domain.*;
import likelion13th.asahi.onmaeul.domain.Class;
import likelion13th.asahi.onmaeul.dto.response.ApiResponse;
import likelion13th.asahi.onmaeul.dto.response.clazz.ClazzItem;
import likelion13th.asahi.onmaeul.dto.response.clazz.ClazzPayload;
import likelion13th.asahi.onmaeul.dto.response.myPage.PagingInfo;
import likelion13th.asahi.onmaeul.repository.ClassRepository;
import likelion13th.asahi.onmaeul.util.CursorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.List;

import static likelion13th.asahi.onmaeul.dto.response.ApiResponse.ok;


@Service
@RequiredArgsConstructor
public class ClassService {
    private final ClassRepository classRepository;

    public ApiResponse<ClazzPayload> findList(String nextCursor, ClassStatus status, int limit) {
        List<Class> classes;
        final Pageable pageable = (Pageable) PageRequest.of(0, limit); //cursor를 기반하여 페이지 가져오기에 offset은 항상 0으로 설정
        if (status == null) { // status가 null이면 전체 조회
            if (nextCursor == null || nextCursor.isBlank()) {
                // 전체 데이터에서 첫 페이지 가져오기
                classes = classRepository.findTop5ByOrderByCreatedAtDescIdDesc(); // 새 메소드 호출
            } else {
                // 전체 데이터에서 다음 페이지 가져오기
                var c = CursorUtil.decode(nextCursor);
                classes = classRepository.findNextPage(c.createdAt(), c.id(), pageable); // 새 메소드 호출
            }
        } else {//status가 OPEN인 경우
            if (nextCursor == null || nextCursor.isBlank()) {
                //첫페이지 가져오기
                classes = classRepository.findTop5ByStatusOrderByCreatedAtDescIdDesc(status);
            } else {
                //두번째 이후 페이지 가져오기
                var c = CursorUtil.decode(nextCursor);
                classes = classRepository.findNextPageByStatus(status, c.createdAt(), c.id(), pageable);
            }
        }

        String newCursor = null;
        boolean hasNext = false;
        if (classes.size() == limit) {
            /*3개의 글이 나올 시 다음이 더 있다고 반환
            3개가 안되는 경우 마지막 페이지
             */

            var last = classes.get(classes.size() - 1); //classes 속 마지막 class 가져오기
            newCursor = CursorUtil.encode(last.getCreatedAt(), last.getId());
            hasNext = true;
            //newCursor이 null이 아니고 has_next이 true여야만 무한 스크롤 제공
        }

        PagingInfo paging = PagingInfo.builder()
                .all((nextCursor == null) && (hasNext == false))
                .count(classes.size())
                .next_cursor(newCursor)
                .has_next(hasNext)
                .build();

        List<ClazzItem> clazzItems = classes.stream()
                .map(e -> {
                    return ClazzItem.builder()
                            .id(e.getId())
                            .title(e.getTitle())
                            .hostName(e.getHost().getUsername())
                            .status(e.getStatus().toString())
                            .description(e.getDescription())
                            .build();
                }).toList();

        ClazzPayload clazzPayload = ClazzPayload.builder()
                .classes(clazzItems)
                .paging(paging)
                .build();


        return ok("class list 조회 성공 ", clazzPayload);
    }
    public ApiResponse<ClazzPayload> search(String keyword, int page, int size,ClassStatus status){
        //pageable 정보
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if(status == null){

        }
        //Repository에서 키워드로 데이터 조회(status OPEN만)
        else {
            Page<Class> classPage = classRepository.findByKeywordAndStatus(keyword, ClassStatus.OPEN, pageable);
            List<Class> classes = classPage.getContent();
        }
        List<ClazzItem> clazzItems= classes.stream()
                .map(e->{
                    return ClazzItem.builder()
                            .id(e.getId())
                            .title(e.getTitle())
                            .hostName(e.getHost().getUsername())
                            .status(e.getStatus().toString())
                            .description(e.getDescription())
                            .build();
                }).toList();

        PagingInfo paging=PagingInfo.builder()
                .all(classPage.hasNext() ==false)
                .count(classes.size())
                .next_cursor(null)
                .has_next(classPage.hasNext())
                .build();

        ClazzPayload clazzPayload=ClazzPayload.builder()
                .classes(clazzItems)
                .paging(paging)
                .build();

        return ok("class list 검색 성공",clazzPayload);
    }


}
