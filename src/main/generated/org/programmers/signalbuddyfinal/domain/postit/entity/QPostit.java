package org.programmers.signalbuddyfinal.domain.postit.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostit is a Querydsl query type for Postit
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostit extends EntityPathBase<Postit> {

    private static final long serialVersionUID = 82603074L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostit postit = new QPostit("postit");

    public final org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity _super = new org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    public final ComparablePath<org.locationtech.jts.geom.Point> coordinate = createComparable("coordinate", org.locationtech.jts.geom.Point.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath danger = createString("danger");

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> expiryDate = createDateTime("expiryDate", java.time.LocalDateTime.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final org.programmers.signalbuddyfinal.domain.member.entity.QMember member;

    public final NumberPath<Long> postitId = createNumber("postitId", Long.class);

    public final StringPath subject = createString("subject");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPostit(String variable) {
        this(Postit.class, forVariable(variable), INITS);
    }

    public QPostit(Path<? extends Postit> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostit(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostit(PathMetadata metadata, PathInits inits) {
        this(Postit.class, metadata, inits);
    }

    public QPostit(Class<? extends Postit> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new org.programmers.signalbuddyfinal.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

