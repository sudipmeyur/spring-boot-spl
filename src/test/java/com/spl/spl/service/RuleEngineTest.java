package com.spl.spl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.spl.spl.entity.RuleEntity;
import com.spl.spl.entity.Season;
import com.spl.spl.entity.TeamSeasonPlayerLevel;
import com.spl.spl.repository.RuleEntityRepository;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

    @Mock
    private RuleEntityRepository ruleEntityRepository;

    @InjectMocks
    private RuleEngine ruleEngine;

    private RuleEntity testRule;
    private PlayerLevelCalcDto testData;
    private Season testSeason;

    @BeforeEach
    void setUp() {
        testSeason = new Season();
        testSeason.setId(1L);

        testRule = new RuleEntity();
        testRule.setId(1L);
        testRule.setSeason(testSeason);
        testRule.setContext("player_budget");
        testRule.setDbRule("l1.totalAmountSpent + l2.totalAmountSpent <= 100");
        testRule.setNotationMap(Map.of("l", "playerLevels.l"));
        testRule.setMapNames(List.of("playerLevels"));
        testRule.setIsActive(true);

        testData = new PlayerLevelCalcDto();
        Map<String, TeamSeasonPlayerLevel> playerLevels = new HashMap<>();
        
        TeamSeasonPlayerLevel l1 = new TeamSeasonPlayerLevel();
        l1.setTotalAmountSpent(new BigDecimal("30"));
        playerLevels.put("l1", l1);
        
        TeamSeasonPlayerLevel l2 = new TeamSeasonPlayerLevel();
        l2.setTotalAmountSpent(new BigDecimal("20"));
        playerLevels.put("l2", l2);
        
        testData.setPlayerLevels(playerLevels);
    }

    @Test
    void getAllRules_ShouldReturnAllRules() {
        // Given
        List<RuleEntity> expectedRules = Arrays.asList(testRule);
        when(ruleEntityRepository.findAll()).thenReturn(expectedRules);

        // When
        List<RuleEntity> result = ruleEngine.getAllRules();

        // Then
        assertEquals(expectedRules, result);
        verify(ruleEntityRepository).findAll();
    }

    @Test
    void getAllRules_ShouldReturnEmptyListWhenNoRules() {
        // Given
        when(ruleEntityRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<RuleEntity> result = ruleEngine.getAllRules();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void saveRule_ShouldSaveAndReturnRule() {
        // Given
        when(ruleEntityRepository.save(testRule)).thenReturn(testRule);

        // When
        RuleEntity result = ruleEngine.saveRule(testRule);

        // Then
        assertEquals(testRule, result);
        verify(ruleEntityRepository).save(testRule);
    }

    @Test
    void saveRule_ShouldThrowExceptionWhenRuleIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.saveRule(null)
        );
        assertEquals("RuleEntity cannot be null", exception.getMessage());
    }

    @Test
    void getRulesBySeasonAndContext_ShouldReturnMatchingRules() {
        // Given
        Long seasonId = 1L;
        String context = "player_budget";
        List<RuleEntity> expectedRules = Arrays.asList(testRule);
        when(ruleEntityRepository.findBySeasonAndContext(seasonId, context)).thenReturn(expectedRules);

        // When
        List<RuleEntity> result = ruleEngine.getRulesBySeasonAndContext(seasonId, context);

        // Then
        assertEquals(expectedRules, result);
        verify(ruleEntityRepository).findBySeasonAndContext(seasonId, context);
    }

    @Test
    void getRulesBySeasonAndContext_ShouldThrowExceptionWhenSeasonIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.getRulesBySeasonAndContext(null, "context")
        );
        assertEquals("Season ID cannot be null", exception.getMessage());
    }

    @Test
    void getRulesBySeasonAndContext_ShouldThrowExceptionWhenContextIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.getRulesBySeasonAndContext(1L, null)
        );
        assertEquals("Context cannot be null or empty", exception.getMessage());
    }

    @Test
    void getRulesBySeasonAndContext_ShouldThrowExceptionWhenContextIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.getRulesBySeasonAndContext(1L, "")
        );
        assertEquals("Context cannot be null or empty", exception.getMessage());
    }

    @Test
    void evaluateRules_ShouldReturnRemainingAmounts() {
        // Given
        Long seasonId = 1L;
        String context = "player_budget";
        List<RuleEntity> rules = Arrays.asList(testRule);
        when(ruleEntityRepository.findBySeasonAndContext(seasonId, context)).thenReturn(rules);

        // When
        List<Double> result = ruleEngine.evaluateRules(seasonId, context, testData);

        // Then
        assertEquals(1, result.size());
        assertEquals(50.0, result.get(0)); // 100 - (30 + 20) = 50
    }

    @Test
    void evaluateRules_ShouldReturnEmptyListWhenNoRules() {
        // Given
        Long seasonId = 1L;
        String context = "player_budget";
        when(ruleEntityRepository.findBySeasonAndContext(seasonId, context)).thenReturn(Collections.emptyList());

        // When
        List<Double> result = ruleEngine.evaluateRules(seasonId, context, testData);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void evaluateRules_ShouldThrowExceptionWhenSeasonIdIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.evaluateRules(null, "context", testData)
        );
        assertEquals("Season ID cannot be null", exception.getMessage());
    }

    @Test
    void evaluateRules_ShouldThrowExceptionWhenContextIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.evaluateRules(1L, null, testData)
        );
        assertEquals("Context cannot be null or empty", exception.getMessage());
    }

    @Test
    void evaluateRules_ShouldThrowExceptionWhenDataIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.evaluateRules(1L, "context", null)
        );
        assertEquals("PlayerLevelCalcDto cannot be null", exception.getMessage());
    }

    @Test
    void evaluateRule_ShouldCalculateRemainingAmount() {
        // When
        double result = ruleEngine.evaluateRule(testData, testRule);

        // Then
        assertEquals(50.0, result); // 100 - (30 + 20) = 50
    }

    @Test
    void evaluateRule_ShouldReturnZeroWhenExceedsThreshold() {
        // Given
        testRule.setDbRule("l1.totalAmountSpent + l2.totalAmountSpent <= 40");

        // When
        double result = ruleEngine.evaluateRule(testData, testRule);

        // Then
        assertEquals(0.0, result); // Max(0, 40 - 50) = 0
    }

    @Test
    void evaluateRule_ShouldHandleLessThanOperator() {
        // Given
        testRule.setDbRule("l1.totalAmountSpent + l2.totalAmountSpent < 100");

        // When
        double result = ruleEngine.evaluateRule(testData, testRule);

        // Then
        assertEquals(49.99, result); // 99.99 - 50 = 49.99
    }

    @Test
    void evaluateRule_ShouldHandleGreaterThanOperator() {
        // Given
        testRule.setDbRule("l1.totalAmountSpent + l2.totalAmountSpent > 40");

        // When
        double result = ruleEngine.evaluateRule(testData, testRule);

        // Then
        assertEquals(0.0, result); // Already exceeds 40.01
    }

    @Test
    void evaluateRule_ShouldHandleGreaterThanEqualOperator() {
        // Given
        testRule.setDbRule("l1.totalAmountSpent + l2.totalAmountSpent >= 60");

        // When
        double result = ruleEngine.evaluateRule(testData, testRule);

        // Then
        assertEquals(10.0, result); // 60 - 50 = 10
    }

    @Test
    void evaluateRule_ShouldHandleEqualOperator() {
        // Given
        testRule.setDbRule("l1.totalAmountSpent + l2.totalAmountSpent == 70");

        // When
        double result = ruleEngine.evaluateRule(testData, testRule);

        // Then
        assertEquals(20.0, result); // 70 - 50 = 20
    }

    @Test
    void evaluateRule_ShouldThrowExceptionWhenRootIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.evaluateRule(null, testRule)
        );
        assertEquals("PlayerLevelCalcDto cannot be null", exception.getMessage());
    }

    @Test
    void evaluateRule_ShouldThrowExceptionWhenRuleEntityIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.evaluateRule(testData, null)
        );
        assertEquals("RuleEntity cannot be null", exception.getMessage());
    }

    @Test
    void evaluateRule_ShouldThrowExceptionWhenDbRuleIsNull() {
        // Given
        testRule.setDbRule(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.evaluateRule(testData, testRule)
        );
        assertEquals("Rule dbRule cannot be null or empty", exception.getMessage());
    }

    @Test
    void evaluateRule_ShouldThrowExceptionWhenDbRuleIsEmpty() {
        // Given
        testRule.setDbRule("");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.evaluateRule(testData, testRule)
        );
        assertEquals("Rule dbRule cannot be null or empty", exception.getMessage());
    }

    @Test
    void evaluateRule_ShouldHandleNullNotationMap() {
        // Given
        testRule.setNotationMap(null);
        testRule.setDbRule("playerLevels.l1.totalAmountSpent <= 50");

        // When
        double result = ruleEngine.evaluateRule(testData, testRule);

        // Then
        assertEquals(20.0, result); // 50 - 30 = 20
    }

    @Test
    void evaluateRule_ShouldHandleNullMapNames() {
        // Given
        testRule.setMapNames(null);
        testRule.setDbRule("l1.totalAmountSpent <= 50");
        testRule.setNotationMap(Map.of("l", "playerLevels.l"));

        // When
        double result = ruleEngine.evaluateRule(testData, testRule);

        // Then
        assertEquals(20.0, result); // 50 - 30 = 20
    }

    @Test
    void evaluateRule_ShouldRoundToTwoDecimalPlaces() {
        // Given
        testRule.setDbRule("l1.totalAmountSpent <= 30.333");

        // When
        double result = ruleEngine.evaluateRule(testData, testRule);

        // Then
        assertEquals(0.33, result); // 30.333 - 30 = 0.333 -> rounded to 0.33
    }

    @Test
    void evaluateRule_ShouldThrowExceptionForUnsupportedRuleFormat() {
        // Given
        testRule.setDbRule("l1.totalAmountSpent INVALID 100");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ruleEngine.evaluateRule(testData, testRule)
        );
        assertTrue(exception.getMessage().contains("Unsupported rule format"));
    }

    @Test
    void evaluateRule_ShouldHandleNullSpelValue() {
        // Given - Create data that will result in null SpEL evaluation
        PlayerLevelCalcDto emptyData = new PlayerLevelCalcDto();
        emptyData.setPlayerLevels(new HashMap<>());
        testRule.setDbRule("l1.totalAmountSpent <= 100");

        // When
        double result = ruleEngine.evaluateRule(emptyData, testRule);

        // Then
        assertEquals(100.0, result); // 100 - 0 = 100 (null treated as 0)
    }
}