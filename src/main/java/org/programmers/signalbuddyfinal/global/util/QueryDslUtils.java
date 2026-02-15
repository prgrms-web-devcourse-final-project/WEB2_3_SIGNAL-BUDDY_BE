package org.programmers.signalbuddyfinal.global.util;

import static com.querydsl.core.types.dsl.Expressions.numberTemplate;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryDslUtils {

    /**
     * createAt이 startDate ~ endDate 범위로 설정
     *
     * @param path      QClass의 LocalDateTime 필드 <br>
     *                  ex) QFeedback.feedback.createdAt, QFeedback.feedback.updatedAt
     * @param startDate 조회하려는 시작 날짜 (null이면 없는 것으로 처리)
     * @param endDate   조회하려는 끝 날짜 (null이면 없는 것으로 처리)
     * @return 시작 날짜 ~ 끝 날짜 범위를 조건으로 설정하여 반환 (둘 다 null이면 전체 조회)
     */
    public static BooleanExpression betweenDates(
        DateTimePath<LocalDateTime> path,
        LocalDate startDate, LocalDate endDate
    ) {
        BooleanExpression predicate = null;

        if (startDate != null) {
            predicate = path.goe(startDate.atStartOfDay());
        }

        if (endDate != null) {
            BooleanExpression endCondition = path.loe(endDate.atStartOfDay());
            predicate = predicate == null ? endCondition : predicate.and(endCondition);
        }

        return predicate != null ? predicate : Expressions.TRUE;
    }

    /**
     * Pageable의 설정한 정렬을 QueryDSL에서 사용할 수 있게 조건을 정렬 조건을 반환
     *
     * @param pageable 쿼리 파라미터로 가져온 값들
     * @param type     정렬할 컬럼의 클래스 <br> ex) QFeedback.feedback.getType()
     * @param variable 정렬할 QClass의 Entity 필드명 <br> ex) "feedback"
     * @return 정렬 조건을 반환
     * @throws org.springframework.dao.InvalidDataAccessApiUsageException 잘못된 필드명을 입력하면 쿼리를 처리하는 중
     *                                                                    해당 예외가 발생한다.
     */
    public static OrderSpecifier<?>[] getOrderSpecifiers(
        Pageable pageable,
        Class<?> type,
        String variable
    ) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        // 정렬 조건이 없는 경우
        if (pageable.getSort().isUnsorted()) {
            return new OrderSpecifier<?>[1];
        }

        PathBuilder<?> pathBuilder = new PathBuilder<>(type, variable);

        // Sort 정보를 기반으로 OrderSpecifier 생성
        for (Sort.Order order : pageable.getSort()) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            orderSpecifiers.add(new OrderSpecifier<>(direction,
                pathBuilder.getComparable(order.getProperty(), Comparable.class)));
        }

        @SuppressWarnings("unchecked")
        OrderSpecifier<?>[] results = orderSpecifiers.toArray(new OrderSpecifier<?>[0]);
        return results;
    }

    /**
     * 타켓 컬럼으로부터 Full Text Search를 한다.
     *
     * @param keyword 검색어
     * @param target1 타켓 컬럼 1
     * @param target2 타켓 컬럼 2
     * @return QueryDSL에서 Where의 조건으로 사용
     */
    public static BooleanExpression fulltextSearch(
        String keyword,
        StringPath target1, StringPath target2
    ) {
        if (keyword == null || keyword.isBlank()) {
            return Expressions.TRUE;
        }

        String formattedSearchWord = keyword + "*";
        return numberTemplate(
            Double.class, "function('match2_against', {0}, {1}, {2})",
            target1, target2, formattedSearchWord
        ).gt(0);
    }

    /**
     * 타켓 컬럼으로부터 Full Text Search 한다.
     *
     * @param keyword 검색어
     * @param target 타켓 컬럼 1
     * @return QueryDSL에서 Where의 조건으로 사용
     */
    public static BooleanExpression fulltextSearch(String keyword, StringPath target) {
        if (keyword == null || keyword.isBlank()) {
            return Expressions.TRUE;
        }

        String formattedSearchWord = keyword + "*";
        return numberTemplate(
            Double.class, "function('match_against', {0}, {1})",
            target, formattedSearchWord
        ).gt(0);
    }

    /**
     * 두 좌표 간의 거리 계산
     *
     * @param coordinate DB의 좌표 데이터 <br> ex) QCrossroad.crossroad.coordinate
     * @param target 비교할 좌표
     * @return 두 좌표 간의 거리(m)
     */
    public static NumberExpression<Double> distanceSphere(
        Expression<Point> coordinate,
        Point target
    ) {
        return Expressions.numberTemplate(
            Double.class,
            "ST_Distance_Sphere({0}, {1})",
            coordinate, target
        );
    }

    /**
     * 중심 좌표와 반경(m)을 받아 MBR(최소 경계 사각형) 내의 데이터 필터링
     *
     * @param center 중심점
     * @param radius 반경(m)
     * @param coordinate 필터링할 DB의 좌표 데이터 <br> ex) QCrossroad.crossroad.coordinate)
     * @return 반경 내 포함 여부, 1(True) or 0(False) 반환
     */
    public static BooleanExpression mbrContains(
        Point center,
        int radius,
        Expression<Point> coordinate
    ) {
        return Expressions.booleanTemplate(
            "MBRContains(GeomFromText({0}), {1})",
            createMBRPolygonText(center.getY(), center.getX(), radius),
            coordinate
        );
    }

    /**
     * 중심 좌표와 반경(m)을 받아 MBR(최소 경계 사각형) 좌표에 대한 문자열 생성
     *
     * @param lat 위도
     * @param lng 경도
     * @param radius 반경(m)
     * @return Polygon 타입의 MBR 좌표 텍스트
     */
    private static String createMBRPolygonText(double lat, double lng, int radius) {
        double radiusKm = Math.abs(radius) / 1000.0;  // m -> km (단위 변환)

        // radius 거리 당 위도와 경도의 차이
        double latDiff = radiusKm / 111.32;
        double lngDiff = radiusKm / (111.32 * Math.cos(Math.toRadians(lat)));

        // MBR 좌표
        double minLat = lat - latDiff;
        double maxLat = lat + latDiff;
        double minLng = lng - lngDiff;
        double maxLng = lng + lngDiff;

        return String.format("Polygon((%f %f, %f %f, %f %f, %f %f, %f %f))",
            minLng, minLat, // 좌하단
            maxLng, minLat, // 우하단
            maxLng, maxLat, // 우상단
            minLng, maxLat, // 좌상단
            minLng, minLat  // 닫기 (시작점과 동일)
        );
    }
}
