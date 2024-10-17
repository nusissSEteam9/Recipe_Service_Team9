package nus.iss.se.team9.recipe_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.recipe_service_team9.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class UserService {
    private final String userServiceUrl;
    private final RestTemplate restTemplate;
    @Autowired
    public UserService(RestTemplate restTemplate,@Value("${user.service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    public Member getMemberById(int id) {
        String url = userServiceUrl + "/member/" + id;
        try {
            ResponseEntity<Member> response = restTemplate.exchange(url, HttpMethod.GET, null, Member.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            System.out.println("Member not found with ID: " + id);
            return null;
        } catch (HttpClientErrorException e) {
            System.out.println("Error response from server: " + e.getStatusCode());
            throw e;
        } catch (Exception e) {
            System.out.println("Error occurred while retrieving member: " + e.getMessage());
            throw new RuntimeException("Error occurred while retrieving member: " + e.getMessage());
        }
    }

    public ResponseEntity<String> saveRecipeToMemberSavedList(Integer memberId, Integer recipeId) {
        try {
            String url = userServiceUrl + "/member/saveRecipe";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("memberId", memberId);
            requestBody.put("recipeId", recipeId);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok("Recipe saved successfully in member-api: " + response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to save recipe: " + response.getBody());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }


    public ResponseEntity<String> removeRecipeFromMemberSavedList(Integer memberId, Recipe recipe){
        try {
            String url = userServiceUrl + "/member/"+ memberId + "/removeRecipe";
            ResponseEntity<String> response = restTemplate.postForEntity(url, recipe, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok("Recipe removed successfully in member-api: " + response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to fail recipe: " + response.getBody());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred: " + e.getMessage());
        }
    }
}
