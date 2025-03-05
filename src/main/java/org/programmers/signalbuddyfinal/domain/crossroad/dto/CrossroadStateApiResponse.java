package org.programmers.signalbuddyfinal.domain.crossroad.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CrossroadStateApiResponse {

    @JsonProperty("itstId")
    private String crossroadApiId;

    // UTC 시간 (timestamp 형식)
    @JsonProperty("trsmUtcTime")
    private Long transTimestamp;

    // e,w,s,n,ne,nw,se,sw 8방위
    // p : 사람, rs: remain second (남은 시간), sn: state name (상태 이름)

    @JsonProperty("ntPdsgRmdrCs")
    private Integer northTimeLeft;

    @JsonProperty("etPdsgRmdrCs")
    private Integer eastTimeLeft;

    @JsonProperty("stPdsgRmdrCs")
    private Integer southTimeLeft;

    @JsonProperty("wtPdsgRmdrCs")
    private Integer westTimeLeft;

    @JsonProperty("nePdsgRmdrCs")
    private Integer northeastTimeLeft;

    @JsonProperty("nwPdsgRmdrCs")
    private Integer northwestTimeLeft;

    @JsonProperty("swPdsgRmdrCs")
    private Integer southwestTimeLeft;

    @JsonProperty("sePdsgRmdrCs")
    private Integer southeastTimeLeft;

    @JsonProperty("ntPdsgStatNm")
    private SignalState northState;

    @JsonProperty("etPdsgStatNm")
    private SignalState eastState;

    @JsonProperty("wtPdsgStatNm")
    private SignalState westState;

    @JsonProperty("stPdsgStatNm")
    private SignalState southState;

    @JsonProperty("nePdsgStatNm")
    private SignalState northeastState;

    @JsonProperty("nwPdsgStatNm")
    private SignalState northwestState;

    @JsonProperty("sePdsgStatNm")
    private SignalState southeastState;

    @JsonProperty("swPdsgStatNm")
    private SignalState southwestState;

}
