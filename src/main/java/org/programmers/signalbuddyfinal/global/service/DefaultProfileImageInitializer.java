package org.programmers.signalbuddyfinal.global.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultProfileImageInitializer {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("classpath:static/images/member/profile-icon.png")
    private Resource defaultImageResource;

    @Value("${default.profile.image.path}")
    private String key;

    @Value("${cloud.aws.s3.folder.member}")
    private String memberDir;

    /**
     * 서버 초기화 후 기본 프로필 이미지의 존재 여부를 확인하고, 존재하지 않을 경우 로컬 리소스에서 이미지를 S3에 업로드.
     */
    @PostConstruct
    public void initDefaultProfileImage() {
        if (!isImageExistInS3()) {
            log.info("기본 프로필 이미지가 S3에 존재하지 않습니다. 업로드를 시작합니다.");
            uploadDefaultImageToS3();
        } else {
            log.info("기본 프로필 이미지가 이미 S3에 존재합니다.");
        }
    }


    /**
     * S3에 키를 가진 객체가 존재하는지 확인합니다.
     *
     * @return 객체가 존재하면 true, 그렇지 않으면 false
     */
    private boolean isImageExistInS3() {
        try {
            s3Client.headObject(
                HeadObjectRequest.builder().bucket(bucket).key(memberDir + key).build());
            return true;
        } catch (S3Exception e) {
            log.warn("S3에서 기본 프로필 이미지 조회 실패: {}", e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    /**
     * 로컬 리소스에 있는 기본 이미지를 S3에 업로드.
     */
    private void uploadDefaultImageToS3() {
        try (InputStream is = defaultImageResource.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket)
                .key(memberDir + key).contentType("image/png").build();

            s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(is, defaultImageResource.contentLength()));
            log.info("기본 프로필 이미지를 S3에 업로드하였습니다.");
        } catch (IOException e) {
            log.error("로컬 기본 이미지 로딩 실패", e);
        } catch (S3Exception e) {
            log.error("S3 업로드 실패: {}", e.awsErrorDetails().errorMessage(), e);
        }
    }
}
