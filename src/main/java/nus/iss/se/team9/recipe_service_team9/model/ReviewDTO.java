package nus.iss.se.team9.recipe_service_team9.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReviewDTO {
    private Integer id;
    private Integer rating;
    private String comment;
    private LocalDate reviewDate;
    private String reviewerUsername;  // Basic information from Member
    private Integer recipeId;     // Basic information from Recipe
    private Integer memberId;

    @Override
    public String toString() {
        return "ReviewDTO{" + "id=" + id + ", rating=" + rating + ", comment='" + comment + '\'' + ", reviewDate=" + reviewDate + ", reviewerUsername='" + reviewerUsername + '\'' + ", recipeId=" + recipeId + ", memberID=" + memberId + '}';
    }
}
