package nus.iss.se.team9.recipe_service_team9.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
@Entity
public class RecipeReport extends Report{
	@ManyToOne
	@JsonBackReference(value = "recipe-reportsToRecipe")
	private Recipe recipeReported;

	// getter and setter
	public Recipe getRecipeReported() {
		return recipeReported;
	}

	public void setRecipeReported(Recipe recipeReported) {
		this.recipeReported = recipeReported;
	}
	
}
