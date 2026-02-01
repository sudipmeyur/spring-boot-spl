package com.spl.spl.service;

import java.time.Year;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional
	public Season completeAuction(Long seasonId, String completionNote) {
		Season season = seasonRepository.findById(seasonId)
				.orElseThrow(() -> new RuntimeException("Season not found"));
		
		season.setIsAuctionCompleted(true);
		season.setAuctionCompletionNote(completionNote);
		
		return seasonRepository.save(season);
	}
}