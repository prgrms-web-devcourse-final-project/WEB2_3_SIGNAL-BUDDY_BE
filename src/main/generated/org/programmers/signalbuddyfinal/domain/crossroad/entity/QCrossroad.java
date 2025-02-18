package org.programmers.signalbuddyfinal.domain.crossroad.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCrossroad is a Querydsl query type for Crossroad
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCrossroad extends EntityPathBase<Crossroad> {

    private static final long serialVersionUID = -1416614574L;

    public static final QCrossroad crossroad = new QCrossroad("crossroad");

    public final org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity _super = new org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity(this);

    public final ComparablePath<org.locationtech.jts.geom.Point> coordinate = createComparable("coordinate", org.locationtech.jts.geom.Point.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath crossroadApiId = createString("crossroadApiId");

    public final NumberPath<Long> crossroadId = createNumber("crossroadId", Long.class);

    public final StringPath name = createString("name");

    public final StringPath status = createString("status");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCrossroad(String variable) {
        super(Crossroad.class, forVariable(variable));
    }

    public QCrossroad(Path<? extends Crossroad> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCrossroad(PathMetadata metadata) {
        super(Crossroad.class, metadata);
    }

}

