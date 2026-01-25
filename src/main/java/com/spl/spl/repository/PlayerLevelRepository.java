package com.spl.spl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spl.spl.entity.PlayerLevel;

public interface PlayerLevelRepository extends JpaRepository<PlayerLevel, Long> {
}