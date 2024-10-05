package nus.iss.se.team9.recipe_service_team9.repo;

import nus.iss.se.team9.recipe_service_team9.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeReportRepository extends JpaRepository<RecipeReport,Integer>{

	List<RecipeReport> findByStatus(Status pending);

}
