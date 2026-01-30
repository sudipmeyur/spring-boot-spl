package com.spl.spl.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spl.spl.entity.RuleEntity;
import com.spl.spl.service.RuleEngine;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {
    
    private final RuleEngine ruleEngine;
    
    @GetMapping
    public ResponseEntity<List<RuleEntity>> getAllRules(
            @RequestParam(required = false) Long seasonId,
            @RequestParam(required = false) String context) {
        
        List<RuleEntity> rules;
        if (seasonId != null && context != null) {
            rules = ruleEngine.getRulesBySeasonAndContext(seasonId, context);
        } else {
            rules = ruleEngine.getAllRules();
        }
        
        return ResponseEntity.ok(rules);
    }
    
    @PostMapping
    public ResponseEntity<RuleEntity> saveRule(@RequestBody RuleEntity ruleEntity) {
        RuleEntity savedRule = ruleEngine.saveRule(ruleEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRule);
    }
}