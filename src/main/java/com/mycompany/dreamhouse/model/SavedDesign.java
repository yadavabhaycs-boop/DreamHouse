package com.mycompany.dreamhouse.model;

public record SavedDesign(long id, String category, double width, double length,
        String name, String description, String itemsJson, String createdAt) {
}
