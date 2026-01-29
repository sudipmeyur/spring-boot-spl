package com.spl.spl.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.views.Views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "team_season", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"team_id", "season_id"}),
	@UniqueConstraint(columnNames = "code")
})
@EntityListeners(AuditingEntityListener.class)
@Data
public class TeamSeason {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonView(Views.Base.class)
	private Long id;

	@JsonView(Views.Base.class)
	@Column(unique = true)
	private String code;

	@JsonView(Views.Summary.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

	@JsonView(Views.Summary.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "season_id")
	private Season season;

	@JsonView(Views.Summary.class)
	private BigDecimal totalAmountSpent;
	
	@JsonView(Views.Summary.class)
	private Integer totalRtmUsed;
	
	@JsonView(Views.Summary.class)
	private Integer totalFreeUsed;
	
	@JsonView(Views.Summary.class)
	private Integer totalPlayer;
	
	@JsonView(Views.Summary.class)
	@Column(columnDefinition = "boolean default false")
	private Boolean isRtmEligible;
	
	@JsonView(Views.TeamSeasonView.class)
	@OneToMany(mappedBy = "teamSeason", fetch = FetchType.LAZY)
	private List<PlayerTeam> playerTeams;

	@JsonView(Views.Summary.class)
	@OneToMany(mappedBy = "teamSeason", fetch = FetchType.LAZY)
	private List<TeamSeasonPlayerLevel> teamSeasonPlayerLevels;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;
}