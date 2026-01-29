package com.spl.spl.service;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;


import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summarizingDouble;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spl.spl.dto.PlayerLevelCalcDto;
import com.spl.spl.dto.PlayerTeamRequest;
import com.spl.spl.entity.Player;
import com.spl.spl.entity.PlayerTeam;
import com.spl.spl.entity.Season;
import com.spl.spl.entity.TeamSeason;
import com.spl.spl.entity.TeamSeasonPlayerLevel;
import com.spl.spl.entity.UnsoldPlayer;
import com.spl.spl.exception.PlayerLimitExceededException;
import com.spl.spl.exception.SplBadRequestException;
import com.spl.spl.repository.PlayerRepository;
import com.spl.spl.repository.PlayerTeamRepository;
import com.spl.spl.repository.TeamSeasonPlayerLevelRepository;
import com.spl.spl.repository.TeamSeasonRepository;
import com.spl.spl.repository.UnsoldPlayerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerTeamService {

	private final PlayerTeamRepository playerTeamRepository;
	private final PlayerRepository playerRepository;
	private final TeamSeasonRepository teamSeasonRepository;
	private final TeamSeasonPlayerLevelRepository teamSeasonPlayerLevelRepository;
	private final UnsoldPlayerRepository unsoldPlayerRepository;

	@Transactional
	public PlayerTeam savePlayerTeam(PlayerTeamRequest request) {

		PlayerTeam result = null;
		List<TeamSeason> summary = new ArrayList<>();
		
		TeamSeason teamSeason = teamSeasonRepository.findByCode(request.getTeamSeasonCode());
		Season season = teamSeason.getSeason();
		
		validateTotalRtmUsed(season,teamSeason,request);
		validateTotalFreeUsed(season,teamSeason,request);
		
		Player player = playerRepository.findByCode(request.getPlayerCode());
		
		validateAmount(season,player,request);
		
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
			playerTeam.setWasUnsold(request.getIsUnsold());
			playerTeam.setIsManager(request.getIsManager());
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
				newPlayerTeam.setWasUnsold(request.getIsUnsold());
				newPlayerTeam.setIsManager(request.getIsManager());
				result = playerTeamRepository.save(newPlayerTeam);
				
				// Add to new team season
				teamSeason.getPlayerTeams().add(result);
			} else {
				existingPlayerTeam.setSoldAmount(request.getSoldAmount());
				existingPlayerTeam.setIsFree(request.getIsFree());
				existingPlayerTeam.setIsRtmUsed(request.getIsRtmUsed());
				existingPlayerTeam.setWasUnsold(request.getIsUnsold());
				existingPlayerTeam.setIsManager(request.getIsManager());
				result = playerTeamRepository.save(existingPlayerTeam);
				// No list changes needed - just amount update
			}
		}
		
		if(result.getWasUnsold()!=null && result.getWasUnsold()) {
			UnsoldPlayer existingUnsoldPlayer = unsoldPlayerRepository.findBySeasonIdAndPlayerId(season.getId(),player.getId());
			if(existingUnsoldPlayer!=null) {
				unsoldPlayerRepository.delete(existingUnsoldPlayer);
			}
		}
		
		manageTeamSeasonStatus(summary);
		
		return result;
	}

	@Transactional
	public void revertPlayerTeam(String playerTeamCode) {
		PlayerTeam playerTeam = playerTeamRepository.findByCode(playerTeamCode);
		
		if (playerTeam == null) {
			throw new com.spl.spl.exception.ResourceNotFoundException("PlayerTeam", playerTeamCode);
		}
		
		List<TeamSeason> affectedTeamSeasons = new ArrayList<>();
		TeamSeason teamSeason = playerTeam.getTeamSeason();
		Season season = teamSeason.getSeason();
		Player player = playerTeam.getPlayer();
		
		affectedTeamSeasons.add(teamSeason);
		
		// Remove player-team assignment
		teamSeason.getPlayerTeams().remove(playerTeam);
		playerTeamRepository.delete(playerTeam);
		
		// Restore unsold player record if it was marked as unsold
		if (playerTeam.getWasUnsold() != null && playerTeam.getWasUnsold()) {
			UnsoldPlayer unsoldPlayer = new UnsoldPlayer();
			unsoldPlayer.setPlayer(player);
			unsoldPlayer.setSeason(season);
			unsoldPlayerRepository.save(unsoldPlayer);
		}
		
		// Recalculate team season statistics
		manageTeamSeasonStatus(affectedTeamSeasons);
	}

	private void validateAmount(Season season, Player player, PlayerTeamRequest request) {
		if(request.getSoldAmount() == null) {
			throw new SplBadRequestException("Sold Amount is required");
		}else if(request.getIsFree() != null && request.getIsFree()) {
			if(BigDecimal.ZERO.compareTo(request.getSoldAmount()) != 0) {
				throw new SplBadRequestException("Sold Amount should be zero for free player");
			}
		}else if(request.getIsUnsold() != null && request.getIsUnsold()) {
			if(season.getMinPlayerAmount().compareTo(request.getSoldAmount()) != 0) {
				throw new SplBadRequestException("Sold Amount should be equals to Min Player Amount for unsold player");
			}
		}else {
			BigDecimal baseAmount = player.getPlayerLevel().getBaseAmount();
			if(baseAmount != null && baseAmount.compareTo(request.getSoldAmount()) > 0) {
				throw new SplBadRequestException("Sold Amount should not be less than Base Amount");
			}
		}
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

    private static final ExpressionParser parser = new SpelExpressionParser();
    
    public static double solveAndSetRemaining(PlayerLevelCalcDto root, String dbRule) {
    	StandardEvaluationContext context = new StandardEvaluationContext(root);
    	
    	// Expand simplified notation (l1.property -> playerLevels.l1.property)
    	String expandedDbRule = expandSimplifiedNotation(dbRule);
    	
    	// Parse the rule to extract left side, operator, and threshold
    	RuleComponents components = parseRule(expandedDbRule);
    	
    	// Convert dot notation to bracket notation for HashMap access
    	String convertedLeftSide = convertDotNotationInFormula(components.leftSide);
    	
    	// Evaluate the left side to get current total
    	Expression leftSideExpr = parser.parseExpression(convertedLeftSide);
    	Object leftSideValue = leftSideExpr.getValue(context);
    	double currentTotal = leftSideValue != null ? Double.parseDouble(leftSideValue.toString()) : 0.0;
    	
    	// Calculate remaining based on operator and threshold
    	double adjustedThreshold = adjustThresholdForOperator(components.threshold, components.operator);
    	double remaining = adjustedThreshold - currentTotal;
    	
    	// Round to 2 decimal places and ensure non-negative result
    	double roundedRemaining = Math.round(remaining * 100.0) / 100.0;
    	return Math.max(0, roundedRemaining);
    }
    
    private static class RuleComponents {
    	String leftSide;
    	String operator;
    	double threshold;
    	
    	RuleComponents(String leftSide, String operator, double threshold) {
    		this.leftSide = leftSide;
    		this.operator = operator;
    		this.threshold = threshold;
    	}
    }
    
    private static RuleComponents parseRule(String rule) {
    	// Parse rules like "l1.amount + l2.amount <= 100"
    	if (rule.contains(" <= ")) {
    		String[] parts = rule.split("\\s*<=\\s*");
    		return new RuleComponents(parts[0].trim(), "<=", Double.parseDouble(parts[1].trim()));
    	} else if (rule.contains(" >= ")) {
    		String[] parts = rule.split("\\s*>=\\s*");
    		return new RuleComponents(parts[0].trim(), ">=", Double.parseDouble(parts[1].trim()));
    	} else if (rule.contains(" < ")) {
    		String[] parts = rule.split("\\s*<\\s*");
    		return new RuleComponents(parts[0].trim(), "<", Double.parseDouble(parts[1].trim()));
    	} else if (rule.contains(" > ")) {
    		String[] parts = rule.split("\\s*>\\s*");
    		return new RuleComponents(parts[0].trim(), ">", Double.parseDouble(parts[1].trim()));
    	} else if (rule.contains(" == ") || rule.contains(" = ")) {
    		String[] parts = rule.split("\\s*(==|=)\\s*");
    		return new RuleComponents(parts[0].trim(), "==", Double.parseDouble(parts[1].trim()));
    	}
    	
    	throw new IllegalArgumentException("Unsupported rule format: " + rule);
    }
    
    private static double adjustThresholdForOperator(double threshold, String operator) {
    	switch (operator) {
    		case "<":
    			return threshold - 0.01;
    		case ">":
    			return threshold + 0.01;
    		case "<=":
    		case ">=":
    		case "==":
    		default:
    			return threshold;
    	}
    }
    
    private static String expandSimplifiedNotation(String input) {
    	// Convert simplified notation like "l1.totalAmountSpent" to "playerLevels.l1.totalAmountSpent"
    	// This handles patterns like l1.property, l2.property, etc.
    	return input.replaceAll("\\b(l\\d+)\\.", "playerLevels.$1.");
    }
    
    private static String convertDotNotationInFormula(String formula) {
    	// Convert all playerLevels.lX.property patterns to bracket notation
    	return formula.replaceAll("playerLevels\\.([a-zA-Z0-9]+)\\.", "playerLevels['$1'].");
    }
    
	private static String convertToSpelMapAccess(String dotNotation) {
		// Convert "playerLevels.l1.totalAmountSpent" to "playerLevels['l1'].totalAmountSpent"
		// Handle cases like "playerLevels.l1.totalAmountSpent" or "playerLevels.l2.maxBudget"
		
		if (!dotNotation.contains(".")) {
			return dotNotation;
		}
		
		String[] parts = dotNotation.split("\\.");
		if (parts.length < 3) {
			return dotNotation; // Not the expected format
		}
		
		// Check if it's a playerLevels map access pattern
		if ("playerLevels".equals(parts[0])) {
			StringBuilder result = new StringBuilder();
			result.append(parts[0]); // "playerLevels"
			result.append("['").append(parts[1]).append("']"); // "['l1']"
			
			// Add remaining parts with dot notation
			for (int i = 2; i < parts.length; i++) {
				result.append(".").append(parts[i]);
			}
			
			return result.toString();
		}
		
		// For other patterns, return as-is
		return dotNotation;
	}

	public static void main(String [] args){
		
		PlayerLevelCalcDto dto = getDummyData();
		
		// Test different comparison operators with rounding
		System.out.println("Remaining amount (<=): " + solveAndSetRemaining(dto, "l1.totalAmountSpent + l2.totalAmountSpent <= 100"));
		System.out.println("Remaining amount (<): " + solveAndSetRemaining(dto, "l1.totalAmountSpent + l2.totalAmountSpent < 100"));
		System.out.println("Remaining amount (>): " + solveAndSetRemaining(dto, "l1.totalAmountSpent + l2.totalAmountSpent > 100"));
		
		// Test with a threshold that would create more decimal places
		System.out.println("Remaining amount (< 99.999): " + solveAndSetRemaining(dto, "l1.totalAmountSpent + l2.totalAmountSpent < 99.999"));

	}

	private static PlayerLevelCalcDto getDummyData() {
		PlayerLevelCalcDto dto = new PlayerLevelCalcDto();
		Map<String,TeamSeasonPlayerLevel> playerLevels = new HashMap<String, TeamSeasonPlayerLevel>();
		
		TeamSeasonPlayerLevel l1 = new TeamSeasonPlayerLevel();
		l1.setTotalAmountSpent(new BigDecimal("35"));
		playerLevels.put("l1", l1);
		
		TeamSeasonPlayerLevel l2 = new TeamSeasonPlayerLevel();
		l2.setTotalAmountSpent(new BigDecimal("21"));
		playerLevels.put("l2", l2);
		dto.setPlayerLevels(playerLevels);
		return dto;
	}
}