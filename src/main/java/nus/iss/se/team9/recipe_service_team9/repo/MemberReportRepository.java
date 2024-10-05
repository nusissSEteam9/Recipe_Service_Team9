package nus.iss.se.team9.recipe_service_team9.repo;

import nus.iss.se.team9.recipe_service_team9.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberReportRepository extends JpaRepository<MemberReport,Integer>{

	List<MemberReport> findByMemberReportedAndStatus(Member member, Status approved);

	List<MemberReport> findByStatus(Status pending);

}
