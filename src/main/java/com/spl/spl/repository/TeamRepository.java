package com.spl.spl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spl.spl.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
	Team findByCode(String code);
}