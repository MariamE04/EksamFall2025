package app.mappers;

import app.dtos.CandidateDTO;
import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;

import java.util.ArrayList;
import java.util.List;

public class CandidateMapper {
    public static CandidateDTO toDto(Candidate candidate){
        List<Integer> skillIds = new ArrayList<>();
        if (candidate.getCandidateSkills() != null) {
            for (CandidateSkill cs : candidate.getCandidateSkills()) {
                skillIds.add(cs.getSkill().getId());
            }
        }

        return new CandidateDTO(
                candidate.getId(),
                candidate.getName(),
                candidate.getPhone(),
                candidate.getEducationBackground(),
                skillIds
        );
    }
}
