package nus.iss.se.team9.recipe_service_team9.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredientDTO {
    private Integer id;
    private String foodText;
    private Double protein;
    private Double calories;
    private Double carbohydrate;
    private Double sugar;
    private Double sodium;
    private Double fat;
    private Double saturatedFat;

    @Override
    public String toString() {
        return foodText + " (" + id + ") " + protein + ", " + calories + ", " + carbohydrate + ", " + sugar + ", " + sodium + ", " + fat + ", " + saturatedFat;
    }

    public IngredientDTO() {
    }

    public IngredientDTO(String foodText, Object protein, Object calories, Object carbohydrate, Object sugar, Object sodium, Object fat, Object saturatedFat) {
        this.foodText = foodText;
        this.protein = handleNutritionData(protein);
        this.calories = handleNutritionData(calories);
        this.carbohydrate = handleNutritionData(carbohydrate);
        this.sugar = handleNutritionData(sugar);
        this.sodium = handleNutritionData(sodium);
        this.fat = handleNutritionData(fat);
        this.saturatedFat = handleNutritionData(saturatedFat);
    }

    private double handleNutritionData(Object nutritionData) {
        if (nutritionData instanceof Integer) {
            return ((Integer) nutritionData).doubleValue();
        } else if (nutritionData instanceof Double) {
            return (Double) nutritionData;
        } else {
            return 0.0;
        }
    }
}
