package org.programmers.signalbuddyfinal.domain.trafficSignal.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficFileResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.programmers.signalbuddyfinal.domain.trafficSignal.exception.TrafficErrorCode;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrafficCsvService {

    private final TrafficRepository trafficRepository;

    @Transactional
    public void saveCsvData(File file) throws IOException {

        try (Reader reader = new BufferedReader( new InputStreamReader(new FileInputStream(file)) ) ){

            List<TrafficSignal> entityList = new ArrayList<>();

            CsvToBean<TrafficFileResponse> csvBean = new CsvToBeanBuilder<TrafficFileResponse>(reader)
                    .withType(TrafficFileResponse.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSkipLines(0)
                    .withEscapeChar('\n')
                    .build();

            List<TrafficFileResponse> traffics = csvBean.parse();

            traffics.forEach(traffic -> {
                System.out.println("serialNumber: " + traffic.getSerial());
                System.out.println("district: " + traffic.getDistrict());
                System.out.println("signalType: " + traffic.getSignalType());
                System.out.println("lat: " + traffic.getLat());
                System.out.println("lng: " + traffic.getLng());
                System.out.println("address: " + traffic.getAddress());
            });

            for(TrafficFileResponse trafficRes : traffics) {
                entityList.add(new TrafficSignal(trafficRes));
            }

            trafficRepository.saveAll(entityList);

        } catch (DataIntegrityViolationException e) {
            log.error("❌ Data Integrity Violation: {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.ALREADY_EXIST_TRAFFIC_SIGNAL);
        }

    }

}
