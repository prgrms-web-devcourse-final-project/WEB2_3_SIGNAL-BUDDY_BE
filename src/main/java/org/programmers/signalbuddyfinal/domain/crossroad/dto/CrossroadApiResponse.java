package org.programmers.signalbuddyfinal.domain.crossroad.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.crossroad.entity.Crossroad;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CrossroadApiResponse {

    @JsonProperty("itstId")
    private String crossroadApiId;

    @JsonProperty("itstNm")
    private String name;

    @JsonProperty("mapCtptIntLat")
    private Double lat; // 위도

    @JsonProperty("mapCtptIntLot")
    private Double lng; // 경도

    public CrossroadApiResponse(Crossroad crossroad) {
        this.crossroadApiId = crossroad.getCrossroadApiId();
        this.name = crossroad.getName();
        this.lat = crossroad.getCoordinate().getY();
        this.lng = crossroad.getCoordinate().getX();
    }

    public Crossroad toEntity() {
        return Crossroad.create()
            .crossroadApiId(this.crossroadApiId)
            .name(this.name)
            .lat(this.lat).lng(this.lng)
            .build();
    }
}
