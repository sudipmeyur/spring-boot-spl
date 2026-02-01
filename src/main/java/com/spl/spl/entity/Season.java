package com.spl.spl.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.views.Views;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "season", uniqueConstraints = {
	@UniqueConstraint(columnNames = "code")
})
@Data
public class Season {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonView(Views.Base.class)
	private Long id;

	@JsonView(Views.Base.class)
	@Column(unique = true)
	private String code;
	
	private Integer year;
	
	@JsonView(Views.Summary.class)
	private BigDecimal minPlayerAmount;
	
	@JsonView(Views.Summary.class)
	private BigDecimal budgetLimit;
	
	@JsonView(Views.Summary.class)
	private Integer maxPlayersAllowed;
	
	@JsonView(Views.Summary.class)
	private Integer maxRtmAllowed;
	
	@JsonView(Views.Summary.class)
	private Integer maxFreeAllowed;
	
	@JsonView(Views.Base.class)
	@Column(columnDefinition = "boolean default false")
	private Boolean isAuctionCompleted = false;
	
	@Column(nullable = true)
	private String auctionCompletionNote;
}
