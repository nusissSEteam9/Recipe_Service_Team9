package nus.iss.se.team9.recipe_service_team9.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class MemberReport extends Report {
	@ManyToOne
	@JsonBackReference(value = "member-reportsToMember")
	private Member memberReported;

	//getter and setter
	public Member getMemberReported() {
		return memberReported;
	}

	public void setMemberReported(Member memberReported) {
		this.memberReported = memberReported;
	}
	
}
