package com.spl.spl.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summarizingDouble;

import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spl.spl.dto.PlayerTeamRequest;
import com.spl.spl.entity.Player;
import com.spl.spl.entity.PlayerTeam;
import com.spl.spl.entity.Season;
import com.spl.spl.entity.TeamSeason;
import com.spl.spl.entity.TeamSeasonPlayerLevel;
import com.spl.spl.exception.PlayerLimitExceededException;
import com.spl.spl.repository.PlayerRepository;
import com.spl.spl.repository.PlayerTeamRepository;
import com.spl.spl.repository.TeamSeasonRepository;
import com.spl.spl.repository.TeamSeasonPlayerLevelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerTeamService {

	private final PlayerTeamRepository playerTeamRepository;
	private final PlayerRepository playerRepository;
	private final TeamSeasonRepository teamSeasonRepository;
	private final TeamSeasonPlayerLevelRepository teamSeasonPlayerLevelRepository;

	@Transactional
	public PlayerTeam savePlayerTeam(PlayerTeamRequest request) {

		PlayerTeam result = null;
		List<TeamSeason> summary = new ArrayList<>();
		
		TeamSeason teamSeason = teamSeasonRepository.findByCode(request.getTeamSeasonCode());
		Season season = teamSeason.getSeason();
		
		validateTotalRtmUsed(season,teamSeason,request);
		validateTotalFreeUsed(season,teamSeason,request);
		
		Player player = playerRepository.findByCode(request.getPlayerCode());
		
		summary.add(teamSeason);

		String generatedCode = player.getCode() + teamSeason.getCode();

		if (StringUtils.isBlank(request.getCode())) {
			PlayerTeam playerTeam = new PlayerTeam();
			playerTeam.setPlayer(player);
			playerTeam.setTeamSeason(teamSeason);
			playerTeam.setCode(generatedCode);
			playerTeam.setSoldAmount(request.getSoldAmount());
			playerTeam.setIsFree(request.getIsFree());
			playerTeam.setIsRtmUsed(request.getIsRtmUsed());
			result = playerTeamRepository.save(playerTeam);
			
			// Manual list management
			teamSeason.getPlayerTeams().add(result);
		} else {
			PlayerTeam existingPlayerTeam = playerTeamRepository.findByCode(request.getCode());
			
			if (!StringUtils.equals(generatedCode, existingPlayerTeam.getCode())) {
				
				summary.add(existingPlayerTeam.getTeamSeason());
				
				// Remove from old team season
				existingPlayerTeam.getTeamSeason().getPlayerTeams().remove(existingPlayerTeam);
				
				playerTeamRepository.delete(existingPlayerTeam);
				PlayerTeam newPlayerTeam = new PlayerTeam();
				newPlayerTeam.setPlayer(player);
				newPlayerTeam.setTeamSeason(teamSeason);
				newPlayerTeam.setCode(generatedCode);
				newPlayerTeam.setSoldAmount(request.getSoldAmount());
				newPlayerTeam.setIsFree(request.getIsFree());
				newPlayerTeam.setIsRtmUsed(request.getIsRtmUsed());
				result = playerTeamRepository.save(newPlayerTeam);
				
				// Add to new team season
				teamSeason.getPlayerTeams().add(result);
			} else {
				existingPlayerTeam.setSoldAmount(request.getSoldAmount());
				existingPlayerTeam.setIsFree(request.getIsFree());
				existingPlayerTeam.setIsRtmUsed(request.getIsRtmUsed());
				result = playerTeamRepository.save(existingPlayerTeam);
				// No list changes needed - just amount update
			}
		}
		
		manageTeamSeasonStatus(summary);
		
		return result;
	}

	private void validateTotalFreeUsed(Season season, TeamSeason teamSeason, PlayerTeamRequest request) {
		if(request.getIsFree() != null && request.getIsFree()) {
			if(season.getMaxFreeAllowed() <= teamSeason.getTotalFreeUsed()) {
				throw new PlayerLimitExceededException("free", teamSeason.getTotalFreeUsed(), season.getMaxFreeAllowed());
			}
		}
	}

	private void validateTotalRtmUsed(Season season, TeamSeason teamSeason, PlayerTeamRequest request) {
		if(request.getIsRtmUsed() != null && request.getIsRtmUsed()) {
			if(season.getMaxRtmAllowed() <= teamSeason.getTotalRtmUsed()) {
				throw new PlayerLimitExceededException("RTM", teamSeason.getTotalRtmUsed(), season.getMaxRtmAllowed());
			}
		}
	}

	private void manageTeamSeasonStatus(List<TeamSeason> summary) {
		if (summary != null && !summary.isEmpty()) {
			for (TeamSeason teamSeason : summary) {
				if (teamSeason != null) {
					// Calculate player level summaries
					teamSeason.getPlayerTeams().stream()
						.collect(groupingBy(
							pt -> pt.getPlayer().getPlayerLevel(),
							summarizingDouble(pt -> pt.getSoldAmount() != null ? pt.getSoldAmount().doubleValue() : 0.0)))
						.forEach((playerLevel, stats) -> {
							TeamSeasonPlayerLevel tspl = teamSeasonPlayerLevelRepository
								.findByTeamSeasonIdAndPlayerLevelId(teamSeason.getId(), playerLevel.getId());
							if (tspl == null) {
								tspl = new TeamSeasonPlayerLevel();
								tspl.setTeamSeason(teamSeason);
								tspl.setPlayerLevel(playerLevel);
							}
							tspl.setTotalAmountSpent(BigDecimal.valueOf(stats.getSum()));
							tspl.setTotalPlayerCount((int) stats.getCount());
							teamSeasonPlayerLevelRepository.save(tspl);
						});
					
					// Update team season totals
					BigDecimal totalAmount = teamSeason.getPlayerTeams().stream()
						.map(PlayerTeam::getSoldAmount)
						.filter(amount -> amount != null)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
					
					long totalRtmUsed =teamSeason.getPlayerTeams().stream()
							.filter(playerTeam -> playerTeam.getIsRtmUsed() != null && playerTeam.getIsRtmUsed()).count();
					
					long totalFreeUsed =teamSeason.getPlayerTeams().stream()
							.filter(playerTeam -> playerTeam.getIsFree() != null && playerTeam.getIsFree()).count();
					
					teamSeason.setTotalRtmUsed((int)totalRtmUsed);				
					teamSeason.setTotalFreeUsed((int)totalFreeUsed);
					
					teamSeason.setTotalAmountSpent(totalAmount);
					teamSeason.setTotalPlayer(teamSeason.getPlayerTeams().size());
					teamSeasonRepository.save(teamSeason);
				}
			}
		}
	}
}