package nus.iss.se.team9.recipe_service_team9;

import nus.iss.se.team9.recipe_service_team9.controller.RecipeController;
import nus.iss.se.team9.recipe_service_team9.model.*;
import nus.iss.se.team9.recipe_service_team9.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RecipeControllerTest {

    @InjectMocks
    private RecipeController recipeController;

    @Mock
    private RecipeService recipeService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private IngredientService ingredientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCheckHealth() {
        String result = recipeController.checkHealth();
        assertEquals("API is connected", result);
    }

    @Test
    void testGetRecipeById_Found() {
        Recipe recipe = new Recipe();
        recipe.setStatus(Status.CREATED);
        when(recipeService.getRecipeById(1)).thenReturn(recipe);

        ResponseEntity<RecipeDTO> response = recipeController.getRecipeById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(recipeService, times(1)).getRecipeById(1);
    }

    @Test
    void testGetRecipeById_NotFound() {
        when(recipeService.getRecipeById(1)).thenReturn(null);

        ResponseEntity<RecipeDTO> response = recipeController.getRecipeById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(recipeService, times(1)).getRecipeById(1);
    }

    @Test
    void testDeleteRecipeById_ExistingRecipe() {
        Recipe recipe = new Recipe();
        when(recipeService.getRecipeById(1)).thenReturn(recipe);

        ResponseEntity<String> response = recipeController.deleteRecipeById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("deleted", response.getBody());
        verify(recipeService, times(1)).delete(recipe);
    }

    @Test
    void testDeleteRecipeById_NonExistingRecipe() {
        when(recipeService.getRecipeById(1)).thenReturn(null);

        ResponseEntity<String> response = recipeController.deleteRecipeById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(recipeService, never()).delete(any(Recipe.class));
    }

    @Test
    void testGetRecipeOwnerMemberId_Found() {
        Recipe recipe = new Recipe();
        Member member = new Member();
        member.setId(5);
        recipe.setMember(member);
        when(recipeService.getRecipeById(1)).thenReturn(recipe);

        ResponseEntity<Integer> response = recipeController.getRecipeOwnerMemberId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody());
    }

    @Test
    void testGetRecipeOwnerMemberId_NotFound() {
        when(recipeService.getRecipeById(1)).thenReturn(null);

        ResponseEntity<Integer> response = recipeController.getRecipeOwnerMemberId(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateRecipeRating_ValidRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("rating", 4.5);

        ResponseEntity<String> response = recipeController.updateRecipeRating(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Recipe rating updated successfully.", response.getBody());
        verify(recipeService, times(1)).updateRecipeRating(1, 4.5);
    }

    @Test
    void testUpdateRecipeRating_MissingRating() {
        Map<String, Object> request = new HashMap<>();

        ResponseEntity<String> response = recipeController.updateRecipeRating(1, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Rating value is missing", response.getBody());
        verify(recipeService, never()).updateRecipeRating(anyInt(), anyDouble());
    }




}
