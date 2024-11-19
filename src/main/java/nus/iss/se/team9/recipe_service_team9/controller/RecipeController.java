package nus.iss.se.team9.recipe_service_team9.controller;

import jakarta.servlet.http.HttpServletRequest;
import nus.iss.se.team9.recipe_service_team9.exception.UnauthorizedException;
import nus.iss.se.team9.recipe_service_team9.facade.RecipeFacade;
import nus.iss.se.team9.recipe_service_team9.factory.FilterStrategyFactory;
import nus.iss.se.team9.recipe_service_team9.factory.SearchStrategyFactory;
import nus.iss.se.team9.recipe_service_team9.filterStrategy.FilterStrategy;
import nus.iss.se.team9.recipe_service_team9.mapper.RecipeMapper;
import nus.iss.se.team9.recipe_service_team9.model.Member;
import nus.iss.se.team9.recipe_service_team9.model.Recipe;
import nus.iss.se.team9.recipe_service_team9.model.RecipeDTO;
import nus.iss.se.team9.recipe_service_team9.model.Status;
import nus.iss.se.team9.recipe_service_team9.searchStrategy.SearchStrategy;
import nus.iss.se.team9.recipe_service_team9.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                                               @RequestHeader("Authorization") String token,
                                               @RequestParam("file") MultipartFile file) {
        try {
            RecipeFacade recipeFacade = new RecipeFacade();
            Integer memberId = recipeFacade.handleExtractId(token);
            Member member = recipeFacade.handleGetMemberById(memberId);
            String imageUrl = recipeFacade.handleImageUpload(file);
            Recipe recipe = recipeFacade.handleNewRecipe(payload, member, imageUrl);
            List<Map<String, Object>> ingredientsPayload = (List<Map<String, Object>>) payload.get("ingredients");
            recipeFacade.handleSetIngredients(ingredientsPayload, recipe);
            recipeFacade.handleSaveRecipe(recipe);
            return ResponseEntity.ok("Recipe created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error creating recipe: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateRecipe(@PathVariable Integer id, @RequestBody Map<String, Object> payload,
                                               @RequestHeader("Authorization") String token,
                                               @RequestParam("file") MultipartFile file) {
        try {

            RecipeFacade recipeFacade = new RecipeFacade();
            Integer memberId = recipeFacade.handleExtractId(token);
            String imageUrl = recipeFacade.handleImageUpload(file);
            Recipe recipe = recipeFacade.handleGetRecipeById(id);
            recipeFacade.handleUpdateRecipe(payload, recipe, imageUrl);
            recipeFacade.handleDeleteIngredientsByRecipeId(id);
            List<Map<String, Object>> ingredientsPayload = (List<Map<String, Object>>) payload.get("ingredients");
            recipeFacade.handleSetIngredients(ingredientsPayload, recipe);
            recipeFacade.handleSaveRecipe(recipe);
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

    @Autowired
    private S3Service s3Service;

    @GetMapping("/presigned-url/healthy-recipe-images/{keyName}")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@PathVariable String keyName,
                                                               @RequestHeader("Authorization") String token) {
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
    public ResponseEntity<String> deleteObject(@PathVariable String keyName,
                                               @RequestHeader("Authorization") String token) {
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
}