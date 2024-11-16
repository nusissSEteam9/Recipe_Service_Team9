package nus.iss.se.team9.recipe_service_team9.factory;

import nus.iss.se.team9.recipe_service_team9.searchStrategy.*;

public class SearchStrategyFactory {
    public static SearchStrategy getSearchStrategy(String type) {
        return switch (type) {
            case "tag" -> new SearchByTagStrategy();
            case "name" -> new SearchByNameStrategy();
            case "description" -> new SearchByDescriptionStrategy();
            default -> new SearchAllStrategy();
        };
    }
}

