package com.spl.spl.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.views.Views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "player_category")
@Data
public class PlayerCategory {

    @Id
    @JsonView(Views.Summary.class)
    private String code;
    
    @JsonView(Views.Summary.class)
    @Column(nullable = false)
    private String name;
    
    @JsonView(Views.Summary.class)
    @Column(name = "icon_path")
    private String iconPath;
    
    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;
}