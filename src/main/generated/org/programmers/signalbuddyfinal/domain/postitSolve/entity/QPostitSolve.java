package org.programmers.signalbuddyfinal.domain.postitSolve.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostitSolve is a Querydsl query type for PostitSolve
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostitSolve extends EntityPathBase<PostitSolve> {

    private static final long serialVersionUID = -2046482286L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostitSolve postitSolve = new QPostitSolve("postitSolve");

    public final org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity _super = new org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final org.programmers.signalbuddyfinal.domain.member.entity.QMember member;

    public final org.programmers.signalbuddyfinal.domain.postit.entity.QPostit postit;

    public final NumberPath<Long> PostitSolvesId = createNumber("PostitSolvesId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPostitSolve(String variable) {
        this(PostitSolve.class, forVariable(variable), INITS);
    }

    public QPostitSolve(Path<? extends PostitSolve> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostitSolve(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostitSolve(PathMetadata metadata, PathInits inits) {
        this(PostitSolve.class, metadata, inits);
    }

    public QPostitSolve(Class<? extends PostitSolve> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new org.programmers.signalbuddyfinal.domain.member.entity.QMember(forProperty("member")) : null;
        this.postit = inits.isInitialized("postit") ? new org.programmers.signalbuddyfinal.domain.postit.entity.QPostit(forProperty("postit"), inits.get("postit")) : null;
    }

}

