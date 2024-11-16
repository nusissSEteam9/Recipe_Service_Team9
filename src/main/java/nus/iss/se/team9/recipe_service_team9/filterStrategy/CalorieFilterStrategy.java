package nus.iss.se.team9.recipe_service_team9.filterStrategy;

import nus.iss.se.team9.recipe_service_team9.exception.UnauthorizedException;
import nus.iss.se.team9.recipe_service_team9.model.Member;
import nus.iss.se.team9.recipe_service_team9.model.Recipe;
import nus.iss.se.team9.recipe_service_team9.service.JwtService;
import nus.iss.se.team9.recipe_service_team9.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

public class CalorieFilterStrategy implements FilterStrategy {
    @Override
    public List<Recipe> filter(List<Recipe> recipes, String token, UserService userService, JwtService jwtService) {
        Integer memberId = jwtService.extractId(token);
        if (memberId == null) {
            throw new UnauthorizedException("Invalid token");
        }
        Member member = userService.getMemberById(memberId);
        Double calorieIntake = member.getCalorieIntake();
        if (calorieIntake == null) {
            throw new IllegalArgumentException("Calorie intake not found");
        }
        return recipes.stream()
                .filter(r -> r.getCalories() <= (calorieIntake / 3))
                .collect(Collectors.toList());
    }
}