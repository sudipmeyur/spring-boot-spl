package com.spl.spl.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spl.spl.entity.PlayerCategory;
import com.spl.spl.repository.PlayerCategoryRepository;

@Service
public class PlayerCategoryService {

    @Autowired
    private PlayerCategoryRepository playerCategoryRepository;

    public List<PlayerCategory> getAllActiveCategories() {
        return playerCategoryRepository.findByIsActiveTrue();
    }
}