package org.programmers.signalbuddyfinal.domain.weather.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class WeatherResponse {

    private Double temperature;
    private Double humidity;
    private Double precipitation;
    private String precipitationType;
    private Integer windDirection;
    private Double windSpeed;
}

/*
T1H
기온
℃
10

RN1
1시간 강수량
mm
8

UUU
동서바람성분
m/s
12

VVV
남북바람성분
m/s
12

REH
습도
%
8

PTY
강수형태
코드값
4

VEC
풍향
deg
10

WSD
풍속
m/s
10

 */