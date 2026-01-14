package com.spl.spl.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "player")
@Data
public class Player {

	@Id
	private Long id;
	private String name;
	private String imageUrl;

}
