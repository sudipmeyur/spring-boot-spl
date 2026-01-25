package com.spl.spl.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.views.Views;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "team_season_player_level", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"team_season_id", "player_level_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Data
public class TeamSeasonPlayerLevel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_season_id")
	@JsonIgnore
	private TeamSeason teamSeason;

	@JsonView(Views.Summary.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_level_id")
	private PlayerLevel playerLevel;

	@JsonView(Views.Summary.class)
	private BigDecimal totalAmountSpent;
	
	@JsonView(Views.Summary.class)
	private Integer totalPlayerCount;

	@LastModifiedDate
	private LocalDateTime updatedAt;
}