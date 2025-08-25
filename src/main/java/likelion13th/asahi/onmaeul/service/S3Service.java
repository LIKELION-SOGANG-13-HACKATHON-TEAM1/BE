package likelion13th.asahi.onmaeul.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;


@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucket;
    private final String cdnUrl; // CDN URL을 저장할 필드 추가

    // 생성자에서 cdn-url 값을 @Value로 주입받습니다.
    public S3Service(
            S3Client s3Client,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.s3.cdn-url}") String cdnUrl // cdn-url 주입
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.cdnUrl = cdnUrl;
    }

    /**
     * MultipartFile을 S3에 업로드하고, CDN의 URL을 반환합니다.
     * @param file 업로드할 파일
     * @return CDN을 통해 접근 가능한 파일의 전체 URL
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public String upload(MultipartFile file) throws IOException {
        // 1. 파일 유효성 검사 (기존과 동일)
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }

        // 2. 고유한 파일 키 생성 (기존과 동일)
        String uniqueKey = createUniqueKey(file.getOriginalFilename());

        // 3. S3에 업로드하기 위한 요청 객체 생성 (기존과 동일)
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(uniqueKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        // 4. S3에 파일 업로드 (기존과 동일)
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // 5. ✨ CDN URL을 사용하여 전체 URL을 생성하여 반환 (수정된 부분)
        return "https://" + cdnUrl + "/" + uniqueKey;
    }

    /**
     * 파일 이름이 겹치지 않도록 고유한 키를 생성합니다. (기존과 동일)
     * (예: 'image.png' -> 'UUID.png')
     * @param originalFilename 원본 파일 이름
     * @return 고유한 파일 키
     */
    private String createUniqueKey(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            // 원본 파일의 이름 대신 확장자만 사용하도록 수정하면 더 깔끔합니다.
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
