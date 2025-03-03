package org.programmers.signalbuddyfinal.domain.trafficSignal.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.WKBWriter;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficFileResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.programmers.signalbuddyfinal.domain.trafficSignal.exception.TrafficErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.nio.charset.Charset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrafficCsvService {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Transactional
    public void saveCsvData(String fileName) throws IOException {

        WKBWriter wkbWriter = new WKBWriter();
        String sql = "INSERT INTO traffic_signals(serial_number, district, signal_type, address, coordinate) VALUES (:serialNumber, :district, :signalType, :address, ST_GeomFromWKB(:coordinate))";

        try {

            if (!isValidFileName(fileName)) {
                throw new SecurityException("경로 탐색 시도 감지됨");
            }

            File file = new File("src/main/resources/static/file/"+fileName);
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("EUC-KR")));

            List<TrafficSignal> entityList = new ArrayList<>();

            CsvToBean<TrafficFileResponse> csvToBean = new CsvToBeanBuilder<TrafficFileResponse>(reader)
                    .withType(TrafficFileResponse.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<TrafficFileResponse> traffics = csvToBean.parse();

            for (TrafficFileResponse trafficRes : traffics) {
                entityList.add(new TrafficSignal(trafficRes));
            }

            //Bulk Insert
            if(!entityList.isEmpty()) {
            int batchSize = 1000;  // 배치 크기
            for (int i = 0; i < entityList.size(); i += batchSize) {
                List<TrafficSignal> batch = entityList.subList(i, Math.min(i+batchSize, entityList.size()));

                System.out.println("데이터 값 : " + Math.min(i+batchSize, entityList.size()));

                MapSqlParameterSource[] batchParams = batch.stream()
                        .map(entity -> new MapSqlParameterSource()
                                .addValue("serialNumber", entity.getSerialNumber())
                                .addValue("district", entity.getDistrict())
                                .addValue("signalType", entity.getSignalType())
                                .addValue("address", entity.getAddress())
                                .addValue("coordinate", wkbWriter.write(entity.getCoordinate())))
                        .toArray(MapSqlParameterSource[]::new);

                namedJdbcTemplate.batchUpdate(sql, batchParams);
            }
}
        } catch (FileNotFoundException e){
            log.error("❌ File Not Found : {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.FILE_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            log.error("❌ Data Integrity Violation: {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.ALREADY_EXIST_TRAFFIC_SIGNAL);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    // 파일 이름 검증 (특수 문자 및 경로 탐색 방지)
    private boolean isValidFileName(String fileName) {
        String regex = "^[a-zA-Z0-9._-]+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(fileName).matches();
    }

}
