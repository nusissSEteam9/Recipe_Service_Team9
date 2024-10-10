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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
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

    @GetMapping("/count-by-year/{year}")
    public ResponseEntity<List<Recipe>> getAllRecipesByYear(@PathVariable int year) {
        List<Recipe> recipes = recipeService.getAllRecipesByYear(year);
        System.out.println("order by year");
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/count-by-tag")
    public ResponseEntity<List<Object[]>> getRecipeCountByTag() {
        List<Object[]> recipeCounts = recipeService.getRecipeCountByTag();
        System.out.println("order by tag");
        return ResponseEntity.ok(recipeCounts);
    }

    @GetMapping
    public ResponseEntity<List<Recipe>> getRecipesByOrder(
            @RequestParam String orderBy, @RequestParam String order) {
        List<Recipe> recipes = recipeService.getRecipesByOrder(orderBy, order);
        return ResponseEntity.ok(recipes);
    }

    @DeleteMapping("/delete-by-member")
    public ResponseEntity<Void> deleteRecipesByMember(@RequestBody Integer memberId) {
        System.out.println("Start to delete recipes");
        List<Recipe> recipes = recipeService.getRecipesByMemberId(memberId);
        System.out.println(recipes);
        for (Recipe recipe : recipes) {
            recipe.setStatus(Status.DELETED);
            recipeService.save(recipe);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/set-recipe-to-deleted/{id}")
    public ResponseEntity<Void> deleteRecipeById(@PathVariable Integer id) {
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe != null) {
            recipeService.delete(recipe);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/getTags")
    public ResponseEntity<List<String>> getTags(@RequestParam("keyword") String keyword) {
        List<String> matchingTags = recipeService.findMatchingTags(keyword);
        return ResponseEntity.ok(matchingTags);
    }

    @PostMapping("/save/{id}")
    public ResponseEntity<String> saveRecipe(@PathVariable Integer id, @RequestHeader("Authorization") String token) {
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
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "searchtype", required = false, defaultValue = "") String type,
            @RequestParam(name = "filter1", defaultValue = "false") boolean filter1,
            @RequestParam(name = "filter2", defaultValue = "false") boolean filter2,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "8") int pageSize) {

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
    public ResponseEntity<String> createRecipe(@RequestBody Map<String, Object> payload){
        try{
            String name = (String) payload.get("name");
            String description = (String) payload.get("description");
            Integer servings = (Integer) payload.get("servings");
            Integer preparationTime = (Integer) payload.get("preparationTime");
            String notes = (String) payload.get("notes");
            String statusStr = (String) payload.get("status");
            String image = (String) payload.get("image");
            Status status = Status.valueOf(statusStr.toUpperCase());

            Recipe recipe = new Recipe();
            recipe.setName(name);
            recipe.setDescription(description);
            recipe.setServings(servings);
            recipe.setPreparationTime(preparationTime);
            recipe.setNotes(notes);
            recipe.setStatus(status);
            recipe.setImage(image);
            recipe.setMember(userService.getMemberById(1));//修改为JWT来获取用户信息

            List<String> steps = (List<String>) payload.get("steps");
            recipe.setSteps(steps);
            recipe.setNumberOfSteps(steps.size());

            List<String> tags = (List<String>) payload.get("tags");
            recipe.setTags(tags);

            recipeService.createRecipe(recipe);

            List<Map<String, Object>> ingredientsPayload = (List<Map<String, Object>>) payload.get("ingredients");
            List<Map<String, Object>> nutritionPayload = (List<Map<String, Object>>) payload.get("nutrition");
            List<Ingredient> ingredients = new ArrayList<>();
            List<Integer> ingredientIds = new ArrayList<>();
            for (int i = 0; i < ingredientsPayload.size(); i++) {
                Map<String, Object> ingredientData = ingredientsPayload.get(i);
                String ingredientName = (String) ingredientData.get("name");
                Integer quantity = Integer.valueOf(ingredientData.get("quantity").toString());
                String unit = (String) ingredientData.get("unit");

                Map<String, Object> nutritionData = nutritionPayload.get(i);
                double calories = handleNutritionData(nutritionData.get("calories"));
                double carbohydrate = handleNutritionData(nutritionData.get("carbohydrate"));
                double fat = handleNutritionData(nutritionData.get("fat"));
                double protein = handleNutritionData(nutritionData.get("protein"));
                double saturatedFat = handleNutritionData(nutritionData.get("saturatedFat"));
                double sodium = handleNutritionData(nutritionData.get("sodium"));
                double sugar = handleNutritionData(nutritionData.get("sugar"));

                Ingredient ingredient = new Ingredient(ingredientName + " " + quantity.toString() + unit, protein, calories, carbohydrate, sugar, sodium, fat, saturatedFat);
                ingredientService.saveIngredient(ingredient);
                ingredientIds.add(ingredient.getId());
                ingredients.add(ingredient);
            }
            handleIngredientIds(ingredientIds, recipe);
            recipe.setIngredients(ingredients);
            setRecipeNutrients(recipe);
            recipe.setHealthScore(recipe.calculateHealthScore());
            recipeService.createRecipe(recipe);
            return ResponseEntity.ok("Recipe created successfully");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating recipe: " + e.getMessage());
        }
    }

    private double handleNutritionData(Object nutritionData) {
        if (nutritionData instanceof Integer) {
            return ((Integer) nutritionData).doubleValue();
        } else if (nutritionData instanceof Double) {
            return (Double) nutritionData;
        }else {
            return 0.0;
        }
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

    private void handleIngredientIds(List<Integer> ingredientIds, Recipe recipe) {
        for (Integer ingredientId : ingredientIds) {
            if (!ingredientId.equals(0)) {
                Ingredient ingredient = ingredientService.getIngredientById(ingredientId);
                ingredient.getRecipes().add(recipe);
                ingredientService.saveIngredient(ingredient);
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
    public ResponseEntity<Map<String, Object>> viewRecipe(@PathVariable("id") Integer id) {
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe.getStatus() == Status.DELETED) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String createdBy = recipe.getMember().getUsername();
        Integer createdByUserId = recipe.getMember().getId();
        Map<String, Object> response = new HashMap<>();
        response.put("recipe", recipe);
        response.put("createdBy", createdBy);
        response.put("createdByUserId", createdByUserId);
        List<Map<String, Object>> reviewList = new ArrayList<>();
        for (Review review : recipe.getReviews()) {
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("id", review.getId());
            reviewData.put("rating", review.getRating());
            reviewData.put("comment", review.getComment());
            reviewData.put("reviewDate", review.getReviewDate());

            // 添加评论人的 id 和 username
            if (review.getMember() != null) {
                reviewData.put("memberId", review.getMember().getId());
                reviewData.put("memberUsername", review.getMember().getUsername());
            } else {
                reviewData.put("memberId", null);
                reviewData.put("memberUsername", "Unknown");
            }
            reviewList.add(reviewData);
        }
        response.put("reviews", reviewList);
        return ResponseEntity.ok(response);
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