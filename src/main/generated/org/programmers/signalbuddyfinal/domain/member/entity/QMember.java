package org.programmers.signalbuddyfinal.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -1101697120L;

    public static final QMember member = new QMember("member1");

    public final org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity _super = new org.programmers.signalbuddyfinal.domain.basetime.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final EnumPath<org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus> memberStatus = createEnum("memberStatus", org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus.class);

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole> role = createEnum("role", org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

