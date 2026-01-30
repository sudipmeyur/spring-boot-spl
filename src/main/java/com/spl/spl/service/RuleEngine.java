package com.spl.spl.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.spl.spl.dto.PlayerLevelCalcDto;
import com.spl.spl.entity.RuleEntity;
import com.spl.spl.repository.RuleEntityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RuleEngine {
    
    private final RuleEntityRepository ruleEntityRepository;
    private static final ExpressionParser parser = new SpelExpressionParser();
    
    /**
     * Retrieves all rule entities from the database.
     * 
     * @return List of all RuleEntity objects, empty list if none found
     * @example getAllRules() -> [RuleEntity{id=1, context="player_budget"}, ...]
     */
    public List<RuleEntity> getAllRules() {
        return ruleEntityRepository.findAll();
    }
    
    /**
     * Saves a rule entity to the database.
     * 
     * @param ruleEntity The rule entity to save (must not be null)
     * @return The saved RuleEntity with generated ID
     * @throws IllegalArgumentException if ruleEntity is null
     * @example saveRule(new RuleEntity(...)) -> RuleEntity{id=5, ...}
     */
    public RuleEntity saveRule(RuleEntity ruleEntity) {
        if (ruleEntity == null) {
            throw new IllegalArgumentException("RuleEntity cannot be null");
        }
        return ruleEntityRepository.save(ruleEntity);
    }
    
    /**
     * Retrieves rules filtered by season ID and context.
     * 
     * @param seasonId The season ID to filter by (must not be null)
     * @param context The context to filter by (must not be null or empty)
     * @return List of matching RuleEntity objects, empty list if none found
     * @throws IllegalArgumentException if seasonId is null or context is null/empty
     * @example getRulesBySeasonAndContext(1L, "player_budget") -> [RuleEntity{...}, ...]
     */
    public List<RuleEntity> getRulesBySeasonAndContext(Long seasonId, String context) {
        if (seasonId == null) {
            throw new IllegalArgumentException("Season ID cannot be null");
        }
        if (!StringUtils.hasText(context)) {
            throw new IllegalArgumentException("Context cannot be null or empty");
        }
        return ruleEntityRepository.findBySeasonAndContext(seasonId, context);
    }
    
    /**
     * Evaluates multiple rules for a given season, context, and data.
     * 
     * @param seasonId The season ID (must not be null)
     * @param context The context (must not be null or empty)
     * @param data The calculation data (must not be null)
     * @return List of remaining amounts for each rule, empty list if no rules found
     * @throws IllegalArgumentException if any parameter is null/invalid
     * @example evaluateRules(1L, "budget", data) -> [25.5, 0.0, 100.0]
     */
    public List<Double> evaluateRules(Long seasonId, String context, PlayerLevelCalcDto data) {
        if (seasonId == null) {
            throw new IllegalArgumentException("Season ID cannot be null");
        }
        if (!StringUtils.hasText(context)) {
            throw new IllegalArgumentException("Context cannot be null or empty");
        }
        if (data == null) {
            throw new IllegalArgumentException("PlayerLevelCalcDto cannot be null");
        }
        
        List<RuleEntity> rules = getRulesBySeasonAndContext(seasonId, context);
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        
        return rules.stream()
            .map(rule -> evaluateRule(data, rule))
            .collect(Collectors.toList());
    }
    
    /**
     * Evaluates a single rule against provided data and returns remaining amount.
     * Processes rule through: expansion -> parsing -> conversion -> SpEL evaluation.
     * 
     * @param root The data context for evaluation (must not be null)
     * @param ruleEntity The rule to evaluate (must not be null with valid dbRule)
     * @return Remaining amount (non-negative, rounded to 2 decimal places)
     * @throws IllegalArgumentException if parameters are null or rule is invalid
     * @example evaluateRule(data, rule{"l1.amount <= 100"}) -> 25.5 (if current is 74.5)
     */
    public double evaluateRule(PlayerLevelCalcDto root, RuleEntity ruleEntity) {
        if (root == null) {
            throw new IllegalArgumentException("PlayerLevelCalcDto cannot be null");
        }
        if (ruleEntity == null) {
            throw new IllegalArgumentException("RuleEntity cannot be null");
        }
        if (!StringUtils.hasText(ruleEntity.getDbRule())) {
            throw new IllegalArgumentException("Rule dbRule cannot be null or empty");
        }
        
        StandardEvaluationContext context = new StandardEvaluationContext(root);
        
        // Step 1: Expand simplified notation (l1.amount -> playerLevels.l1.amount)
        String expandedDbRule = expandSimplifiedNotation(ruleEntity.getDbRule(), ruleEntity.getNotationMap());
        
        // Step 2: Parse rule components (left side, operator, threshold)
        RuleComponents components = parseRule(expandedDbRule);
        
        // Step 3: Convert to SpEL bracket notation (playerLevels.l1 -> playerLevels['l1'])
        String convertedLeftSide = convertDotNotationInFormula(components.leftSide, 
                ruleEntity.getMapNames() != null ? ruleEntity.getMapNames().toArray(new String[0]) : new String[0]);
        
        // Step 4: Evaluate SpEL expression
        Expression leftSideExpr = parser.parseExpression(convertedLeftSide);
        Object leftSideValue;
        try {
            leftSideValue = leftSideExpr.getValue(context);
        } catch (Exception e) {
            // Handle cases where SpEL evaluation fails (e.g., null properties)
            leftSideValue = null;
        }
        double currentTotal = leftSideValue != null ? Double.parseDouble(leftSideValue.toString()) : 0.0;
        
        // Step 5: Calculate remaining amount
        double adjustedThreshold = adjustThresholdForOperator(components.threshold, components.operator);
        double remaining = adjustedThreshold - currentTotal;
        
        return Math.max(0, Math.round(remaining * 100.0) / 100.0);
    }
    
    /**
     * Internal class to hold parsed rule components.
     */
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
    
    /**
     * Parses a rule string into components (left side, operator, threshold).
     * Supports operators: <=, >=, <, >, ==, =
     * 
     * @param rule The rule string to parse (must not be null or empty)
     * @return RuleComponents containing parsed elements
     * @throws IllegalArgumentException if rule format is unsupported or null/empty
     * @example parseRule("l1.amount + l2.amount <= 100") -> RuleComponents{leftSide="l1.amount + l2.amount", operator="<=", threshold=100.0}
     */
    private RuleComponents parseRule(String rule) {
        if (!StringUtils.hasText(rule)) {
            throw new IllegalArgumentException("Rule cannot be null or empty");
        }
        
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
    
    /**
     * Adjusts threshold values for strict inequality operators.
     * Adds/subtracts 0.01 for < and > operators to handle boundary conditions.
     * 
     * @param threshold The original threshold value
     * @param operator The comparison operator (must not be null)
     * @return Adjusted threshold value
     * @example adjustThresholdForOperator(100.0, "<") -> 99.99
     * @example adjustThresholdForOperator(50.0, ">=") -> 50.0
     */
    private double adjustThresholdForOperator(double threshold, String operator) {
        if (operator == null) {
            return threshold;
        }
        
        switch (operator) {
            case "<": return threshold - 0.01;
            case ">": return threshold + 0.01;
            case "<=":
            case ">=":
            case "==":
            default: return threshold;
        }
    }
    
    /**
     * Expands simplified notation using configurable pattern-replacement mappings.
     * Transforms shorthand like "l1.amount" to full paths like "playerLevels.l1.amount".
     * 
     * @param input The input string with simplified notation (returns as-is if null)
     * @param patternReplacements Map of pattern->replacement mappings (ignored if null)
     * @return Expanded string with full notation
     * @example expandSimplifiedNotation("l1.amount + l2.count", {"l": "playerLevels.l"}) -> "playerLevels.l1.amount + playerLevels.l2.count"
     */
    private String expandSimplifiedNotation(String input, Map<String, String> patternReplacements) {
        if (input == null) {
            return null;
        }
        if (patternReplacements == null || patternReplacements.isEmpty()) {
            return input;
        }
        
        String result = input;
        for (Map.Entry<String, String> entry : patternReplacements.entrySet()) {
            String pattern = entry.getKey();
            String replacement = entry.getValue();
            
            if (StringUtils.hasText(pattern) && StringUtils.hasText(replacement)) {
                // Use word boundaries and escape special regex characters
                String regexPattern = "\\b(" + Pattern.quote(pattern) + "\\d+)\\.";
                // Build replacement string - replace only the exact pattern at the end
                String replacementTemplate;
                if (replacement.endsWith("." + pattern)) {
                    // Handle cases like "playerLevels.l" -> "playerLevels.$1"
                    replacementTemplate = replacement.substring(0, replacement.length() - pattern.length()) + "$1";
                } else {
                    // Fallback: replace first occurrence
                    replacementTemplate = replacement.replaceFirst(Pattern.quote(pattern), "$1");
                }
                result = result.replaceAll(regexPattern, replacementTemplate + ".");
            }
        }
        return result;
    }
    
    /**
     * Converts dot notation to bracket notation for SpEL Map access.
     * Transforms "mapName.key.property" to "mapName['key'].property" for proper SpEL evaluation.
     * 
     * @param formula The formula string to convert (returns as-is if null)
     * @param mapNames Variable arguments of map names to convert (ignored if null/empty)
     * @return Formula with bracket notation for specified maps
     * @example convertDotNotationInFormula("playerLevels.l1.amount", "playerLevels") -> "playerLevels['l1'].amount"
     */
    private String convertDotNotationInFormula(String formula, String... mapNames) {
        if (formula == null) {
            return null;
        }
        if (mapNames == null || mapNames.length == 0) {
            // If no mapNames specified, convert playerLevels dot notation specifically
            return formula.replaceAll("playerLevels\\.(\\w+)\\.", "playerLevels['$1'].");
        }
        
        String result = formula;
        for (String mapName : mapNames) {
            if (StringUtils.hasText(mapName)) {
                result = result.replaceAll(mapName + "\\.([a-zA-Z0-9]+)\\.", mapName + "['$1'].");
            }
        }
        return result;
    }
}