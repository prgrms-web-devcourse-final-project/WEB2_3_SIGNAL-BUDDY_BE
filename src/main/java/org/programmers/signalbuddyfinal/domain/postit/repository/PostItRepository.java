package org.programmers.signalbuddyfinal.domain.postit.repository;

import org.programmers.signalbuddyfinal.domain.postit.entity.Postit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostItRepository extends JpaRepository<Postit, Long> {


}
