package nus.iss.se.team9.recipe_service_team9.filterStrategy;

import nus.iss.se.team9.recipe_service_team9.model.Recipe;
import nus.iss.se.team9.recipe_service_team9.service.JwtService;
import nus.iss.se.team9.recipe_service_team9.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

public class HealthScoreFilterStrategy implements FilterStrategy {
    @Override
    public List<Recipe> filter(List<Recipe> recipes, String token, UserService userService, JwtService jwtService) {
        return recipes.stream()
                .filter(r -> r.getHealthScore() >= 4)
                .collect(Collectors.toList());
    }
}

