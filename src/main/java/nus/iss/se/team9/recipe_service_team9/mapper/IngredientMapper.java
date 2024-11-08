package nus.iss.se.team9.recipe_service_team9.mapper;

import nus.iss.se.team9.recipe_service_team9.model.Ingredient;
import nus.iss.se.team9.recipe_service_team9.model.IngredientDTO;

public class IngredientMapper {
    public static Ingredient toIngredient(IngredientDTO ingredientDTO) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(ingredientDTO.getId());
        ingredient.setFoodText(ingredientDTO.getFoodText());
        ingredient.setProtein(ingredientDTO.getProtein());
        ingredient.setCalories(ingredientDTO.getCalories());
        ingredient.setCarbohydrate(ingredientDTO.getCarbohydrate());
        ingredient.setSugar(ingredientDTO.getSugar());
        ingredient.setSodium(ingredientDTO.getSodium());
        ingredient.setFat(ingredientDTO.getFat());
        ingredient.setSaturatedFat(ingredientDTO.getSaturatedFat());
        return ingredient;
    }
}
