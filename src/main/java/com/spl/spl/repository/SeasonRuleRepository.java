package com.spl.spl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spl.spl.entity.Rule;
import com.spl.spl.entity.SeasonRule;
import com.spl.spl.entity.SeasonRuleId;

@Repository
public interface SeasonRuleRepository extends JpaRepository<SeasonRule, SeasonRuleId> {
    
    SeasonRule findByCode(String code);
    
    @Query("SELECT sr.rule FROM SeasonRule sr WHERE sr.season.id = :seasonId AND sr.rule.context = :context AND sr.rule.isActive = true ORDER BY sr.rule.priority")
    List<Rule> findBySeasonIdAndRuleContext(@Param("seasonId") Long seasonId, @Param("context") String context);
}