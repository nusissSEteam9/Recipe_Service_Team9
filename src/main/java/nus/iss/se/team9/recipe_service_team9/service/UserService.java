package nus.iss.se.team9.recipe_service_team9.service;

import jakarta.transaction.Transactional;
import nus.iss.se.team9.recipe_service_team9.mapper.MemberMapper;
import nus.iss.se.team9.recipe_service_team9.model.Member;
import nus.iss.se.team9.recipe_service_team9.model.MemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class UserService {
    private final String userServiceUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public UserService(RestTemplate restTemplate, @Value("${user.service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    public Member getMemberById(int id) {
        String url = userServiceUrl + "/member/" + id;
        try {
            ResponseEntity<MemberDTO> response = restTemplate.exchange(url, HttpMethod.GET, null, MemberDTO.class);
            MemberDTO memberDTO = response.getBody();
            if (memberDTO != null) {
                return MemberMapper.toMember(memberDTO);
            } else {
                System.out.println("Member not found with ID: " + id);
                return null;
            }
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

    public Boolean checkIfRecipeSaved(Integer recipeId, String token) {
        String url = userServiceUrl + "/checkIfRecipeSaved?recipeId=" + recipeId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Boolean> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Boolean.class
        );
        return response.getBody();
    }
}
