package com.spl.spl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.spl.spl.dto.PlayerLevelCalcDto;
import com.spl.spl.entity.Rule;
import com.spl.spl.entity.Season;
import com.spl.spl.entity.TeamSeason;
import com.spl.spl.entity.TeamSeasonPlayerLevel;
import com.spl.spl.repository.SeasonRuleRepository;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

    @Mock
    private SeasonRuleRepository seasonRuleRepository;

    @InjectMocks
    private RuleEngine ruleEngine;

    private Rule testRule;
    private PlayerLevelCalcDto testData;
    private Season testSeason;

    @BeforeEach
    void setUp() {
        testSeason = new Season();
        testSeason.setId(1L);

        testRule = new Rule();
        testRule.setId(1L);
        testRule.setContext("player_budget");
        testRule.setRuleStatement("l1.totalAmountSpent + l2.totalAmountSpent <= 100");
        testRule.setNotationMap(Map.of("l", "playerLevels.l"));
        testRule.setMapNames(List.of("playerLevels"));
        testRule.setIsActive(true);

        
        Map<String, TeamSeasonPlayerLevel> playerLevels = new HashMap<>();
        
        TeamSeasonPlayerLevel l1 = new TeamSeasonPlayerLevel();
        l1.setTotalAmountSpent(new BigDecimal("30"));
        playerLevels.put("l1", l1);
        
        TeamSeasonPlayerLevel l2 = new TeamSeasonPlayerLevel();
        l2.setTotalAmountSpent(new BigDecimal("20"));
        playerLevels.put("l2", l2);
        
        testData = PlayerLevelCalcDto.builder().playerLevels(playerLevels).build();
    }

   

    @Test
    void getRulesBySeasonAndContext_ShouldReturnMatchingRules() {
        Long seasonId = 1L;
        String context = "player_budget";
        List<Rule> expectedRules = Arrays.asList(testRule);
        when(seasonRuleRepository.findBySeasonIdAndRuleContext(seasonId, context)).thenReturn(expectedRules);

        List<Rule> result = ruleEngine.getRulesBySeasonAndContext(seasonId, context);

        assertEquals(expectedRules, result);
        verify(seasonRuleRepository).findBySeasonIdAndRuleContext(seasonId, context);
    }

    @Test
    void evaluateRule_ShouldCalculateRemainingAmount() {
        double result = ruleEngine.evaluateRule(testData, testRule);
        assertEquals(50.0, result);
    }

    @Test
    void evaluateRule_WithPlayerBudgetValidation_ShouldCalculateCorrectly() {
        // Create season with budget validation data
        Season season = new Season();
        season.setId(6L);
        season.setCode("s6");
        season.setYear(2026);
        season.setBudgetLimit(new BigDecimal("100.00"));
        season.setMaxPlayersAllowed(11);
        season.setMinPlayerAmount(new BigDecimal("2.00"));
        season.setMaxFreeAllowed(1);
        season.setMaxRtmAllowed(1);

        // Create team season
        TeamSeason team = new TeamSeason();
        team.setTotalAmountSpent(new BigDecimal("50.00"));
        team.setTotalPlayer(8);

        // Create rule for budget validation
        Rule budgetRule = new Rule();
        budgetRule.setId(1L);
        budgetRule.setContext("player_budget_validation");
        budgetRule.setRuleStatement("team.totalAmountSpent + ((season.maxPlayersAllowed - team.totalPlayer)-1) * season.minPlayerAmount <= 100");
        budgetRule.setRuleName("Total Budget Limit");
        budgetRule.setRuleCategory("common");
        budgetRule.setPriority(1);
        budgetRule.setIsActive(true);

        // Create PlayerLevelCalcDto
        PlayerLevelCalcDto calcDto = PlayerLevelCalcDto.builder()
                .team(team)
                .season(season)
                .playerLevels(Collections.emptyMap())
                .build();

        double result = ruleEngine.evaluateRule(calcDto, budgetRule);
        
        // Expected: 100 - (50 + (11-8-1) * 2) = 100 - (50 + 4) = 46
        assertEquals(46.0, result);
    }
}