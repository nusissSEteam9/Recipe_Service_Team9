package nus.iss.se.team9.recipe_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.recipe_service_team9.model.*;
import nus.iss.se.team9.recipe_service_team9.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class RecipeService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RecipeRepository recipeRepository;

    // get specific recipe by id
    public Recipe getRecipeById(Integer id) {
        Optional<Recipe> recipe = recipeRepository.findById(id);
        return recipe.orElse(null);
    };

    public List<String> findMatchingTags(String keyword) {
        Set<String> allUniqueTags = getAllUniqueTags();
        return allUniqueTags.stream()
                .filter(tag -> tag.toLowerCase().contains(keyword.toLowerCase())).collect(Collectors.toList());
    }

    // get all unique tags
    public Set<String> getAllUniqueTags() {
        List<String> tagLists = recipeRepository.findAllDistinctTags();
        Set<String> uniqueTags = new HashSet<>();
        for (String tags : tagLists) {
            uniqueTags.addAll(Arrays.asList(tags.split(",")));
        }
        return uniqueTags;
    }

    // save specific recipe by id
    public void saveRecipe(Recipe recipe, Member member) {
        member.getSavedRecipes().add(recipe);
        recipe.getMembersWhoSave().add(member);
        recipe.setNumberOfSaved(recipe.getNumberOfSaved() + 1);
        memberRepository.save(member);
        recipeRepository.save(recipe);
    }

    // unsubscribe specific recipe by id
    public void unsubscribeRecipe(Recipe recipe, Member member) {
        member.getSavedRecipes().remove(recipe);
        recipe.getMembersWhoSave().remove(member);
        recipe.setNumberOfSaved(recipe.getNumberOfSaved() - 1);
        memberRepository.save(member);
        recipeRepository.save(recipe);

    }

    public Page<Recipe> searchByTag(String tag, int pageNo, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize);
        return recipeRepository.findByTagsContainingByPage(tag, pageRequest);
    }

    public List<Recipe> searchByTag(String tag) {
        return recipeRepository.findByTagsContaining(tag);
    }

    public List<Recipe> searchByName(String query) {
        return recipeRepository.findByNameContaining(query);
    }

    public List<Recipe> searchByDescription(String query) {
        return recipeRepository.findByDescriptionContaining(query);
    }

    public List<Recipe> searchAll(String query) {
        List<Recipe> results1 = recipeRepository.findByNameContaining(query);
        List<Recipe> results2 = recipeRepository.findByTagsContaining(query);
        List<Recipe> results3 = recipeRepository.findByDescriptionContaining(query);
        return mergeLists(results1, results2, results3);
    }

    // merge
    public List<Recipe> mergeLists(List<Recipe> listByName, List<Recipe> listByTag, List<Recipe> listByDescription) {
        return Stream.of(listByName, listByTag, listByDescription).flatMap(List::stream).distinct()
                .collect(Collectors.toList());
    }

    public void createRecipe(Recipe newRecipe) {
        recipeRepository.save(newRecipe);
    }

    public void updateRecipe(Recipe newRecipe) {
        recipeRepository.save(newRecipe);
    }


}
