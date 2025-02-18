package org.programmers.signalbuddyfinal.domain.social.repository;

import org.programmers.signalbuddyfinal.domain.social.entity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialProviderRepository extends JpaRepository<SocialProvider, Long> {

    boolean existsByOauthProviderAndSocialId(String provider, String providerId);
}
