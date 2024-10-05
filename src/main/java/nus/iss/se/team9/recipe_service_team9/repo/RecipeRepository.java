package nus.iss.se.team9.recipe_service_team9.repo;

import nus.iss.se.team9.recipe_service_team9.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {

	@Query("SELECT DISTINCT r.tags FROM Recipe r")
	List<String> findAllDistinctTags();

	@Query("SELECT r FROM Recipe r WHERE r.name LIKE %:name% AND r.status = 'PUBLIC'")
	List<Recipe> findByNameContaining(@Param("name") String name);

	@Query("SELECT r FROM Recipe r JOIN r.tags t WHERE t LIKE %:tag% AND r.status = 'PUBLIC'")
	List<Recipe> findByTagsContaining(@Param("tag") String tag);

	@Query("SELECT r FROM Recipe r WHERE r.description LIKE %:description% AND r.status = 'PUBLIC'")
	List<Recipe> findByDescriptionContaining(@Param("description") String description);

	@Query("SELECT r FROM Recipe r WHERE r.member = :member AND r.status = :status")
	List<Recipe> findByMember(Member member, Status status);

	@Query("SELECT r FROM Recipe r WHERE FUNCTION('YEAR', r.submittedDate) = :year ORDER BY r.submittedDate")
	List<Recipe> getAllRecipesByYear(@Param("year") int year);

	@Query("SELECT t, COUNT(r) AS recipeCount FROM Recipe r JOIN r.tags t GROUP BY t ORDER BY recipeCount DESC")
	List<Object[]> getRecipeCountByTag();

	@Query("SELECT r FROM Review r WHERE r.recipe = :recipe ORDER BY r.reviewDate DESC")
	List<Review> getReviewsByRecipe(@Param("recipe") Recipe recipe);

	@Query("SELECT r FROM Recipe r JOIN r.tags t WHERE t LIKE %:tag% AND r.status = 'PUBLIC'")
	Page<Recipe> findByTagsContainingByPage(@Param("tag") String tag, Pageable pageable);

	@Query("SELECT r FROM Recipe r WHERE r.name LIKE %:name% AND r.status = 'PUBLIC'")
	Page<Recipe> findByNameContainingByPage(@Param("name") String name, Pageable pageable);

	@Query("SELECT r FROM Recipe r WHERE r.description LIKE %:description% AND r.status = 'PUBLIC'")
	Page<Recipe> findByDescriptionContainingByPage(@Param("description") String description, Pageable pageable);

	@Query("SELECT r FROM Recipe r WHERE r.status = 'PUBLIC'")
	Page<Recipe> findAllPublic(PageRequest pageRequest);

	List<Recipe> findAllByOrderByRatingAsc();

	List<Recipe> findAllByOrderByRatingDesc();

	List<Recipe> findAllByOrderByNumberOfSavedAsc();

	List<Recipe> findAllByOrderByNumberOfSavedDesc();

	List<Recipe> findAllByOrderByHealthScoreAsc();

	List<Recipe> findAllByOrderByHealthScoreDesc();

}
