package com.c019shranth.madproject.models;

public class Category {
    private String id;
    private String name;
    private String image;

    public Category() {
    }

    public Category(String id, String image, String name) {
        this.id = id;
        this.image = image;
        this.name = name;
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

    public String getImage() { // Change the return type to String
        return image;
    }

    public void setImage(String image) { // Change parameter type to String
        this.image = image;
    }
}
