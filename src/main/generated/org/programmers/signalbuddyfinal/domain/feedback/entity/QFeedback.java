package org.programmers.signalbuddyfinal.domain.feedback.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedback is a Querydsl query type for Feedback
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedback extends EntityPathBase<Feedback> {

    private static final long serialVersionUID = -772748746L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedback feedback = new QFeedback("feedback");

    public final org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity _super = new org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity(this);

    public final EnumPath<org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus> answerStatus = createEnum("answerStatus", org.programmers.signalbuddyfinal.domain.feedback.entity.enums.AnswerStatus.class);

    public final StringPath category = createString("category");

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> feedbackId = createNumber("feedbackId", Long.class);

    public final NumberPath<Long> likeCount = createNumber("likeCount", Long.class);

    public final org.programmers.signalbuddyfinal.domain.member.entity.QMember member;

    public final StringPath secret = createString("secret");

    public final StringPath subject = createString("subject");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFeedback(String variable) {
        this(Feedback.class, forVariable(variable), INITS);
    }

    public QFeedback(Path<? extends Feedback> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedback(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedback(PathMetadata metadata, PathInits inits) {
        this(Feedback.class, metadata, inits);
    }

    public QFeedback(Class<? extends Feedback> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new org.programmers.signalbuddyfinal.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

