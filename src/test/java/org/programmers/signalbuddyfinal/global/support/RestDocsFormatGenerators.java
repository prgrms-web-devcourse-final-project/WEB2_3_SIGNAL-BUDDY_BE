package org.programmers.signalbuddyfinal.global.support;

import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import com.epages.restdocs.apispec.HeaderDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

/**
 * 요청 값의 양식을 설정
 */
public final class RestDocsFormatGenerators {

    public static FieldDescriptor[] commonResponseFormat() {
        FieldDescriptor[] commonDocs = new FieldDescriptor[3];
        commonDocs[0] = fieldWithPath("status").type(JsonFieldType.STRING)
            .description("응답 상태");
        commonDocs[1] = fieldWithPath("message").type(JsonFieldType.NULL)
            .description("응답 메시지");
        commonDocs[2] = fieldWithPath("data").type(JsonFieldType.VARIES)
            .description("응답 데이터");
        return commonDocs;
    }

    public static FieldDescriptor[] commonErrorFormat() {
        FieldDescriptor[] commonDocs = new FieldDescriptor[2];
        commonDocs[0] = fieldWithPath("status").type(JsonFieldType.STRING)
            .description("응답 상태");
        commonDocs[1] = fieldWithPath("message").type(JsonFieldType.STRING)
            .description("응답 메시지");
        return commonDocs;
    }

    public static FieldDescriptor[] pageResponseFormat() {
        FieldDescriptor[] commonDocs = commonResponseFormat();

        FieldDescriptor[] pageDocs = new FieldDescriptor[7];
        pageDocs[0] = fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER)
            .description("전체 댓글의 개수");
        pageDocs[1] = fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER)
            .description("전체 페이지 수");
        pageDocs[2] = fieldWithPath("data.currentPageNumber").type(JsonFieldType.NUMBER)
            .description("현재 페이지 (0 페이지부터 시작)");
        pageDocs[3] = fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER)
            .description("한 페이지 당 데이터의 수");
        pageDocs[4] = fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN)
            .description("다음 페이지의 존재 여부");
        pageDocs[5] = fieldWithPath("data.hasPrevious").type(JsonFieldType.BOOLEAN)
            .description("이전 페이지의 존재 여부");
        pageDocs[6] = fieldWithPath("data.searchResults").type(JsonFieldType.ARRAY)
            .description("검색 결과 목록");

        return Stream.concat(Arrays.stream(commonDocs), Arrays.stream(pageDocs))
            .toArray(FieldDescriptor[]::new);
    }

    public static FieldDescriptor[] pageResponseWithMemberFormat() {
        FieldDescriptor[] pageDocs = pageResponseFormat();

        FieldDescriptor[] memberDocs = new FieldDescriptor[7];
        memberDocs[0] = fieldWithPath("data.searchResults[].member")
            .type(JsonFieldType.OBJECT)
            .description("작성자 정보");
        memberDocs[1] = fieldWithPath("data.searchResults[].member.memberId")
            .type(JsonFieldType.NUMBER)
            .description("작성자 ID(PK)");
        memberDocs[2] = fieldWithPath("data.searchResults[].member.email")
            .type(JsonFieldType.STRING)
            .description("작성자의 이메일");
        memberDocs[3] = fieldWithPath("data.searchResults[].member.nickname")
            .type(JsonFieldType.STRING)
            .description("작성자의 닉네임");
        memberDocs[4] = fieldWithPath("data.searchResults[].member.profileImageUrl")
            .type(JsonFieldType.STRING)
            .description("작성자의 프로필 이미지 URL");
        memberDocs[5] = fieldWithPath("data.searchResults[].member.role")
            .type(JsonFieldType.STRING)
            .description("""
                작성자의 권한
                - USER : 일반 사용자
                - ADMIN : 관리자
                """);
        memberDocs[6] = fieldWithPath("data.searchResults[].member.memberStatus")
            .type(JsonFieldType.STRING)
            .description("""
                작성자의 탈퇴 여부
                - ACTIVITY : 활동 상태
                - WITHDRAWAL : 탈퇴 상태
                """);

        return Stream.concat(Arrays.stream(pageDocs), Arrays.stream(memberDocs))
            .toArray(FieldDescriptor[]::new);
    }

    public static HeaderDescriptorWithType jwtFormat() {
        return headerWithName(HttpHeaders.AUTHORIZATION).type(SimpleType.STRING)
            .description("JWT");
    }

    public static String getTokenExample() {
        return "Bearer Your_Token";
    }

    /**
     * 해당 formName에 첨부할 Mock Image MultipartFile 반환
     *
     * @param formName Multipart Form Field Name
     * @return MockMultipartFile
     */
    public static MockMultipartFile getMockImageFile(String formName) {
        return new MockMultipartFile(
            formName,
            "image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image".getBytes()
        );
    }

    public static Schema commonResponse = schema("CommonResponse");
}
