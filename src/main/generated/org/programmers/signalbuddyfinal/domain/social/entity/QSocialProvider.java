package org.programmers.signalbuddyfinal.domain.social.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSocialProvider is a Querydsl query type for SocialProvider
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSocialProvider extends EntityPathBase<SocialProvider> {

    private static final long serialVersionUID = -1882120169L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSocialProvider socialProvider = new QSocialProvider("socialProvider");

    public final org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity _super = new org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final org.programmers.signalbuddyfinal.domain.member.entity.QMember member;

    public final StringPath oauthProvider = createString("oauthProvider");

    public final StringPath socialId = createString("socialId");

    public final NumberPath<Long> socialProviderId = createNumber("socialProviderId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSocialProvider(String variable) {
        this(SocialProvider.class, forVariable(variable), INITS);
    }

    public QSocialProvider(Path<? extends SocialProvider> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSocialProvider(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSocialProvider(PathMetadata metadata, PathInits inits) {
        this(SocialProvider.class, metadata, inits);
    }

    public QSocialProvider(Class<? extends SocialProvider> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new org.programmers.signalbuddyfinal.domain.member.entity.QMember(forProperty("member")) : null;
    }

}

