package org.programmers.signalbuddyfinal.domain.postit.batch;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.entity.QPostit;
import org.springframework.batch.item.ItemReader;

@Slf4j
public class PostItCustomReader implements ItemReader<Postit> {

    private final JPAQueryFactory queryFactory;
    private final int pageSize;
    private Long lastSeenId;
    private List<Postit> currentPage;
    private int currentIndex;

    public PostItCustomReader(EntityManagerFactory entityManagerFactory, int pageSize) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        this.queryFactory = new JPAQueryFactory(entityManager);
        this.pageSize = pageSize;
        this.lastSeenId = null;
        this.currentIndex = 0;
    }

    @Override
    public Postit read() {
        if (currentPage == null || currentIndex >= currentPage.size()) {
            fetchNextPage();
        }

        if (currentPage == null || currentPage.isEmpty()) {
            return null;
        }

        Postit postit = currentPage.get(currentIndex);
        currentIndex++;
        return postit;
    }

    private void fetchNextPage() {
        QPostit qPostit = QPostit.postit;

        currentPage = queryFactory
            .selectFrom(qPostit)
            .where(
                lastSeenId != null ? qPostit.postitId.gt(lastSeenId) : null,
                qPostit.expiryDate.after(LocalDateTime.now())
            ).orderBy(qPostit.postitId.asc())
            .offset(0)
            .limit(pageSize)
            .fetch();

        if (!currentPage.isEmpty()) {
            lastSeenId = currentPage.get(currentPage.size() - 1).getPostitId();
        }
        currentIndex = 0;
    }

}
