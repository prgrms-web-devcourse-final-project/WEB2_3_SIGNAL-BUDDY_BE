package org.programmers.signalbuddyfinal.domain.air_quality.service;

import lombok.RequiredArgsConstructor;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoordinateConverter {

    private final CoordinateTransform transform;

    public CoordinateConverter() {
        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem crsWGS84 = factory.createFromName("EPSG:4326");
        CoordinateReferenceSystem crsTM = factory.createFromName("EPSG:5181");

        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        this.transform = ctFactory.createTransform(crsWGS84, crsTM);
    }

    public ProjCoordinate convert(double longitude, double latitude) {
        ProjCoordinate srcCoord = new ProjCoordinate(longitude, latitude);
        ProjCoordinate destCoord = new ProjCoordinate();

        transform.transform(srcCoord, destCoord);
        return destCoord;
    }
}
