package app.mappers;

import app.dtos.CandidateDTO;
import app.dtos.SkillDTO;
import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;

import java.util.ArrayList;
import java.util.List;

public class SkillMapper {
    public static SkillDTO toDto(Skill skill){
        List<Integer> candidateIds = new ArrayList<>();
        if (skill.getCandidateSkills() != null) {
            for (CandidateSkill cs : skill.getCandidateSkills()) {
                candidateIds.add(cs.getCandidate().getId());
            }
        }

        return new SkillDTO(
                skill.getId(),
                skill.getName(),
                skill.getSlug(),
                skill.getCategory(),
                skill.getDescription(),
                candidateIds
        );
    }
}
