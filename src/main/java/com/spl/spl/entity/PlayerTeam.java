package com.spl.spl.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.views.Views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "player_team")
@IdClass(PlayerTeamId.class)
@EntityListeners(AuditingEntityListener.class)
@Data
public class PlayerTeam {
	
	private String code;

	@Id
	@ManyToOne
	@JoinColumn(name = "player_id")
	@JsonView(Views.Summary.class)
	private Player player;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_season_id")
	@JsonIgnore
	private TeamSeason teamSeason;

	@JsonView(Views.Summary.class)
	private BigDecimal soldAmount;
	
	@Column(columnDefinition = "boolean default false")
	@JsonView(Views.Summary.class)
	private Boolean isManager;
	
	@Column(columnDefinition = "boolean default false")
	@JsonView(Views.Summary.class)
	private Boolean isRtmUsed;
	
	@JsonView(Views.Summary.class)
	@Column(columnDefinition = "boolean default false")
	private Boolean isFree;
	
	@JsonView(Views.Summary.class)
	@Column(columnDefinition = "boolean default false")
	private Boolean wasUnsold;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;
}
