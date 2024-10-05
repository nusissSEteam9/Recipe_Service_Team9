package nus.iss.se.team9.recipe_service_team9.repo;

import nus.iss.se.team9.recipe_service_team9.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
	User findByUsername(String username);

}
