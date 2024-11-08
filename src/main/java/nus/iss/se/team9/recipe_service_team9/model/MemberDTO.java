package nus.iss.se.team9.recipe_service_team9.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MemberDTO {
    private Integer id;
    private String username;
    private String email;
    private Double height;
    private Double weight;
    private Integer age;
    private LocalDate birthdate;
    private String gender;
    private Double calorieIntake;
    private LocalDate registrationDate;
    private String memberStatus;  // Representing the enum value as a string
    private List<String> preferenceList;

    @Override
    public String toString() {
        return "MemberDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", age=" + age +
                ", birthdate=" + birthdate +
                ", gender='" + gender + '\'' +
                ", calorieIntake=" + calorieIntake +
                ", registrationDate=" + registrationDate +
                ", memberStatus='" + memberStatus + '\'' +
                ", preferenceList=" + preferenceList +
                '}';
    }
}
