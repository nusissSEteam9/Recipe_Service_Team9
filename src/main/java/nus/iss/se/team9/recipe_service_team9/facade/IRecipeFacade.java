package nus.iss.se.team9.recipe_service_team9.facade;

import nus.iss.se.team9.recipe_service_team9.model.Member;
import nus.iss.se.team9.recipe_service_team9.model.Recipe;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


public interface IRecipeFacade {
    Recipe handleNewRecipe(Map<String, Object> payload, Member member, String imageUrl) throws Exception;

    void handleSaveRecipe(Recipe recipe) throws Exception;

    Member handleGetMemberById(Integer memberId) throws Exception;

    Integer handleExtractId(String token) throws Exception;

    void handleSetIngredients(List<Map<String, Object>> ingredientsPayload, Recipe recipe) throws Exception;

    String handleImageUpload(MultipartFile file) throws Exception;

    Recipe handleGetRecipeById(Integer recipeId) throws Exception;

    void handleUpdateRecipe(Map<String, Object> payload, Recipe recipe, String imageUrl) throws Exception;

    void handleDeleteIngredientsByRecipeId(Integer recipeId) throws Exception;
}
