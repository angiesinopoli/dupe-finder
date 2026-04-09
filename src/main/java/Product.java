package com.example.dupeFinder;

import java.util.List;

public class Product {
    private String product_name;
    private String categories;
    private List<String> ingredients_tags;

    public String getProduct_name() { return product_name; }
    public void setProduct_name(String product_name) { this.product_name = product_name; }

    public String getCategories() { return categories; }
    public void setCategories(String categories) { this.categories = categories; }

    public List<String> getIngredients_tags() { return ingredients_tags; }
    public void setIngredients_tags(List<String> ingredients_tags) { this.ingredients_tags = ingredients_tags; }
}