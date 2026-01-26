package com.spl.spl.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "unsold_player", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"player_id", "season_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Data
public class UnsoldPlayer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonView(Views.Base.class)
	private Long id;


	@JsonView(Views.Summary.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "player_id")
	private Player player;

	@JsonView(Views.Summary.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "season_id")
	private Season season;

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime createdAt;
}