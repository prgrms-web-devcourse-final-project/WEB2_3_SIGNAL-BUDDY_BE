package org.programmers.signalbuddyfinal.domain.trafficSignal.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
<<<<<<< HEAD
import org.programmers.signalbuddyfinal.domain.feedback_report.exception.FeedbackReportErrorCode;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
=======
>>>>>>> b9eea33 ([refactor] : 파일 데이터 저장)
import org.programmers.signalbuddyfinal.domain.trafficSignal.dto.TrafficFileResponse;
import org.programmers.signalbuddyfinal.domain.trafficSignal.entity.TrafficSignal;
import org.programmers.signalbuddyfinal.domain.trafficSignal.exception.TrafficErrorCode;
import org.programmers.signalbuddyfinal.domain.trafficSignal.repository.TrafficRepository;
<<<<<<< HEAD
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
=======
>>>>>>> b9eea33 ([refactor] : 파일 데이터 저장)
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<<<<<<< HEAD

import java.io.*;
import java.nio.charset.Charset;
=======
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
>>>>>>> b9eea33 ([refactor] : 파일 데이터 저장)
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrafficCsvService {

    private final TrafficRepository trafficRepository;

    @Transactional
<<<<<<< HEAD
    public void saveCsvData(File file, CustomUser2Member user) throws IOException {



        try (Reader reader = new BufferedReader( new InputStreamReader(new FileInputStream(file), Charset.forName("EUC-KR") ) ) ){

            List<TrafficSignal> entityList = new ArrayList<>();

            CsvToBean<TrafficFileResponse> csvBean = new CsvToBeanBuilder<TrafficFileResponse>(reader)
                    .withType(TrafficFileResponse.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSkipLines(0)
                    .withEscapeChar('\n')
                    .build();

            List<TrafficFileResponse> traffics = csvBean.parse();

            for (TrafficFileResponse trafficFileResponse : traffics) {
                System.out.println(trafficFileResponse.getLng());
            }
=======
    public void saveCsvData(MultipartFile file) throws IOException {

        List<TrafficSignal> entityList = new ArrayList<>();
        Reader reader = new BufferedReader( new InputStreamReader(file.getInputStream() ) );

        try {
            CsvToBean<TrafficFileResponse> csvToBean = new CsvToBeanBuilder<TrafficFileResponse>(reader)
                    .withType(TrafficFileResponse.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<TrafficFileResponse> traffics = csvToBean.parse();
>>>>>>> b9eea33 ([refactor] : 파일 데이터 저장)

            for(TrafficFileResponse trafficRes : traffics) {
                entityList.add(new TrafficSignal(trafficRes));
            }

            trafficRepository.saveAll(entityList);

        } catch (DataIntegrityViolationException e) {
<<<<<<< HEAD
            log.error("❌ Data Integrity Violation: {}", e.getMessage(), e);
            throw new BusinessException(TrafficErrorCode.ALREADY_EXIST_TRAFFIC_SIGNAL);
=======
            throw new BusinessException(TrafficErrorCode.ALREADY_EXIST_TRAFFIC_SIGNAL);
        } catch (Exception e){
            log.error(e.getMessage());
>>>>>>> b9eea33 ([refactor] : 파일 데이터 저장)
        }

    }

<<<<<<< HEAD
    // 관리자만 접근 가능
    private void verifyAdmin(CustomUser2Member user) {
        if (!MemberRole.ADMIN.equals(user.getRole())) {
            throw new BusinessException(
                    FeedbackReportErrorCode.REQUEST_NOT_AUTHORIZED
            );
        }
    }

=======
>>>>>>> b9eea33 ([refactor] : 파일 데이터 저장)
            log.error("❌ Data Integrity Violation: {}", e.getMessage(), e);
    // 관리자만 접근 가능
    private void verifyAdmin(CustomUser2Member user) {
        if (!MemberRole.ADMIN.equals(user.getRole())) {
            throw new BusinessException(
                    FeedbackReportErrorCode.REQUEST_NOT_AUTHORIZED
            );
        }
    }

}
