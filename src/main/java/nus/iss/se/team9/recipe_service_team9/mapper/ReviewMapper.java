package nus.iss.se.team9.recipe_service_team9.mapper;

import nus.iss.se.team9.recipe_service_team9.model.Review;
import nus.iss.se.team9.recipe_service_team9.model.ReviewDTO;

public class ReviewMapper {
    public static ReviewDTO toReviewDTO(Review review) {
        if (review == null) {
            return null;
        }

        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewDate(review.getReviewDate());
        if (review.getMember() != null) {
            dto.setReviewerUsername(review.getMember().getUsername());
            dto.setMemberId(review.getMember().getId());
        } else {
            dto.setReviewerUsername("Unknown");
            dto.setMemberId(null);
        }

        return dto;
    }
}
