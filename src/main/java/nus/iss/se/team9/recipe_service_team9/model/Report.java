package nus.iss.se.team9.recipe_service_team9.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
public abstract class Report {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column
	@NotBlank(message = "Reason is required")
	private String reason;
	@Enumerated(EnumType.STRING)
	private Status status;
	@ManyToOne
//	@JsonBackReference(value = "member-reports")
	@JsonIgnore
	private Member member;
}
