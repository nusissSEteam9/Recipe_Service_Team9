package nus.iss.se.team9.recipe_service_team9.repo;

import nus.iss.se.team9.recipe_service_team9.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient,Integer>{

	@Query("SELECT i FROM Ingredient i "
			+ "WHERE i.foodText = :foodText")
	Optional<Ingredient> findByfoodText(@Param("foodText") String foodText);

}
