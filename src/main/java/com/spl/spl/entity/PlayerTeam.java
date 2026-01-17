package com.spl.spl.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
	private Player player;

	@Id
	@ManyToOne
	@JoinColumn(name = "team_season_id")
	private TeamSeason teamSeason;

	private BigDecimal soldAmount;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;
}
