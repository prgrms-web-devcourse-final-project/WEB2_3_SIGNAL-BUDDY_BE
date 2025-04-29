package org.programmers.signalbuddyfinal.domain.trafficSignal.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.WKBWriter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficFileResponse;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@Transactional
public class TrafficCsvServiceTest extends ServiceTest {

    @InjectMocks
    private TrafficCsvService trafficCsvService;

    @Mock
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    private File testCsvFile;
    private String fileName;

    @BeforeEach
    void setUp() throws URISyntaxException {

        fileName = "seoul_traffic_light.csv";

        URL resource = getClass().getClassLoader().getResource("response/"+fileName);
        assertThat(resource).isNotNull();
        testCsvFile = new File(resource.toURI());
    }

    @Test
    void csvSaveMethodTest(){
        doReturn(new int[]{1, 1})
            .when(namedJdbcTemplate)
            .batchUpdate(anyString(), any(MapSqlParameterSource[].class));

        trafficCsvService.saveCsvData(fileName);

        verify(namedJdbcTemplate, atLeastOnce()).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
    }

    @Test
    @DisplayName("데이터 리더 처리 검증 및 메소드 호출")
    void csvParsingTest() throws IOException {
        // given
        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testCsvFile)));

        CsvToBean<TrafficFileResponse> csvToBean = new CsvToBeanBuilder<TrafficFileResponse>(reader)
                .withType(TrafficFileResponse.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        // when
        List<TrafficFileResponse> traffics = csvToBean.parse();

        traffics.forEach(traffic -> {
            System.out.println("serialNumber: " + traffic.getSerial());
            System.out.println("district: " + traffic.getDistrict());
            System.out.println("signalType: " + traffic.getSignalType());
            System.out.println("lat: " + traffic.getLat());
            System.out.println("lng: " + traffic.getLng());
            System.out.println("address: " + traffic.getAddress());
        });

        // then
        assertThat(traffics).isNotEmpty();
        assertThat(traffics.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("데이터 매핑 점검")
    void csvParsingDtoTest() {
        // given
        String csvContent = """
                serial,district,signalType,lat,lng,address
                123, 서울, 교차로, 37.5665, 126.9780, 서울시 중구
                124, 부산, 신호등, 35.179, 129.0756, 부산시 해운대구
                """;

        BufferedReader reader = new BufferedReader(new StringReader(csvContent));

        // when
        CsvToBean<TrafficFileResponse> csvBean = new CsvToBeanBuilder<TrafficFileResponse>(reader)
                .withType(TrafficFileResponse.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

        List<TrafficFileResponse> trafficList = csvBean.parse();

        //then
        System.out.println(trafficList.get(0).getSerial());
        System.out.println(trafficList.get(1).getSerial());
        assertThat(trafficList.get(0).getSerial()).isEqualTo(123L);

    }

}
