package nus.iss.se.team9.recipe_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.recipe_service_team9.model.Ingredient;
import nus.iss.se.team9.recipe_service_team9.repo.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class IngredientService {
    private final IngredientRepository ingredientRepo;

    @Autowired
    public IngredientService(IngredientRepository ingredientRepo) {
        this.ingredientRepo = ingredientRepo;
    }

    // get specific ingredient by id
    public Ingredient getIngredientById(Integer id) {
        Optional<Ingredient> ingredient = ingredientRepo.findById(id);
        return ingredient.orElse(null);
    }

    // save ingredient
    public void saveIngredient(Ingredient ingredient) {
        ingredientRepo.save(ingredient);
    }
}
