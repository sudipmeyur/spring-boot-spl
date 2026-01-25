package com.spl.spl.service;

import java.time.Year;

import org.springframework.stereotype.Service;

import com.spl.spl.entity.Season;
import com.spl.spl.repository.SeasonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeasonService {

	private final SeasonRepository seasonRepository;

	public Season getCurrentSeason() {
		return seasonRepository.findByYear(Year.now().getValue());
	}
}