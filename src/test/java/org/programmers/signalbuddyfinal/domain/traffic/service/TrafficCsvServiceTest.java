package org.programmers.signalbuddyfinal.domain.traffic.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficFileResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRepository;
import org.programmers.signalbuddyfinal.domain.trafficSignal.service.TrafficCsvService;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Transactional
public class TrafficCsvServiceTest extends ServiceTest {

    @Autowired
    private TrafficRepository trafficRepository;

    @Autowired
    private TrafficCsvService trafficCsvService;

    private File testCsvFile;

    @BeforeEach
    void setUp() throws URISyntaxException, NullPointerException {

        testCsvFile = new File(getClass()
                .getClassLoader()
                .getResource("static/traffic/seoul_traffic_light_test.csv")
                .toURI());
    }

    @Test
    @DisplayName("데이터 랜더 처리 검증")
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
                serial, district, signalType, lat, lng, address
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


    @Test
    @DisplayName("보행등 정보 저장 검증")
    void saveTrafficInfo() throws IOException {

        //when
        trafficCsvService.saveCsvData( testCsvFile );

        //then
        assertThat(trafficRepository.count()).isGreaterThan(0);

    }
}
