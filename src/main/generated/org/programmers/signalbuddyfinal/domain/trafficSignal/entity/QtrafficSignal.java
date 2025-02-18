package org.programmers.signalbuddyfinal.domain.trafficSignal.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QtrafficSignal is a Querydsl query type for trafficSignal
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QtrafficSignal extends EntityPathBase<trafficSignal> {

    private static final long serialVersionUID = -903594478L;

    public static final QtrafficSignal trafficSignal = new QtrafficSignal("trafficSignal");

    public final StringPath address = createString("address");

    public final ComparablePath<org.locationtech.jts.geom.Point> coordinate = createComparable("coordinate", org.locationtech.jts.geom.Point.class);

    public final StringPath district = createString("district");

    public final NumberPath<Long> serialNumber = createNumber("serialNumber", Long.class);

    public final StringPath signal_type = createString("signal_type");

    public final NumberPath<Long> trafficSignalId = createNumber("trafficSignalId", Long.class);

    public QtrafficSignal(String variable) {
        super(trafficSignal.class, forVariable(variable));
    }

    public QtrafficSignal(Path<? extends trafficSignal> path) {
        super(path.getType(), path.getMetadata());
    }

    public QtrafficSignal(PathMetadata metadata) {
        super(trafficSignal.class, metadata);
    }

}

