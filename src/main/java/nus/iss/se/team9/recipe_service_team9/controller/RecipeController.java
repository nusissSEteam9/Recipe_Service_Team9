package nus.iss.se.team9.recipe_service_team9.controller;

import jakarta.servlet.http.HttpServletRequest;
import nus.iss.se.team9.recipe_service_team9.exception.UnauthorizedException;
import nus.iss.se.team9.recipe_service_team9.factory.FilterStrategyFactory;
import nus.iss.se.team9.recipe_service_team9.factory.SearchStrategyFactory;
import nus.iss.se.team9.recipe_service_team9.filterStrategy.FilterStrategy;
import nus.iss.se.team9.recipe_service_team9.mapper.IngredientMapper;
import nus.iss.se.team9.recipe_service_team9.mapper.RecipeMapper;
import nus.iss.se.team9.recipe_service_team9.model.*;
import nus.iss.se.team9.recipe_service_team9.searchStrategy.SearchStrategy;
import nus.iss.se.team9.recipe_service_team9.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/health")
    public String checkHealth() {
        return "API is connected";
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable("id") Integer id) {
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe == null || recipe.getStatus() == Status.DELETED) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        RecipeDTO recipeDTO = RecipeMapper.toRecipeDTO(recipe);
        return ResponseEntity.ok(recipeDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRecipeById(@PathVariable Integer id) {
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe != null) {
            recipeService.delete(recipe);
            return ResponseEntity.ok("deleted");
        } else {
            return ResponseEntity.notFound()
                                 .build();
        }
    }

    @GetMapping("/count-by-year/{year}")
    public ResponseEntity<List<RecipeDTO>> getAllRecipesByYear(@PathVariable int year) {
        List<Recipe> recipes = recipeService.getAllRecipesByYear(year);
        List<RecipeDTO> recipeDTOs = recipes.stream()
                                            .map(RecipeMapper::toRecipeDTO)
                                            .toList();
        System.out.println("order by year");
        return ResponseEntity.ok(recipeDTOs);
    }

    @GetMapping("/count-by-tag")
    public ResponseEntity<List<Object[]>> getRecipeCountByTag() {
        List<Object[]> recipeCounts = recipeService.getRecipeCountByTag();
        System.out.println("order by tag");
        return ResponseEntity.ok(recipeCounts);
    }

    @GetMapping
    public ResponseEntity<List<Recipe>> getRecipesByOrder(@RequestParam String orderBy, @RequestParam String order) {
        List<Recipe> recipes = recipeService.getRecipesByOrder(orderBy, order);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/getPublicRecipesByMemberId/{id}")
    public ResponseEntity<List<RecipeDTO>> getRecipesByMember(@PathVariable("id") Integer id) {
        List<Recipe> recipes = recipeService.getPublicRecipesByMemberId(id);
        List<RecipeDTO> recipeDTOs = recipes.stream()
                                            .map(RecipeMapper::toRecipeDTO)
                                            .toList();
        return ResponseEntity.ok(recipeDTOs);
    }

    @GetMapping("/getRecipeOwnerMemberId/{recipeId}")
    public ResponseEntity<Integer> getRecipeOwnerMemberId(@PathVariable Integer recipeId) {
        try {
            Recipe recipe = recipeService.getRecipeById(recipeId);
            if (recipe != null && recipe.getMember() != null) {
                Integer memberId = recipe.getMember()
                                         .getId();  // 获取 Member 的 ID
                return ResponseEntity.ok(memberId);  // 返回 memberId
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(null);
        }
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
        return ResponseEntity.noContent()
                             .build();
    }

    @PutMapping("/setRating/{id}")
    public ResponseEntity<String> updateRecipeRating(@PathVariable Integer id,
                                                     @RequestBody Map<String, Object> request) {
        try {
            if (!request.containsKey("rating")) {
                return ResponseEntity.badRequest()
                                     .body("Rating value is missing");
            }
            double rating = (double) request.get("rating");
            recipeService.updateRecipeRating(id, rating);
            return ResponseEntity.ok("Recipe rating updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to update recipe rating: " + e.getMessage());
        }
    }

    @PutMapping("/setNumberOfSaved/{id}")
    public ResponseEntity<String> updateRecipeNumberOfSaved(@PathVariable Integer id, @RequestBody String operation) {
        try {
            recipeService.updateNumberOfSaved(id, operation);//save or remove
            return ResponseEntity.ok("Operation successful");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                                 .body("Invalid operation: " + operation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("An unexpected error occurred");
        }
    }

    @GetMapping("/getTags")
    public ResponseEntity<List<String>> getTags(@RequestParam("keyword") String keyword) {
        List<String> matchingTags = recipeService.findMatchingTags(keyword);
        return ResponseEntity.ok(matchingTags);
    }

    @GetMapping("/getAllUniqueTags")
    public ResponseEntity<Set<String>> getAllUniqueTags() {
        return ResponseEntity.ok(recipeService.getAllUniqueTags());
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Map<String, Object>> viewRecipe(@PathVariable("id") Integer id,
                                                          @RequestHeader("Authorization") String token) {
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe.getStatus() == Status.DELETED) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        RecipeDTO recipeDTO = RecipeMapper.toRecipeDTO(recipe);
        String createdBy = recipe.getMember()
                                 .getUsername();
        Integer createdByUserId = recipe.getMember()
                                        .getId();
        Map<String, Object> response = new HashMap<>();
        response.put("recipe", recipeDTO);
        response.put("isSaved", userService.checkIfRecipeSaved(id, token));
        response.put("createdBy", createdBy);
        response.put("createdByUserId", createdByUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/{tag}")
    public ResponseEntity<Map<String, Object>> searchByTag(@PathVariable String tag,
                                                           @RequestParam(defaultValue = "0") int pageNo,
                                                           @RequestParam(defaultValue = "8") int pageSize,
                                                           HttpServletRequest request) {
        Page<Recipe> recipePage = recipeService.searchByTag(tag, pageNo, pageSize);
        List<RecipeDTO> recipeDTOs = recipePage.getContent()
                                               .stream()
                                               .map(RecipeMapper::toRecipeDTO)
                                               .toList();
        Map<String, Object> response = new HashMap<>();
        response.put("results", recipeDTOs);
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
            @RequestParam(defaultValue = "0") int pageNo, @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "8") int pageSize) {

        SearchStrategy searchStrategy = SearchStrategyFactory.getSearchStrategy(type);
        List<Recipe> results = searchStrategy.search(query, recipeService);

        List<Recipe> filteredResults = results;
        if (filter1) {
            FilterStrategy filterStrategy = FilterStrategyFactory.getFilterStrategy("filter1");
            filteredResults = filterStrategy.filter(filteredResults, token, userService, jwtService);
        }
        if (filter2) {
            FilterStrategy filterStrategy = FilterStrategyFactory.getFilterStrategy("filter2");
            try {
                filteredResults = filterStrategy.filter(filteredResults, token, userService, jwtService);
            } catch (UnauthorizedException e) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        int totalRecipes = filteredResults.size();
        int startIndex = pageNo * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalRecipes);
        int totalPages = (totalRecipes + pageSize - 1) / pageSize;

        List<RecipeDTO> recipeDTOList = filteredResults.subList(startIndex, endIndex)
                .stream()
                .map(RecipeMapper::toRecipeDTO)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("currentPage", pageNo + 1);
        response.put("totalPages", totalPages);
        response.put("pageSize", pageSize);
        response.put("results", recipeDTOList);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createRecipe(@RequestBody Map<String, Object> payload,
                                               @RequestHeader("Authorization") String token) {
        try {
            Integer memberId = jwtService.extractId(token);
            if (memberId == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            Member member = userService.getMemberById(memberId);

            Map<String, Object> nutritionData = (Map<String, Object>) payload.get("nutrition");
            List<String> steps = (List<String>) payload.get("steps");
            List<String> tags = (List<String>) payload.get("tags");

            Recipe recipe = new Recipe((String) payload.get("name"), (String) payload.get("description"),
                                       (String) payload.get("notes"), (String) payload.get("image"), 0.0,
                                       (Integer) payload.get("preparationTime"), (Integer) payload.get("servings"),
                                       steps.size(), member, handleNutritionData(nutritionData.get("calories")),
                                       handleNutritionData(nutritionData.get("protein")),
                                       handleNutritionData(nutritionData.get("carbohydrate")),
                                       handleNutritionData(nutritionData.get("sugar")),
                                       handleNutritionData(nutritionData.get("sodium")),
                                       handleNutritionData(nutritionData.get("fat")),
                                       handleNutritionData(nutritionData.get("saturated_fat")), steps, tags);

            List<Map<String, Object>> ingredientsPayload = (List<Map<String, Object>>) payload.get("ingredients");
            List<Ingredient> ingredients = new ArrayList<>();
            for (Map<String, Object> ingredientData : ingredientsPayload) {
                // Create and save each Ingredient
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
            setRecipeNutrients(recipe);
            recipe.setHealthScore(recipe.calculateHealthScore());

            recipeService.save(recipe);

            return ResponseEntity.ok("Recipe created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error creating recipe: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateRecipe(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        try {
            Recipe recipe = recipeService.getRecipeById(id);
            if (recipe == null) {
                return ResponseEntity.notFound()
                                     .build();
            }
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

            recipeService.deleteIngredientsByRecipeId(id);

            List<Map<String, Object>> ingredientsPayload = (List<Map<String, Object>>) payload.get("ingredients");
            List<Ingredient> ingredients = new ArrayList<>();
            for (Map<String, Object> ingredientData : ingredientsPayload) {
                // Create and save each Ingredient
                IngredientDTO ingredientDTO = new IngredientDTO(ingredientData.get("foodText")
                                                                              .toString(),
                                                                ingredientData.get("protein"),
                                                                ingredientData.get("calories"),
                                                                ingredientData.get("carbohydrate"),
                                                                ingredientData.get("sugar"),
                                                                ingredientData.get("sodium"),
                                                                ingredientData.get("fat"),
                                                                ingredientData.get("saturatedFat"));
                Ingredient ingredient = IngredientMapper.toIngredient(ingredientDTO);
                ingredient.getRecipes()
                          .add(recipe);
                ingredientService.saveIngredient(ingredient);
                ingredients.add(ingredient);
            }
            recipe.setIngredients(ingredients);
            setRecipeNutrients(recipe);
            recipe.setHealthScore(recipe.calculateHealthScore());

            recipeService.save(recipe);
            return ResponseEntity.ok("Recipe updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error updating recipe: " + e.getMessage());
        }
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

    private void handleImageUpload(MultipartFile pictureFile, Recipe recipe) {
        if (pictureFile != null && !pictureFile.isEmpty()) {
            String uploadDirectory = "src/main/resources/static/images";
            String uniqueFileName = UUID.randomUUID()
                                        .toString() + "_" + pictureFile.getOriginalFilename();
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

    public void setRecipeNutrients(Recipe recipe) {
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

    @Autowired
    private S3Service s3Service;

    @GetMapping("/presigned-url/healthy-recipe-images/{keyName}")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@PathVariable String keyName, @RequestHeader("Authorization") String token) {
        if (jwtService.extractId(token) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "Unauthorized"));
        }
        Map<String, String> response = new HashMap<>();

        try {
            String presignedUrl = s3Service.createPresignedGetUrl(keyName);
            response.put("url", presignedUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Error generating presigned URL: " + e.getMessage()));
        }
    }

    @GetMapping("/presigned-url/listAllBuckets")
    public ResponseEntity<List<String>> getAllBuckets() {
        return ResponseEntity.ok(s3Service.ListAllBuckets());
    }

    @GetMapping("/presigned-url/listAllObjects")
    public ResponseEntity<List<String>> getAllObjects() {
        return ResponseEntity.ok(s3Service.ListAllObjects());
    }

    @DeleteMapping("/presigned-url/deleteObject/{keyName}")
    public ResponseEntity<String> deleteObject(@PathVariable String keyName, @RequestHeader("Authorization") String token) {
        if (jwtService.extractId(token) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Unauthorized");
        }
        try {
            s3Service.deleteObject(keyName);
            return ResponseEntity.ok("Object deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error deleting object: " + e.getMessage());
        }
    }

    @PostMapping("/uploadImage")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
        if (jwtService.extractId(token) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "Unauthorized"));
        }
        try {
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
            String fileUrl = s3Service.getObjectUrl(keyName);
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "Error uploading image: " + e.getMessage()));
        }
    }
}