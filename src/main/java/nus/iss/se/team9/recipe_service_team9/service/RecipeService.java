package nus.iss.se.team9.recipe_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.recipe_service_team9.model.Ingredient;
import nus.iss.se.team9.recipe_service_team9.model.Recipe;
import nus.iss.se.team9.recipe_service_team9.model.Status;
import nus.iss.se.team9.recipe_service_team9.repo.IngredientRepository;
import nus.iss.se.team9.recipe_service_team9.repo.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserService userService;
    private final IngredientRepository ingredientRepository;
    
    @Autowired
    public RecipeService(RecipeRepository recipeRepository, UserService userService,
                         IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.userService = userService;
        this.ingredientRepository = ingredientRepository;
    }

    public void save(Recipe recipe){
        recipeRepository.save(recipe);
    }

    public void delete(Recipe recipe){
        recipe.setStatus(Status.DELETED);
        recipeRepository.save(recipe);
    }

    public void updateRecipeRating(Integer recipeId, double rating) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with ID: " + recipeId));
        recipe.setRating(rating);
        recipe.setNumberOfRating(recipe.getNumberOfRating() + 1);
        recipeRepository.save(recipe);
    }

    // get specific recipe by id
    public Recipe getRecipeById(Integer id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);
        return recipe.orElse(null);
    }

    public List<Recipe> getPublicRecipesByMemberId(Integer memberId){
        return recipeRepository.getRecipesByMemberIdAndStatus(memberId,Status.PUBLIC);
    }

    public List<Recipe> getRecipesByMemberId(Integer memberId){
        return recipeRepository.getRecipesByMemberId(memberId);
    }

    public List<String> findMatchingTags(String keyword) {
        Set<String> allUniqueTags = getAllUniqueTags();
        return allUniqueTags.stream()
                .filter(tag -> tag.toLowerCase().contains(keyword.toLowerCase())).collect(Collectors.toList());
    }

    // get all unique tags
    public Set<String> getAllUniqueTags() {
        List<String> tagLists = recipeRepository.findAllDistinctTags();
        Set<String> uniqueTags = new HashSet<>();
        for (String tags : tagLists) {
            uniqueTags.addAll(Arrays.asList(tags.split(",")));
        }
        return uniqueTags;
    }

    public void updateNumberOfSaved(Integer recipeId, String operation) {
        Recipe recipe = this.getRecipeById(recipeId);

        if (operation.equals("save")) {
            recipe.setNumberOfSaved(recipe.getNumberOfSaved() + 1);
        } else if (operation.equals("remove")) {
            recipe.setNumberOfSaved(recipe.getNumberOfSaved() - 1);
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
        recipeRepository.save(recipe);
    }

    public Page<Recipe> searchByTag(String tag, int pageNo, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize);
        return recipeRepository.findByTagsContainingByPage(tag, pageRequest);
    }

    public List<Recipe> searchByTag(String tag) {
        return recipeRepository.findByTagsContaining(tag);
    }

    public List<Recipe> searchByName(String query) {
        return recipeRepository.findByNameContaining(query);
    }

    public List<Recipe> searchByDescription(String query) {
        return recipeRepository.findByDescriptionContaining(query);
    }

    public List<Recipe> searchAll(String query) {
        List<Recipe> results1 = recipeRepository.findByNameContaining(query);
        List<Recipe> results2 = recipeRepository.findByTagsContaining(query);
        List<Recipe> results3 = recipeRepository.findByDescriptionContaining(query);
        return mergeLists(results1, results2, results3);
    }

    // merge
    public List<Recipe> mergeLists(List<Recipe> listByName, List<Recipe> listByTag, List<Recipe> listByDescription) {
        return Stream.of(listByName, listByTag, listByDescription).flatMap(List::stream).distinct()
                .collect(Collectors.toList());
    }

    public List<Recipe> getAllRecipesByYear(int year) {
        return recipeRepository.getAllRecipesByYear(year);
    }

    public List<Object[]> getRecipeCountByTag() {
        return recipeRepository.getRecipeCountByTag();
    }

    public List<Recipe> getRecipesByOrder(String orderBy, String order) {
        List<Recipe> recipes = new ArrayList<>();
        switch (orderBy) {
            case "rating" -> {
                if (order.equals("asc")) {
                    recipes = recipeRepository.findAllByOrderByRatingAsc();
                } else if (order.equals("desc")) {
                    recipes = recipeRepository.findAllByOrderByRatingDesc();
                }
            }
            case "numberOfSaved" -> {
                if (order.equals("asc")) {
                    recipes = recipeRepository.findAllByOrderByNumberOfSavedAsc();
                } else if (order.equals("desc")) {
                    recipes = recipeRepository.findAllByOrderByNumberOfSavedDesc();
                }
            }
            case "healthScore" -> {
                if (order.equals("asc")) {
                    recipes = recipeRepository.findAllByOrderByHealthScoreAsc();
                } else if (order.equals("desc")) {
                    recipes = recipeRepository.findAllByOrderByHealthScoreDesc();
                }
            }
            default -> recipes = recipeRepository.findAll();
        }
        return recipes;
    }

    public void deleteIngredientsByRecipeId(Integer recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found with ID: " + recipeId));
        List<Ingredient> ingredients = recipe.getIngredients();
        if (!ingredients.isEmpty()) {
			ingredientRepository.deleteAll(ingredients);
        }
        recipe.setIngredients(new ArrayList<>());
        recipeRepository.save(recipe);
    }
    
    
}
