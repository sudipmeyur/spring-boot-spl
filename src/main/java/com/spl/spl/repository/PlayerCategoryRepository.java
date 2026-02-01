package com.spl.spl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spl.spl.entity.PlayerCategory;

@Repository
public interface PlayerCategoryRepository extends JpaRepository<PlayerCategory, String> {
    List<PlayerCategory> findByIsActiveTrue();
}