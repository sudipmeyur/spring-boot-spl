package com.spl.spl.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.spl.spl.dto.ItemsResponse;
import com.spl.spl.dto.ItemsResponse.ItemsData;
import com.spl.spl.entity.PlayerCategory;
import com.spl.spl.service.PlayerCategoryService;
import com.spl.spl.views.Views;

@RestController
@RequestMapping("/api/player-categories")
@CrossOrigin(origins = "*")
public class PlayerCategoryController {

    @Autowired
    private PlayerCategoryService playerCategoryService;

    @GetMapping
    @JsonView(Views.Summary.class)
    public ResponseEntity<ItemsResponse<PlayerCategory>> getAllCategories() {
        List<PlayerCategory> categories = playerCategoryService.getAllActiveCategories();
        return ResponseEntity.ok(new ItemsResponse<>(new ItemsData<>(categories)));
    }
}