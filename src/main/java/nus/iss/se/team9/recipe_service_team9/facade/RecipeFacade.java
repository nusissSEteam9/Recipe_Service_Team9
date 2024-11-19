package nus.iss.se.team9.recipe_service_team9.facade;

import nus.iss.se.team9.recipe_service_team9.mapper.IngredientMapper;
import nus.iss.se.team9.recipe_service_team9.model.*;
import nus.iss.se.team9.recipe_service_team9.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecipeFacade implements IRecipeFacade {
    @Autowired
    private RecipeService recipeService;
    @Autowired
    private IngredientService ingredientService;
    @Autowired
    private UserService userService;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private JwtService jwtService;

    @Override
    public Recipe handleNewRecipe(Map<String, Object> payload, Member member, String imageUrl) throws Exception {

        Map<String, Object> nutritionData = (Map<String, Object>) payload.get("nutrition");
        List<String> steps = (List<String>) payload.get("steps");
        List<String> tags = (List<String>) payload.get("tags");
        Recipe recipe = new Recipe((String) payload.get("name"), (String) payload.get("description"),
                                   (String) payload.get("notes"), imageUrl, 0.0,
                                   (Integer) payload.get("preparationTime"), (Integer) payload.get("servings"),
                                   steps.size(), member, handleNutritionData(nutritionData.get("calories")),
                                   handleNutritionData(nutritionData.get("protein")),
                                   handleNutritionData(nutritionData.get("carbohydrate")),
                                   handleNutritionData(nutritionData.get("sugar")),
                                   handleNutritionData(nutritionData.get("sodium")),
                                   handleNutritionData(nutritionData.get("fat")),
                                   handleNutritionData(nutritionData.get("saturated_fat")), steps, tags);
        setRecipeNutrients(recipe);
        recipe.setHealthScore(recipe.calculateHealthScore());
        return recipe;
    }

    @Override
    public void handleSaveRecipe(Recipe recipe) throws Exception {
        recipeService.save(recipe);
    }

    @Override
    public Member handleGetMemberById(Integer memberId) throws Exception {
        return userService.getMemberById(memberId);
    }

    @Override
    public Integer handleExtractId(String token) throws Exception {
        Integer memberId = jwtService.extractId(token);
        if (memberId == null) {
            throw new Exception("Unauthorized access");
        }
        return memberId;
    }

    @Override
    public void handleSetIngredients(List<Map<String, Object>> ingredientsPayload, Recipe recipe) throws Exception {
        List<Ingredient> ingredients = new ArrayList<>();
        for (Map<String, Object> ingredientData : ingredientsPayload) {
            IngredientDTO ingredientDTO = new IngredientDTO(ingredientData.get("food_text")
                                                                          .toString(),
                                                            ingredientData.get("protein"),
                                                            ingredientData.get("calories"),
                                                            ingredientData.get("carbohydrate"),
                                                            ingredientData.get("sugar"),
                                                            ingredientData.get("sodium"), ingredientData.get("fat"),
                                                            ingredientData.get("saturated_fat"));
            Ingredient ingredient = IngredientMapper.toIngredient(ingredientDTO);
            ingredient.getRecipes()
                      .add(recipe);
            ingredientService.saveIngredient(ingredient);
            ingredients.add(ingredient);
        }
        recipe.setIngredients(ingredients);
    }

    @Override
    public String handleImageUpload(MultipartFile file) throws Exception {
        String fileName = UUID.randomUUID().toString();
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = file.getOriginalFilename()
                                .substring(file.getOriginalFilename()
                                               .lastIndexOf("."));
        }
        String keyName = fileName + fileExtension;
        s3Service.uploadObject(file, keyName);
        return s3Service.getObjectUrl(keyName);
    }

    @Override
    public Recipe handleGetRecipeById(Integer recipeId) throws Exception {
        Recipe recipe = recipeService.getRecipeById(recipeId);
        if (recipe == null) {
            throw new Exception("Recipe not found");
        }
        return recipe;
    }

    @Override
    public void handleUpdateRecipe(Map<String, Object> payload, Recipe recipe, String imageUrl) throws Exception {
        recipe.setName((String) payload.get("name"));
        recipe.setDescription((String) payload.get("description"));
        recipe.setNotes((String) payload.get("notes"));
        recipe.setPreparationTime((Integer) payload.get("preparationTime"));
        recipe.setServings((Integer) payload.get("servings"));
        recipe.setSteps((List<String>) payload.get("steps"));
        recipe.setTags((List<String>) payload.get("tags"));
        recipe.setImage((String) payload.get("image"));
        recipe.setStatus(Status.valueOf(((String) payload.get("status")).toUpperCase()));

        Map<String, Object> nutritionData = (Map<String, Object>) payload.get("nutrition");
        recipe.setCalories(handleNutritionData(nutritionData.get("calories")));
        recipe.setProtein(handleNutritionData(nutritionData.get("protein")));
        recipe.setCarbohydrate(handleNutritionData(nutritionData.get("carbohydrate")));
        recipe.setSugar(handleNutritionData(nutritionData.get("sugar")));
        recipe.setSodium(handleNutritionData(nutritionData.get("sodium")));
        recipe.setFat(handleNutritionData(nutritionData.get("fat")));
        recipe.setSaturatedFat(handleNutritionData(nutritionData.get("saturatedFat")));
        setRecipeNutrients(recipe);
        recipe.setHealthScore(recipe.calculateHealthScore());
    }

    @Override
    public void handleDeleteIngredientsByRecipeId(Integer recipeId) throws Exception {
        recipeService.deleteIngredientsByRecipeId(recipeId);
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

    private List<Ingredient> handleSetIngredients(Map<String, Object> payload, Recipe recipe) throws Exception {
        List<Map<String, Object>> ingredientsPayload = (List<Map<String, Object>>) payload.get("ingredients");
        List<Ingredient> ingredients = new ArrayList<>();
        for (Map<String, Object> ingredientData : ingredientsPayload) {
            IngredientDTO ingredientDTO = new IngredientDTO(ingredientData.get("foodText").toString(),
                                                            ingredientData.get("protein"),
                                                            ingredientData.get("calories"),
                                                            ingredientData.get("carbohydrate"),
                                                            ingredientData.get("sugar"),
                                                            ingredientData.get("sodium"),
                                                            ingredientData.get("fat"),
                                                            ingredientData.get("saturatedFat"));
            Ingredient ingredient = IngredientMapper.toIngredient(ingredientDTO);
            ingredient.getRecipes().add(recipe);
            ingredientService.saveIngredient(ingredient);
            ingredients.add(ingredient);
        }
        return ingredients;
    }

    private void setRecipeNutrients(Recipe recipe) {
        int servings = recipe.getServings();
        Double calories = recipe.getCalories();
        Double protein = recipe.getProtein();
        Double carbohydrate = recipe.getCarbohydrate();
        Double sugar = recipe.getSugar();
        Double sodium = recipe.getSodium();
        Double fat = recipe.getFat();
        Double saturatedFat = recipe.getSaturatedFat();
        recipe.setCalories(Math.round((calories / servings) * 10.0) / 10.0);
        // Calculate PDV of each macronutrient by using their reference intake
        double proteinPDV = (protein / servings) / 50 * 100;
        proteinPDV = Math.round(proteinPDV * 10.0) / 10.0;
        double carbohydratePDV = (carbohydrate / servings) / 260 * 100;
        carbohydratePDV = Math.round(carbohydratePDV * 10.0) / 10.0;
        double sugarPDV = (sugar / servings) / 90 * 100;
        sugarPDV = Math.round(sugarPDV * 10.0) / 10.0;
        double sodiumPDV = (sodium / 1000 / servings) / 6 * 100;
        sodiumPDV = Math.round(sodiumPDV * 10.0) / 10.0;
        double fatPDV = (fat / servings) / 70 * 100;
        fatPDV = Math.round(fatPDV * 10.0) / 10.0;
        double saturatedFatPDV = (saturatedFat / servings) / 20 * 100;
        saturatedFatPDV = Math.round(saturatedFatPDV * 10.0) / 10.0;
        recipe.setProtein(proteinPDV);
        recipe.setCarbohydrate(carbohydratePDV);
        recipe.setSugar(sugarPDV);
        recipe.setSodium(sodiumPDV);
        recipe.setFat(fatPDV);
        recipe.setSaturatedFat(saturatedFatPDV);
    }
}
