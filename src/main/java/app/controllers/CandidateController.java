package app.controllers;

import app.DAO.CandidateDAO;
import app.DAO.SkillDAO;
import app.config.HibernateConfig;
import app.dtos.CandidateDTO;
import app.dtos.SkillStatsDTO;
import app.entities.Candidate;
import app.entities.Skill;
import app.enums.Category;
import app.mappers.CandidateMapper;
import app.servies.SkillStatsService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CandidateController {
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private CandidateDAO candidateDAO = new CandidateDAO(emf);
    private SkillDAO skillDAO = new SkillDAO(emf);

    // GET /candidates
    public void getAllCandidates(Context ctx){
        String categoryParam = ctx.queryParam("category"); // kan være null
        List<Candidate> candidates = candidateDAO.getAll();

        if (categoryParam != null) {
            try {
                Category filterCategory = Category.valueOf(categoryParam.toUpperCase());
                candidates = candidates.stream()
                        .filter(c -> c.getCandidateSkills().stream()
                                .anyMatch(cs -> cs.getSkill().getCategory() == filterCategory))
                        .toList();
            } catch (IllegalArgumentException e) {
                ctx.status(HttpStatus.BAD_REQUEST).json("Invalid category or does not exist");
                return;
            }
        }

        List<CandidateDTO> candidateDTOS = candidates.stream()
                .map(CandidateMapper::toDto)
                .toList();
        ctx.status(HttpStatus.OK).json(candidateDTOS);
    }

    // GET /candidate/{id}
    public void getCandidateById(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        Candidate candidate = candidateDAO.getById(id);

        if(candidate != null){
            ctx.status(HttpStatus.OK).json(CandidateMapper.toDto(candidate));

        List<SkillStatsDTO> skillStats = SkillStatsService.skillsWithStats

        } else {
            ctx.status(HttpStatus.NOT_FOUND).result("Candidate not found");
        }
    }

    // POST /candidate
    public void createCandidate(Context ctx){
        CandidateDTO dto = ctx.bodyAsClass(CandidateDTO.class);

        Candidate candidate = new Candidate();
        candidate.setName(dto.getName());
        candidate.setPhone(dto.getPhone());
        candidate.setEducationBackground(dto.getEducationBackground());

        Candidate created = candidateDAO.create(candidate);
        ctx.status(HttpStatus.CREATED).json(CandidateMapper.toDto(created));
    }

    // PUT /candidate/{id}
    public void updateCandidate(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        CandidateDTO dto = ctx.bodyAsClass(CandidateDTO.class);

        Candidate existing = candidateDAO.getById(id);
        if (existing == null){
            ctx.status(HttpStatus.NOT_FOUND).result("Candidate not found");
            return;
        }

        existing.setName(dto.getName());
        existing.setPhone(dto.getPhone());
        existing.setEducationBackground(dto.getEducationBackground());

        Candidate updated = candidateDAO.update(existing);
        ctx.status(HttpStatus.OK).json(CandidateMapper.toDto(updated));
    }

    // DELETE /candidate/{id}
    public void deleteCandidate(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        boolean deleted = candidateDAO.delete(id);
        if(deleted){
            ctx.status(HttpStatus.NO_CONTENT).result("Candidate deleted");
        } else {
            ctx.status(HttpStatus.NOT_FOUND).result("Candidate not found");
        }
    }

    // PUT /candidates/{candidateId}/skills/{skillId}
    public void addSkillToCandidate(Context ctx) {
        int candidateId = Integer.parseInt(ctx.pathParam("candidateId"));
        int skillId = Integer.parseInt(ctx.pathParam("skillId"));
        candidateDAO.addSkillToCandidate(candidateId, skillId);
        ctx.status(200).json("Skill added to candidate");
    }

}
