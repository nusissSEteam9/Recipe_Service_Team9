package nus.iss.se.team9.recipe_service_team9.mapper;

import nus.iss.se.team9.recipe_service_team9.model.*;

import java.util.stream.Collectors;

public class RecipeMapper {
    public static RecipeDTO toRecipeDTO(Recipe recipe) {
        if (recipe == null) {
            return null;
        }

        RecipeDTO recipeDTO = new RecipeDTO();
        recipeDTO.setId(recipe.getId());
        recipeDTO.setName(recipe.getName());
        recipeDTO.setDescription(recipe.getDescription());
        recipeDTO.setRating(recipe.getRating());
        recipeDTO.setNumberOfSaved(recipe.getNumberOfSaved());
        recipeDTO.setNumberOfRating(recipe.getNumberOfRating());
        recipeDTO.setPreparationTime(recipe.getPreparationTime());
        recipeDTO.setServings(recipe.getServings());
        recipeDTO.setNumberOfSteps(recipe.getNumberOfSteps());
        recipeDTO.setHealthScore(recipe.getHealthScore());
        recipeDTO.setNotes(recipe.getNotes());
        recipeDTO.setImage(recipe.getImage());
        recipeDTO.setStatus(recipe.getStatus().toString());
        recipeDTO.setCalories(recipe.getCalories());
        recipeDTO.setProtein(recipe.getProtein());
        recipeDTO.setCarbohydrate(recipe.getCarbohydrate());
        recipeDTO.setSugar(recipe.getSugar());
        recipeDTO.setSodium(recipe.getSodium());
        recipeDTO.setFat(recipe.getFat());
        recipeDTO.setSaturatedFat(recipe.getSaturatedFat());
        recipeDTO.setSubmittedDate(recipe.getSubmittedDate());
        recipeDTO.setSteps(recipe.getSteps());
        recipeDTO.setTags(recipe.getTags());

        // Map ingredients
        recipeDTO.setIngredients(recipe.getIngredients().stream()
                .map(RecipeMapper::toIngredientDTO)
                .collect(Collectors.toList()));

        // Map reviews
        recipeDTO.setReviews(recipe.getReviews().stream()
                .map(RecipeMapper::toReviewDTO)
                .collect(Collectors.toList()));

        // Map member (basic info only)
        recipeDTO.setMember(toMemberDTO(recipe.getMember()));

        return recipeDTO;
    }

    public static IngredientDTO toIngredientDTO(Ingredient ingredient) {
        if (ingredient == null) {
            return null;
        }

        IngredientDTO dto = new IngredientDTO();
        dto.setId(ingredient.getId());
        dto.setFoodText(ingredient.getFoodText());
        dto.setProtein(ingredient.getProtein());
        dto.setCalories(ingredient.getCalories());
        dto.setCarbohydrate(ingredient.getCarbohydrate());
        dto.setSugar(ingredient.getSugar());
        dto.setSodium(ingredient.getSodium());
        dto.setFat(ingredient.getFat());
        dto.setSaturatedFat(ingredient.getSaturatedFat());

        return dto;
    }

    public static ReviewDTO toReviewDTO(Review review) {
        if (review == null) {
            return null;
        }

        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewDate(review.getReviewDate());
        dto.setReviewerUsername(review.getMember().getUsername()); // Get member's username
        dto.setRecipeId(review.getRecipe().getId());           // Get recipe ID
        dto.setMemberId(review.getMember().getId());           // Get member ID

        return dto;
    }

    public static MemberDTO toMemberDTO(Member member) {
        if (member == null) {
            return null;
        }

        MemberDTO dto = new MemberDTO();
        dto.setId(member.getId());
        dto.setUsername(member.getUsername());
        dto.setEmail(member.getEmail());
        dto.setHeight(member.getHeight());
        dto.setWeight(member.getWeight());
        dto.setAge(member.getAge());
        dto.setBirthdate(member.getBirthdate());
        dto.setGender(member.getGender());
        dto.setCalorieIntake(member.getCalorieIntake());
        dto.setRegistrationDate(member.getRegistrationDate());
        dto.setMemberStatus(member.getMemberStatus().toString());
        dto.setPreferenceList(member.getPreferenceList());

        return dto;
    }

    public static Recipe toRecipe(RecipeDTO recipeDTO) {
        if (recipeDTO == null) {
            return null;
        }
        Recipe recipe = new Recipe();
        recipe.setName(recipeDTO.getName());
        recipe.setDescription(recipeDTO.getDescription());
        recipe.setRating(recipeDTO.getRating());
        recipe.setNumberOfSaved(recipeDTO.getNumberOfSaved());
        recipe.setNumberOfRating(recipeDTO.getNumberOfRating());
        recipe.setPreparationTime(recipeDTO.getPreparationTime());
        recipe.setServings(recipeDTO.getServings());
        recipe.setNumberOfSteps(recipeDTO.getNumberOfSteps());
        recipe.setHealthScore(recipeDTO.getHealthScore());
        recipe.setNotes(recipeDTO.getNotes());
        recipe.setImage(recipeDTO.getImage());
        recipe.setStatus(Status.valueOf(recipeDTO.getStatus()));
        recipe.setCalories(recipeDTO.getCalories());
        recipe.setProtein(recipeDTO.getProtein());
        recipe.setCarbohydrate(recipeDTO.getCarbohydrate());
        recipe.setSugar(recipeDTO.getSugar());
        recipe.setSodium(recipeDTO.getSodium());
        recipe.setFat(recipeDTO.getFat());
        recipe.setSaturatedFat(recipeDTO.getSaturatedFat());
        recipe.setSubmittedDate(recipeDTO.getSubmittedDate());
        recipe.setSteps(recipeDTO.getSteps());
        recipe.setTags(recipeDTO.getTags());
        return recipe;
    }
}
