package com.c019shranth.madproject.models;

import java.io.Serializable;

public class Recipe implements Serializable {

    private String id;
    private String name;
    private String image;
    private String description;
    private String category;
    private String instructions;
    private String ingredients;
    private String calories;
    private String time;
    private String authorId;
    private String servings;

    public Recipe() {
    }

    public Recipe(String id, String name, String image, String description, String category, String instructions, String ingredients, String calories, String time, String servings) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.category = category;
        this.instructions = instructions;
        this.ingredients = ingredients;
        this.calories = calories;
        this.time = time;
        this.servings = servings;
    }

    public Recipe(String id) {
        this.id = id;
    }

    public Recipe(String recipeName, String recipeDescription, String cookingTime, String calories, String recipeCategory, String image, String authorId) {
        this.name = recipeName;
        this.description = recipeDescription;
        this.time = cookingTime;
        this.calories = calories;
        this.category = recipeCategory;
        this.image = image;
        this.authorId = authorId;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getServings() {
        return servings;
    }

    public void setServings(String servings) {
        this.servings = servings;
    }

    public String getAuthorId() {
        return authorId; // Added return statement
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId; // Added setter for authorId
    }
}
