package app.controllers;

import app.DAO.CandidateDAO;
import app.DAO.SkillDAO;
import app.config.HibernateConfig;
import app.dtos.SkillDTO;
import app.entities.Candidate;
import app.entities.Skill;
import app.mappers.SkillMapper;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class SkillController {
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private CandidateDAO candidateDAO = new CandidateDAO(emf);
    private SkillDAO skillDAO = new SkillDAO(emf);

    // GET /skill
    public void getAllSkills(Context ctx){
        List<Skill> skills = skillDAO.getAll();
        List<SkillDTO> skillDTOS = skills.stream().map(SkillMapper::toDto).toList();
        ctx.status(HttpStatus.OK).json(skillDTOS);
    }

    // GET /skill/{id}
    public void getSkillById(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        Skill skill = skillDAO.getById(id);

        if(skill != null){
            ctx.status(HttpStatus.OK).json(SkillMapper.toDto(skill));
        } else {
            ctx.status(HttpStatus.NOT_FOUND).result("Skill not found");
        }
    }

    // POST /skill
    public void createSkill(Context ctx){
        SkillDTO dto = ctx.bodyAsClass(SkillDTO.class);

        Skill skill = new Skill();
        skill.setName(dto.getName());
        skill.setCategory(dto.getCategory());
        skill.setDescription(dto.getDescription());

        Skill created = skillDAO.create(skill);
        ctx.status(HttpStatus.CREATED).json(SkillMapper.toDto(created));
    }

    // PUT /skill/{id}
    public void updateSkill(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        SkillDTO dto = ctx.bodyAsClass(SkillDTO.class);

        Skill existing = skillDAO.getById(id);
        if (existing == null){
            ctx.status(HttpStatus.NOT_FOUND).result("Skill not found");
            return;
        }

        existing.setName(dto.getName());
        existing.setCategory(dto.getCategory());
        existing.setDescription(dto.getDescription());

        Skill updated = skillDAO.update(existing);
        ctx.status(HttpStatus.OK).json(SkillMapper.toDto(updated));
    }

    // DELETE /skill/{id}
    public void deleteSkill(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        boolean deleted = skillDAO.delete(id);
        if(deleted){
            ctx.status(HttpStatus.NO_CONTENT).result("Skill deleted");
        } else {
            ctx.status(HttpStatus.NOT_FOUND).result("Skill not found");
        }
    }

}
