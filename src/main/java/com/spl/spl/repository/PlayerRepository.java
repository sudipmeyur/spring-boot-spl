package com.spl.spl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spl.spl.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {
}
