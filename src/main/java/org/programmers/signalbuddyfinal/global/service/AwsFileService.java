package org.programmers.signalbuddyfinal.global.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.exception.GlobalErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsFileService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에서 파일을 가져옵니다.
     *
     * @param filename S3에 저장된 파일명
     * @param dir      S3 저장 폴더
     * @return Resource 객체
     * @throws IllegalStateException 유효하지 않은 URL 또는 읽을 수 없는 파일인 경우
     */
    public Resource getFileFromS3(String filename, String dir) {
        final String filePath = dir + filename;
        final URL url = generateFileUrl(filePath);

        try {
            final UrlResource resource = createUrlResource(url);

            if (!resource.exists() || !resource.isReadable()) {
                log.error("유효하지 않은 파일: {}", filePath);
                throw new BusinessException(
                    GlobalErrorCode.FILE_LOAD_ERROR_NOT_EXIST_FILE);
            }

            return resource;
        } catch (MalformedURLException e) {
            log.error("URL 생성 실패: {}", filePath, e);
            throw new BusinessException(GlobalErrorCode.FILE_LOAD_ERROR_INVALID_URL);
        }
    }

    /**
     * S3에 파일을 저장합니다.
     *
     * @param file 업로드할 파일
     * @param dir  S3 저장 폴더
     * @return 저장된 파일 이름
     * @throws IllegalStateException 파일 업로드 중 오류가 발생한 경우
     */
    public String uploadFileToS3(MultipartFile file, String dir) {
        final String uniqueFilename = UUID.randomUUID() + "-" + file.getOriginalFilename();
        final String key = dir + uniqueFilename;

        try (InputStream inputStream = file.getInputStream()) {
            uploadToS3(key, inputStream, file.getContentType(), file.getSize());
            return uniqueFilename;
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
            throw new BusinessException(GlobalErrorCode.FILE_UPLOAD_FAILURE);
        }
    }

    /**
     * S3 URL 생성
     *
     * @param key S3 객체 키
     * @return URL 객체
     */
    private URL generateFileUrl(String key) {
        final GetUrlRequest request = GetUrlRequest.builder().bucket(bucket).key(key).build();
        return s3Client.utilities().getUrl(request);
    }

    /**
     * S3에 파일 업로드
     *
     * @param key         S3 객체 키
     * @param inputStream 업로드할 파일의 InputStream
     * @param contentType 파일의 Content-Type
     * @param size        파일 크기
     * @throws IllegalStateException S3에 파일 업로드 중 오류가 발생한 경우
     */
    private void uploadToS3(String key, InputStream inputStream, String contentType, long size) {
        try {
            final PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket)
                .key(key).contentType(contentType).build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));
        } catch (Exception e) {
            log.error("파일 업로드 실패: {}", key, e);
            throw new BusinessException(GlobalErrorCode.S3_UPLOAD_FAILURE);
        }
    }

    /**
     * UrlResource 생성 (테스트를 위한 메서드 분리)
     *
     * @param url 생성할 URL
     * @return UrlResource 객체
     * @throws MalformedURLException URL이 잘못된 경우.
     */
    protected UrlResource createUrlResource(URL url) throws MalformedURLException {
        return new UrlResource(url);
    }
}
