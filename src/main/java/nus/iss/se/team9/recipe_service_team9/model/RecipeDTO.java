package nus.iss.se.team9.recipe_service_team9.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RecipeDTO {
    private Integer id;
    private String name;
    private String description;
    private Double rating;
    private Integer numberOfSaved;
    private Integer numberOfRating;
    private Integer preparationTime;
    private Integer servings;
    private Integer numberOfSteps;
    private Integer healthScore;
    private String notes;
    private String image;
    private String status;
    private Double calories;
    private Double protein;
    private Double carbohydrate;
    private Double sugar;
    private Double sodium;
    private Double fat;
    private Double saturatedFat;
    private LocalDate submittedDate;
    private List<String> steps;
    private List<String> tags;
    private List<IngredientDTO> ingredients;  // Using DTO for Ingredient
    private List<ReviewDTO> reviews;          // Using DTO for Review
    private MemberDTO member;                 // Using DTO for Member
}
