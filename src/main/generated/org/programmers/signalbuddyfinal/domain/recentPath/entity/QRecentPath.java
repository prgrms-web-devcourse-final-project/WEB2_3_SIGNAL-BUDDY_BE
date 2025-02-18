package org.programmers.signalbuddyfinal.domain.recentPath.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecentPath is a Querydsl query type for RecentPath
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecentPath extends EntityPathBase<RecentPath> {

    private static final long serialVersionUID = -852245332L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecentPath recentPath = new QRecentPath("recentPath");

    public final org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity _super = new org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity(this);

    public final org.programmers.signalbuddyfinal.domain.bookmark.entity.QBookmark bookmark;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final ComparablePath<org.locationtech.jts.geom.Point> endPoint = createComparable("endPoint", org.locationtech.jts.geom.Point.class);

    public final org.programmers.signalbuddyfinal.domain.member.entity.QMember member;

    public final StringPath name = createString("name");

    public final NumberPath<Long> recentPathId = createNumber("recentPathId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRecentPath(String variable) {
        this(RecentPath.class, forVariable(variable), INITS);
    }

    public QRecentPath(Path<? extends RecentPath> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecentPath(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecentPath(PathMetadata metadata, PathInits inits) {
        this(RecentPath.class, metadata, inits);
    }

    public QRecentPath(Class<? extends RecentPath> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.bookmark = inits.isInitialized("bookmark") ? new org.programmers.signalbuddyfinal.domain.bookmark.entity.QBookmark(forProperty("bookmark"), inits.get("bookmark")) : null;
        this.member = inits.isInitialized("member") ? new org.programmers.signalbuddyfinal.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

