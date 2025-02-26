package org.programmers.signalbuddyfinal.global.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class DefaultProfileImageInitializerTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private Resource defaultImageResource;

    @InjectMocks
    private DefaultProfileImageInitializer initializer;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(initializer, "bucket", "test-bucket");
        ReflectionTestUtils.setField(initializer, "defaultImageResource", defaultImageResource);
        ReflectionTestUtils.setField(initializer, "key", "key");
        ReflectionTestUtils.setField(initializer, "memberDir", "memberDir");
    }

    @DisplayName("S3에 기본 이미지가 존재하는 경우 업로드 실행되지 않아야 함")
    @Test
    public void initDefaultImageWhenImageExists() {
        HeadObjectResponse dummyResponse = HeadObjectResponse.builder().build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(dummyResponse);

        initializer.initDefaultProfileImage();

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @DisplayName("S3에 기본 이미지가 존재하지 않으면 기본 이미지 업로드 실행")
    @Test
    public void initDefaultImageWhenImageNotExist() throws IOException {
        // S3Exception을 발생시켜 S3에 이미지가 없음을 가정
        S3Exception s3Exception = (S3Exception) S3Exception.builder().message("Not Found")
            .awsErrorDetails(AwsErrorDetails.builder().errorMessage("Not Found").build()).build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(s3Exception);

        byte[] dummyData = "dummy image data".getBytes();
        InputStream dummyInputStream = new ByteArrayInputStream(dummyData);
        when(defaultImageResource.getInputStream()).thenReturn(dummyInputStream);
        when(defaultImageResource.contentLength()).thenReturn((long) dummyData.length);

        initializer.initDefaultProfileImage();

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}