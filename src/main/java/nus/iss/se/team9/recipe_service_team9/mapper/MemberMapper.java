package nus.iss.se.team9.recipe_service_team9.mapper;

import nus.iss.se.team9.recipe_service_team9.model.Member;
import nus.iss.se.team9.recipe_service_team9.model.MemberDTO;
import nus.iss.se.team9.recipe_service_team9.model.Status;

public class MemberMapper {
    public static Member toMember(MemberDTO memberDTO) {
        Member member = new Member();
        member.setId(memberDTO.getId());
        member.setUsername(memberDTO.getUsername());
        member.setEmail(memberDTO.getEmail());
        member.setHeight(memberDTO.getHeight());
        member.setWeight(memberDTO.getWeight());
        member.setAge(memberDTO.getAge());
        member.setBirthdate(memberDTO.getBirthdate());
        member.setGender(memberDTO.getGender());
        member.setCalorieIntake(memberDTO.getCalorieIntake());
        member.setRegistrationDate(memberDTO.getRegistrationDate());
        member.setMemberStatus(Status.valueOf(memberDTO.getMemberStatus().toUpperCase()));
        member.setPreferenceList(memberDTO.getPreferenceList());
        return member;
    }
}
