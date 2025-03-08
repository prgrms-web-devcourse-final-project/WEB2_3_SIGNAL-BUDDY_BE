package org.programmers.signalbuddyfinal.domain.postit_report.service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.programmers.signalbuddyfinal.domain.postit.repository.PostItRepository;
import org.programmers.signalbuddyfinal.domain.postit.service.PostItComplete;
import org.programmers.signalbuddyfinal.domain.postit_report.entity.PostItReport;
import org.programmers.signalbuddyfinal.domain.postit_report.exeception.PostItReportErrorCode;
import org.programmers.signalbuddyfinal.domain.postit_report.repository.PostItReportRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostItReportService {

    private final PostItReportRepository postItReportRepository;
    private final PostItRepository postItRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final PostItComplete postItComplete;

    private static final String USER_PREFIX = "postItReport:";
    private static final String COUNT_PREFIX = "postItReportCount:";
    private static final String REPORT_LOCK_PREFIX = "postItReportLock:";
    private static final int MAX_REPORT_COUNT = 10;
    private static final long LOCK_EXPIRATION_TIME = 5;


    @Transactional
    public void addReport(Long postItId, CustomUser2Member user) {

        String key = generateKey(postItId);
        String countKey = generateCountKey(postItId);
        String lockKey = REPORT_LOCK_PREFIX + postItId;

        SetOperations<Object, Object> reportSetOperations = redisTemplate.opsForSet();
        ValueOperations<Object, Object> countValueOperations = redisTemplate.opsForValue();

        if (!acquireLock(countValueOperations, lockKey)) {
            throw new BusinessException(PostItReportErrorCode.ALREADY_SUSPENDED_POSTIT);
        }

        try {
            checkAlreadyReported(reportSetOperations, key, postItId, user);

            Long reportCount = incrementReportCount(countKey, countValueOperations, postItId);
            Postit postit = postItRepository.findByIdOrThrow(postItId);

            saveReport(postit, user, reportCount);

            if (reportCount >= MAX_REPORT_COUNT) {
                handleExcessiveReport(postit, countKey, key);
            } else {
                redisTemplate.expire(countKey, 24L, TimeUnit.HOURS);
                redisTemplate.expire(key, 12L, TimeUnit.HOURS);
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Transactional
    public void cancelReport(Long postItId, CustomUser2Member user) {

        String key = generateKey(postItId);
        String countKey = generateCountKey(postItId);
        String lockKey = REPORT_LOCK_PREFIX + postItId;

        SetOperations<Object, Object> reportSetOperations = redisTemplate.opsForSet();
        ValueOperations<Object, Object> countValueOperations = redisTemplate.opsForValue();

        if (!acquireLock(countValueOperations, lockKey)) {
            throw new BusinessException(PostItReportErrorCode.ALREADY_SUSPENDED_POSTIT);
        }

        try {
            checkNotReported(reportSetOperations, key, postItId, user);

            reportSetOperations.add(key, user.getMemberId());

            Long reportCount = decrementReportCount(countKey, countValueOperations, postItId);
            Postit postit = postItRepository.findByIdOrThrow(postItId);

            cancelReportAtRepository(postit, user, reportCount);

            redisTemplate.expire(countKey, 24L, TimeUnit.HOURS);
            reportSetOperations.remove(key, user.getMemberId());

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    String generateKey(Long postItId) {
        return USER_PREFIX + ":" + postItId;
    }

    String generateCountKey(Long postItId) {
        return COUNT_PREFIX + ":" + postItId;
    }

    private boolean acquireLock(ValueOperations<Object, Object> countOperations,
        String lockKey) {
        return Boolean.TRUE.equals(countOperations.setIfAbsent(
            lockKey,
            "locked",
            LOCK_EXPIRATION_TIME,
            TimeUnit.SECONDS
        ));
    }

    private void checkAlreadyReported(SetOperations<Object, Object> userOperations, String key,
        Long postItId, CustomUser2Member user) {

        if (userOperations.isMember(key, user.getMemberId())
            || postItReportRepository.findPostItReportByPostItIdAndMemberId(postItId,
            user.getMemberId()) != null) {
            throw new BusinessException(PostItReportErrorCode.ALREADY_REPORTED_POSITI);
        }
        userOperations.add(key, user.getMemberId());
    }

    private void checkNotReported(SetOperations<Object, Object> userOperations, String key,
        Long postItId, CustomUser2Member user) {
        if (!userOperations.isMember(key, user.getMemberId())
            || postItReportRepository.findPostItReportByPostItIdAndMemberId(postItId,
            user.getMemberId()) == null) {
            throw new BusinessException(PostItReportErrorCode.NOT_FOUND_REPORTED_POSITI);
        }
        userOperations.add(key, user.getMemberId());
    }

    private void handleExcessiveReport(Postit postit, String countKey, String key) {
        postItComplete.completePostIt(postit, LocalDateTime.now());
        redisTemplate.delete(countKey);
        redisTemplate.delete(key);
    }

    private Long incrementReportCount(String countKey,
        ValueOperations<Object, Object> countValueOperations, Long postItId) {

        Long reportCount = (Long) countValueOperations.get(countKey);

        if (reportCount == null) {
            reportCount = postItRepository.findCountById(postItId).getReportCount();
        }

        countValueOperations.set(countKey, reportCount + 1);

        return (Long) countValueOperations.get(countKey);
    }

    private Long decrementReportCount(String countKey,
        ValueOperations<Object, Object> countValueOperations, Long postItId) {

        Long reportCount = (Long) countValueOperations.get(countKey);

        if (reportCount == null) {
            reportCount = postItRepository.findCountById(postItId).getReportCount();
        }

        countValueOperations.set(countKey, reportCount - 1);

        return (Long) countValueOperations.get(countKey);
    }

    private void saveReport(Postit postit, CustomUser2Member user, Long reportCount) {

        postItReportRepository.save(
            new PostItReport(memberRepository.findByIdOrThrow(user.getMemberId()), postit));
        postit.updateReportCount(reportCount);

    }

    private void cancelReportAtRepository(Postit postit, CustomUser2Member user, Long reportCount) {
        postItReportRepository.deleteByPostItIdAndUserId(postit.getPostitId(), user.getMemberId());
        postit.updateReportCount(reportCount);
    }
}
