package com.spl.spl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spl.spl.entity.Season;

public interface SeasonRepository extends JpaRepository<Season, Long> {
	Season findByCode(String code);
	Season findByYear(Integer year);
}