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
    public void saveCsvData(String fileName) {

        log.debug("보행등 파일 데이터 저장 - fileName = {}", fileName);

        WKBWriter wkbWriter = new WKBWriter();
        String sql = "INSERT INTO traffic_signals(serial_number, district, signal_type, address, coordinate) "
            + "VALUES (:serialNumber, :district, :signalType, :address, ST_GeomFromWKB(:coordinate))";
        /*
        WKBWriter 처리
        WKB (Well-Known Binary) 는 공간 데이터를 이진 형식으로 표현하는 표준
        -> JPA + Spatial로 DB 저장시 binary형식으로 저장되는데 WKB형식과 동일
        -> 좌표값을 WKB형식으로 변환해서 저장이 필요
         */

        try {

            if (!isValidFileName(fileName)) {
                log.error("SecurityException - fileName = {}", fileName);
                throw new SecurityException("경로 탐색 시도 감지됨");
            }

            File file = new File("src/main/resources/static/file/"+fileName);
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("EUC-KR")));

            List<TrafficSignal> entityList = new ArrayList<>();

            log.info("csvtoBean-opencsv (csv파일 객체화)");
            CsvToBean<TrafficFileResponse> csvToBean = new CsvToBeanBuilder<TrafficFileResponse>(reader)
                    .withType(TrafficFileResponse.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<TrafficFileResponse> traffics = csvToBean.parse();

            log.info("DTO -> Entity로 데이터 변환");
            for (TrafficFileResponse trafficRes : traffics) {
                entityList.add(new TrafficSignal(trafficRes));
            }

            //Bulk Insert
            if(!entityList.isEmpty()) {
                int batchSize = 1000;  // 배치 크기
                for (int i = 0; i < entityList.size(); i += batchSize) {
                    List<TrafficSignal> batch = entityList.subList(i, Math.min(i+batchSize, entityList.size()));
                    log.info("배치 범위 - i = {}, i+ batchSize = {}", i, batchSize+i);

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

                log.info("csv파일 데이터 저장 완료");
            }
        } catch (FileNotFoundException e){
            log.error("File Not Found : {}", e.getMessage());
            throw new BusinessException(TrafficErrorCode.FILE_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            log.error("Data Integrity Violation: {}", e.getMessage());
            throw new BusinessException(TrafficErrorCode.ALREADY_EXIST_TRAFFIC_SIGNAL);
        } catch (Exception e) {
            log.error("error message = {}",e.getMessage());
            throw new RuntimeException("파일 처리 중 예외 발생", e);
        }

    }

    // 파일 이름 검증 (특수 문자 및 경로 탐색 방지)
    private boolean isValidFileName(String fileName) {
        String regex = "^[a-zA-Z0-9._-]+$";
        Pattern pattern = Pattern.compile(regex);
        boolean matches = pattern.matcher(fileName).matches();

        log.info("파일 이름 검증 - match = {}", matches);
        return matches;
    }

}
