package com.spl.spl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spl.spl.entity.PlayerTeam;

public interface PlayerTeamRepository extends JpaRepository<PlayerTeam, Long> {
}
