package org.programmers.signalbuddyfinal.global.support;

import static com.epages.restdocs.apispec.Schema.schema;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

import com.epages.restdocs.apispec.Schema;
import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;

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

        // commonDocs, pageDocs 두 배열 합치기
        return Stream.concat(Arrays.stream(commonDocs), Arrays.stream(pageDocs))
            .toArray(FieldDescriptor[]::new);
    }

    public static Attributes.Attribute phoneNumberFormat() {
        return key("format").value("000-0000-0000");
    }

    public static Attributes.Attribute emailFormat() {
        return key("format").value("example-email@example.com");
    }

    public static Attributes.Attribute imageUrlFormat() {
        return key("format").value("https://example-image-url.com");
    }

    public static Attributes.Attribute tokenFormat() {
        return key("format").value("Bearer YOUR_TOKEN");
    }

    public static Attributes.Attribute encodingFormat() {
        return key("format").value("URL 인코딩 필수");
    }

    public static Attributes.Attribute pageFormat() {
        return  key("format").value("0부터 시작");
    }

    public static Schema commonResponse = schema("CommonResponse");
}
