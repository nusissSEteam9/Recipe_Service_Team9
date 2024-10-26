package nus.iss.se.team9.recipe_service_team9.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@Entity
public class Review {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column
	private Integer rating;
	@Column(length = 1200)
	private String comment;
	@Column
	private LocalDate reviewDate;
	@ManyToOne
//	@JsonBackReference(value = "member-reviews")
	@JsonIgnore
	private Member member;
	@ManyToOne
	@JsonBackReference(value = "recipe-reviews")
	private Recipe recipe;
	
	public Review() {
		setReviewDate(LocalDate.now());
	}
	
	public Review(int rating, String comment, Member member, Recipe recipe) {
		this.rating = rating;
		this.comment = comment;
		this.member = member;
		this.recipe = recipe;
	}
}
