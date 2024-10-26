package nus.iss.se.team9.recipe_service_team9.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
public class MemberReport extends Report {
	@ManyToOne
	@JsonBackReference(value = "member-reportsToMember")
	private Member memberReported;
}
