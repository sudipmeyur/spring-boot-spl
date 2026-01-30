package com.spl.spl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spl.spl.entity.RuleEntity;

@Repository
public interface RuleEntityRepository extends JpaRepository<RuleEntity, Long> {
    
    @Query("SELECT r FROM RuleEntity r WHERE r.season.id = :seasonId " +
           "AND r.context = :context AND r.isActive = true ORDER BY r.priority")
    List<RuleEntity> findBySeasonAndContext(@Param("seasonId") Long seasonId, 
                                           @Param("context") String context);
    
    @Query("SELECT r FROM RuleEntity r WHERE r.season.id = :seasonId " +
           "AND r.isActive = true ORDER BY r.context, r.priority")
    List<RuleEntity> findBySeasonId(@Param("seasonId") Long seasonId);
    
    List<RuleEntity> findByContextAndIsActiveOrderByPriority(String context, Boolean isActive);
}