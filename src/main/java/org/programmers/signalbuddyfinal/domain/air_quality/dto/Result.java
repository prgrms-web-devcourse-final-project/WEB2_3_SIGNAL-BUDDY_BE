package org.programmers.signalbuddyfinal.domain.air_quality.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;

@Getter
@JacksonXmlRootElement(localName = "RESULT")
public class Result {

    @JsonProperty("CODE")
    @JacksonXmlProperty(localName = "CODE")
    private String code;

    @JsonProperty("MESSAGE")
    @JacksonXmlProperty(localName = "MESSAGE")
    private String message;
}
