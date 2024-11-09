package nus.iss.se.team9.recipe_service_team9.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Entity
public class Recipe {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column
	@NotBlank(message = "Name is required")
	private String name;
	@Column(length = 800)
	private String description;
	@Column
	private Double rating;
	@Column
	private Integer numberOfSaved;
	@Column
	private Integer numberOfRating;
	@Column
	@NotNull(message = "Preparation Time is required")
	private Integer preparationTime;
	@Column
	@NotNull(message = "Servings is required")
	private Integer servings;
	@Column
	private Integer numberOfSteps;
	@Column
	private Integer healthScore;
	@Column
	private String notes;
	@Column
	private String image;
	@Enumerated(EnumType.STRING)
	private Status status;
	@Column
	private Double calories;
	@Column
	private Double protein;
	@Column
	private Double carbohydrate;
	@Column
	private Double sugar;
	@Column
	private Double sodium;
	@Column
	private Double fat;
	@Column
	private Double saturatedFat;
	@Column
	private LocalDate submittedDate;
	@ElementCollection
	@NotEmpty(message = "At least 1 step is required")
	@Column(length = 500)
	private List<String> steps;
	@ElementCollection
	private List<String> tags;
	
	@ManyToMany
	@JoinTable(name = "ingredient_recipes", joinColumns = @JoinColumn(name = "recipes_id"), inverseJoinColumns = @JoinColumn(name = "ingredients_id"))
	@JsonManagedReference(value = "recipe-ingredients")
	private List<Ingredient> ingredients;
	
	@OneToMany(mappedBy = "recipe")
	@JsonManagedReference(value = "recipe-reviews")
	private List<Review> reviews;
	
	@OneToMany(mappedBy = "recipeReported")
	@JsonManagedReference(value = "recipe-reportsToRecipe")
	private List<RecipeReport> reportsToRecipe;
	
	@ManyToOne
	@JoinColumn(name = "member_id")
	@JsonBackReference(value = "member-addedRecipes")
	private Member member;
	
	@ManyToMany
	@JoinTable(name = "recipe_members_who_save", joinColumns = @JoinColumn(name = "saved_recipes_id"), inverseJoinColumns = @JoinColumn(name = "members_who_save_id"))
	@JsonIgnore
	private List<Member> membersWhoSave;
	
	public Recipe() {
		ingredients = new ArrayList<>();
		tags = new ArrayList<>();
		reviews = new ArrayList<>();
		reportsToRecipe = new ArrayList<>();
		numberOfSaved = 0;
		numberOfRating = 0;
		rating = 0.0;
		submittedDate = LocalDate.now();
	}
	
	public Recipe(String name, String description, Member member) {
		this();
		this.name = name;
		this.description = description;
		this.member = member;
		status = Status.PUBLIC;
		steps = new ArrayList<>();
	}
	
	public Recipe(String name, String description, String notes, String image, double rating, int preparationTime,
				  int servings, int numberOfSteps, Member member, double calories, double protein, double carbohydrate,
				  double sugar, double sodium, double fat, double saturatedFat, List<String> steps, List<String> tags) {
		this(name, description, member);
		this.notes = notes;
		this.image = image;
		this.rating = rating;
		this.numberOfSaved = 0;
		this.preparationTime = preparationTime;
		this.servings = servings;
		this.numberOfSteps = numberOfSteps;
		this.calories = calories;
		this.protein = protein;
		this.carbohydrate = carbohydrate;
		this.sugar = sugar;
		this.sodium = sodium;
		this.fat = fat;
		this.saturatedFat = saturatedFat;
		this.steps = steps;
		this.tags = tags;
		
		this.healthScore = calculateHealthScore();
		this.numberOfRating = 0;
	}
	
	// Getter and Setter methods
	
	public int calculateHealthScore() {
		healthScore = 0;
		if (protein >= 10 && protein <= 15) {
			healthScore++;
		}
		if (carbohydrate >= 55 && carbohydrate <= 75) {
			healthScore++;
		}
		if (sugar < 10) {
			healthScore++;
		}
		if (sodium < 33) {
			healthScore++;
		}
		if (fat >= 15 && fat <= 30) {
			healthScore++;
		}
		if (saturatedFat < 10) {
			healthScore++;
		}
		return healthScore;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Recipe recipe = (Recipe) o;
		return Objects.equals(id, recipe.id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
}