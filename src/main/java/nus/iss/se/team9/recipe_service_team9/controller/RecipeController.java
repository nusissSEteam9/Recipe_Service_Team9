package nus.iss.se.team9.recipe_service_team9.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import nus.iss.se.team9.recipe_service_team9.model.*;
import nus.iss.se.team9.recipe_service_team9.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/recipe")
public class RecipeController {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RecipeService recipeService;
    @Autowired
    private UserService userService;
    @Autowired
    private IngredientService ingredientService;

    @GetMapping("/getTags")
    public ResponseEntity<List<String>> getTags(@RequestParam("keyword") String keyword) {
        List<String> matchingTags = recipeService.findMatchingTags(keyword);
        return ResponseEntity.ok(matchingTags);
    }

    @PostMapping("/save/{id}")
    public ResponseEntity<String> saveRecipe(@PathVariable Integer id,@RequestHeader("Authorization") String token) {
        Recipe recipe = recipeService.getRecipeById(id);
        Member member = userService.getMemberById(jwtService.extractId(token));
        recipeService.saveRecipe(recipe, member);
        return ResponseEntity.ok("Recipe saved successfully");
    }

    @PostMapping("/unsubscribe/{id}")
    public ResponseEntity<String> unsubscribeRecipe(@PathVariable Integer id, @RequestHeader("Authorization") String token) {
        Recipe recipe = recipeService.getRecipeById(id);
        Member member = userService.getMemberById(jwtService.extractId(token));
        recipeService.unsubscribeRecipe(recipe, member);
        return ResponseEntity.ok("Unsubscribed from recipe successfully");
    }

    @GetMapping("/review/{id}")
    public ResponseEntity<Review> reviewRecipe(@PathVariable Integer id) {
        Recipe recipe = recipeService.getRecipeById(id);
        Review review = new Review();
        review.setRecipe(recipe);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/search/{tag}")
    public ResponseEntity<Map<String, Object>> searchByTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "12") int pageSize,
            HttpServletRequest request) {
        Page<Recipe> recipePage = recipeService.searchByTag(tag, pageNo, pageSize);
        Map<String, Object> response = new HashMap<>();
        response.put("results", recipePage.getContent());
        response.put("currentPage", recipePage.getNumber());
        response.put("totalPages", recipePage.getTotalPages());
        response.put("pageSize", pageSize);
        response.put("request", request.getRequestURI());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRecipe(
            @RequestParam("query") String query,
            @RequestParam("searchtype") String type,
            @RequestParam(name = "filter1", defaultValue = "false") boolean filter1,
            @RequestParam(name = "filter2", defaultValue = "false") boolean filter2,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "12") int pageSize) {

        List<Recipe> results = switch (type) {
            case "tag" -> recipeService.searchByTag(query);
            case "name" -> recipeService.searchByName(query);
            case "description" -> recipeService.searchByDescription(query);
            default -> recipeService.searchAll(query);
        };

        // Filter results
    List<Recipe> filteredResults = results;
		if (filter1) {
        filteredResults = results.stream().filter(r -> r.getHealthScore() >= 4).collect(Collectors.toList());
    }
		if (filter2) {
        Integer memberId = jwtService.extractId(token);
        if (memberId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Member member = userService.getMemberById(memberId);
        Double calorieIntake = member.getCalorieIntake();
        if (calorieIntake == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        filteredResults = results.stream().filter(r -> r.getCalories() <= (calorieIntake / 3))
                .collect(Collectors.toList());
    }

    int totalRecipes = filteredResults.size();
    int startIndex = pageNo * pageSize;
    int endIndex = Math.min(startIndex + pageSize, totalRecipes);
    int totalPages = (totalRecipes + pageSize - 1) / pageSize;

    Map<String, Object> response = new HashMap<>();
		response.put("currentPage", pageNo);
		response.put("totalPages", totalPages);
		response.put("pageSize", pageSize);
		response.put("results", filteredResults.subList(startIndex, endIndex));
		return ResponseEntity.ok(response);
    }


    @PostMapping("/create")
    public ResponseEntity<String> addRecipe(@Valid @ModelAttribute("recipe") Recipe recipe,
                                            BindingResult bindingResult,
                                            @RequestParam("timeUnit") String timeUnit,
                                            @RequestParam("pictureInput") MultipartFile pictureFile,
                                            @RequestParam("ingredientIds") String ingredientIds,
                                            @RequestHeader("Authorization") String token
                                            ) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>("Binding error at recipe creation", HttpStatus.BAD_REQUEST);
        }

        // If preparation time entered in hours, convert to mins
        if (timeUnit.equals("hours")) {
            int preparationTime = recipe.getPreparationTime();
            recipe.setPreparationTime(preparationTime * 60);
        }

        // Set the image and ingredients
        handleImageUpload(pictureFile, recipe);
        handleIngredientIds(ingredientIds, recipe);

        Member member = userService.getMemberById(jwtService.extractId(token));
        recipe.setMember(member);

        setRecipeNutrients(recipe);
        recipe.setHealthScore(recipe.calculateHealthScore());
        recipeService.createRecipe(recipe);
        return ResponseEntity.ok("Recipe created successfully");
    }

    private void handleImageUpload(MultipartFile pictureFile, Recipe recipe) {
        if (pictureFile != null && !pictureFile.isEmpty()) {
            String uploadDirectory = "src/main/resources/static/images";
            String uniqueFileName = UUID.randomUUID().toString() + "_" + pictureFile.getOriginalFilename();
            Path uploadPath = Path.of(uploadDirectory);
            Path filePath = uploadPath.resolve(uniqueFileName);
            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(pictureFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            recipe.setImage(uniqueFileName);
        }
    }

    private void handleIngredientIds(String ingredientIds, Recipe recipe) {
        List<Ingredient> ingredients = recipe.getIngredients();
        String[] ingredientsToAdd = ingredientIds.split(",");
        for (String ingredientId : ingredientsToAdd) {
            if (!ingredientId.isEmpty()) {
                Ingredient ingredient = ingredientService.getIngredientById(Integer.parseInt(ingredientId));
                ingredient.getRecipes().add(recipe);
                ingredientService.saveIngredient(ingredient);
                ingredients.add(ingredient);
            }
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteRecipe(@PathVariable("id") Integer id) {
        Recipe recipe = recipeService.getRecipeById(id);
        List<Ingredient> ingredients = recipe.getIngredients();
        for (Ingredient ingredient : ingredients) {
            ingredient.getRecipes().remove(recipe);
            ingredientService.saveIngredient(ingredient);
        }
        recipe.setStatus(Status.DELETED);
        recipeService.updateRecipe(recipe);
        return ResponseEntity.ok("Recipe deleted successfully");
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Recipe> viewRecipe(@PathVariable("id") Integer id) {
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe.getStatus() == Status.DELETED) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(recipe);
    }

    public void setRecipeNutrients(Recipe recipe) {
        // Sum up nutrients from each recipe
        List<Ingredient> ingredients = recipe.getIngredients();
        int servings = recipe.getServings();
        Double calories = 0.0;
        Double protein = 0.0;
        Double carbohydrate = 0.0;
        Double sugar = 0.0;
        Double sodium = 0.0;
        Double fat = 0.0;
        Double saturatedFat = 0.0;
        for (Ingredient ingredient : ingredients) {
            calories += ingredient.getCalories();
            protein += ingredient.getProtein();
            carbohydrate += ingredient.getCarbohydrate();
            sugar += ingredient.getSugar();
            sodium += ingredient.getSodium();
            fat += ingredient.getFat();
            saturatedFat += ingredient.getSaturatedFat();
        }
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
