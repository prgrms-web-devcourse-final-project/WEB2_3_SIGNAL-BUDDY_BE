package org.programmers.signalbuddyfinal.domain.weather.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.programmers.signalbuddyfinal.domain.weather.dto.GridResponse;
import org.programmers.signalbuddyfinal.domain.weather.dto.Weather;
import org.programmers.signalbuddyfinal.domain.weather.dto.WeatherResponse;
import org.programmers.signalbuddyfinal.domain.weather.entity.GridCoordinate;
import org.programmers.signalbuddyfinal.domain.weather.exception.WeatherErrorCode;
import org.programmers.signalbuddyfinal.domain.weather.repository.GridCoordinateRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    public static final String WEATHER_EVENT_NAME = "WEATHER_UPDATE";
    public static final double RADIUS = 0.05; // 5km 반경
    private static final Long TIMEOUT = 60L * 1000 * 30; // 30분
    private final GridCoordinateRepository gridCoordinateRepository;
    private final WeatherProvider weatherProvider;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();


    @Transactional
    public void saveExcel() throws IOException {
        ZipSecureFile.setMinInflateRatio(0.0001);
        try (final InputStream inputStream = new ClassPathResource(
            "/static/file/grid_coordinates.xlsx").getInputStream(); final Workbook workbook = new XSSFWorkbook(
            inputStream)) {

            final Sheet sheet = workbook.getSheetAt(0);
            final List<GridCoordinate> gridCoordinates = new ArrayList<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) { // 헤더 생략
                    continue;
                }
                try {
                    final double longitude = getNumericCellValue(row.getCell(7))
                                             + getNumericCellValue(row.getCell(8)) / 60
                                             + getNumericCellValue(row.getCell(9)) / 3600;

                    final double latitude = getNumericCellValue(row.getCell(10))
                                            + getNumericCellValue(row.getCell(11)) / 60
                                            + getNumericCellValue(row.getCell(12)) / 3600;

                    final GridCoordinate gridCoordinate = GridCoordinate.builder()
                        .regionCode(getStringCell(row.getCell(1)))
                        .city(getStringCell(row.getCell(2))).district(getStringCell(row.getCell(3)))
                        .subdistrict(getStringCell(row.getCell(4)))
                        .gridX(getNumericCellValue(row.getCell(5)))
                        .gridY(getNumericCellValue(row.getCell(6))).lng(longitude).lat(latitude)
                        .build();
                    gridCoordinates.add(gridCoordinate);
                } catch (Exception e) {
                    log.error("Error saving data", e);
                    throw new BusinessException(WeatherErrorCode.EXCEL_READ_ERROR);
                }
            }
            gridCoordinateRepository.saveAll(gridCoordinates);
        }
    }

    public WeatherResponse getWeatherData(double nx, double ny) {
        final List<Weather> weathers = weatherProvider.requestWeatherApi((int) nx, (int) ny);
        try {
            double temperature = 0;
            int precipitationTypeNumber = 0;
            double humidity = 0;
            double precipitation = 0;
            int windDirection = 0;
            double windSpeed = 0;

            for (Weather weather : weathers) {
                switch (weather.getCategory()) {
                    case Weather.T1H -> temperature = Double.parseDouble(weather.getObsrValue());
                    case Weather.PTY ->
                        precipitationTypeNumber = Integer.parseInt(weather.getObsrValue());
                    case Weather.REH -> humidity = Double.parseDouble(weather.getObsrValue());
                    case Weather.RN1 -> precipitation = Double.parseDouble(weather.getObsrValue());
                    case Weather.VEC -> windDirection = (int) Math.floor(
                        (Double.parseDouble(weather.getObsrValue()) + 22.5 * 0.5) / 22.5);
                    case Weather.WSD -> windSpeed = Double.parseDouble(weather.getObsrValue());
                    default -> log.debug("Unknown weather category: {}", weather.getCategory());
                }
            }

            final String precipitationType = getPrecipitationType(precipitationTypeNumber);

            return WeatherResponse.builder().temperature(temperature)
                .precipitationType(precipitationType).humidity(humidity)
                .precipitation(precipitation).windDirection(windDirection).windSpeed(windSpeed)
                .build();
        } catch (Exception e) {
            throw new BusinessException(WeatherErrorCode.RESPONSE_PARSE_ERROR);
        }
    }

    public SseEmitter subscribeToWeather(double lat, double lng) {
        final GridResponse gridResponse = gridCoordinateRepository.findByLatAndLngWithRadius(lat,
            lng, RADIUS);
        final String key = gridResponse.getGridX() + "," + gridResponse.getGridY();
        final SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(key, emitter);

        emitter.onCompletion(() -> emitters.remove(key));
        emitter.onTimeout(() -> {
            emitters.remove(key);
            emitter.complete();
        });
        // 구독 시 즉시 최신 날씨 정보 전송
        sendWeatherUpdate(emitter, gridResponse.getGridX(), gridResponse.getGridY());

        return emitter;
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void sendWeatherUpdates() {
        emitters.forEach((key, emitter) -> {
            final String[] coords = key.split(",");
            final double nx = Double.parseDouble(coords[0]);
            final double ny = Double.parseDouble(coords[1]);

            sendWeatherUpdate(emitter, nx, ny);
        });
    }

    private void sendWeatherUpdate(SseEmitter emitter, double nx, double ny) {
        final WeatherResponse weatherData = getWeatherData(nx, ny);
        try {
            emitter.send(SseEmitter.event().name(WEATHER_EVENT_NAME).data(weatherData));
        } catch (IOException e) {
            log.error("날씨 데이터 전송 실패", e);
            emitter.completeWithError(e);
        }
    }

    private String getStringCell(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> null;
        };
    }

    private double getNumericCellValue(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        return switch (cell.getCellType()) {
            case STRING -> Double.parseDouble(cell.getStringCellValue().trim());
            case NUMERIC -> cell.getNumericCellValue();
            default -> 0.0;
        };
    }

    private String getPrecipitationType(int precipitationTypeNumber) {
        switch (precipitationTypeNumber) {
            case 0 -> {
                return "없음";
            }
            case 1 -> {
                return "비";
            }
            case 2 -> {
                return "비/눈";
            }
            case 3 -> {
                return "눈";
            }
            case 5 -> {
                return "빗방울";
            }
            case 6 -> {
                return "빗방울눈날림";
            }
            case 7 -> {
                return "눈날림";
            }
            default -> throw new BusinessException(WeatherErrorCode.WEATHER_API_RESPONSE_ERROR);
        }
    }
}
