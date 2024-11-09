package nus.iss.se.team9.recipe_service_team9.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
public class Ingredient {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column
	private String foodText;
	@Column
	private Double protein;
	@Column
	private Double calories;
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
	@ManyToMany(mappedBy = "ingredients")
	//	@JsonIgnore
	@JsonBackReference(value = "ingredient_recipes")
	private List<Recipe> recipes;
	public Ingredient() {
		recipes = new ArrayList<>();
	}
	public Ingredient(String foodText, double protein, double calories, double carbohydrate, double sugar, double sodium, double fat, double saturatedFat) {
		this.foodText = foodText;
		this.protein = protein;
		this.calories = calories;
		this.carbohydrate = carbohydrate;
		this.sugar = sugar;
		this.sodium = sodium;
		this.fat = fat;
		this.saturatedFat = saturatedFat;
		recipes = new ArrayList<>();
	}
	@Override
	public String toString() {
		return foodText + " (" + id + ") " + protein + ", " + calories + ", " + carbohydrate + ", " + sugar + ", " + sodium + ", " + fat + ", " + saturatedFat;
	}
}
