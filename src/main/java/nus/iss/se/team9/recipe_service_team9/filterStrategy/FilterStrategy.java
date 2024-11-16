package nus.iss.se.team9.recipe_service_team9.filterStrategy;

import nus.iss.se.team9.recipe_service_team9.model.Recipe;
import nus.iss.se.team9.recipe_service_team9.service.JwtService;
import nus.iss.se.team9.recipe_service_team9.service.UserService;

import java.util.List;

public interface FilterStrategy {
    List<Recipe> filter(List<Recipe> recipes, String token, UserService userService, JwtService jwtService);
}