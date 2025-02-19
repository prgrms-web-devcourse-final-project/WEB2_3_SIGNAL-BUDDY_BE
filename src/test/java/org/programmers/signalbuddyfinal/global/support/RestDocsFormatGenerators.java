package org.programmers.signalbuddyfinal.global.support;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

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
        commonDocs[2] = fieldWithPath("data").type(JsonFieldType.OBJECT)
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
}
