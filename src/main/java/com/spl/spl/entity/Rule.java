package com.spl.spl.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.spl.spl.converter.ListStringConverter;
import com.spl.spl.converter.MapStringConverter;

import lombok.Data;

@Entity
@Table(name = "rule")
@Data
public class Rule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String context;
    
    @Column(length = 100)
    private String ruleCategory;
    
    @Column(length = 200)
    private String ruleName;
    
    @Column(nullable = false, length = 500)
    private String ruleStatement;
    
    @Convert(converter = MapStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, String> notationMap;
    
    @Convert(converter = ListStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> mapNames;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Integer priority = 0;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}