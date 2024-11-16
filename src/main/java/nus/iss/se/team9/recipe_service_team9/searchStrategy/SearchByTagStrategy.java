package nus.iss.se.team9.recipe_service_team9.searchStrategy;

import nus.iss.se.team9.recipe_service_team9.model.Recipe;
import nus.iss.se.team9.recipe_service_team9.service.RecipeService;

import java.util.List;

public class SearchByTagStrategy implements SearchStrategy {
    @Override
    public List<Recipe> search(String query, RecipeService recipeService) {
        return recipeService.searchByTag(query);
    }
}