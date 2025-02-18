package org.programmers.signalbuddyfinal.domain.feedbackSummary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFeedbackSummary is a Querydsl query type for FeedbackSummary
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedbackSummary extends EntityPathBase<FeedbackSummary> {

    private static final long serialVersionUID = 1518413682L;

    public static final QFeedbackSummary feedbackSummary = new QFeedbackSummary("feedbackSummary");

    public final StringPath categoryCount = createString("categoryCount");

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final StringPath rank = createString("rank");

    public final NumberPath<Long> todayCount = createNumber("todayCount", Long.class);

    public QFeedbackSummary(String variable) {
        super(FeedbackSummary.class, forVariable(variable));
    }

    public QFeedbackSummary(Path<? extends FeedbackSummary> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFeedbackSummary(PathMetadata metadata) {
        super(FeedbackSummary.class, metadata);
    }

}

