package nus.iss.se.team9.recipe_service_team9.factory;

import nus.iss.se.team9.recipe_service_team9.filterStrategy.CalorieFilterStrategy;
import nus.iss.se.team9.recipe_service_team9.filterStrategy.FilterStrategy;
import nus.iss.se.team9.recipe_service_team9.filterStrategy.HealthScoreFilterStrategy;

public class FilterStrategyFactory {
    public static FilterStrategy getFilterStrategy(String filterType) {
        return switch (filterType) {
            case "filter1" -> new HealthScoreFilterStrategy();
            case "filter2" -> new CalorieFilterStrategy();
            default -> null;
        };
    }
}