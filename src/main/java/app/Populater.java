package app;

import Security.daos.SecurityDAO;
import Security.entities.Role;
import Security.entities.User;
import app.DAO.CandidateDAO;
import app.DAO.SkillDAO;
import app.config.HibernateConfig;
import app.entities.Candidate;
import app.entities.Skill;
import app.enums.Category;
import jakarta.persistence.EntityManagerFactory;

public class Populater {

    public static void populate(EntityManagerFactory emf) {
        CandidateDAO candidateDAO = new CandidateDAO(emf);
        SkillDAO skillDAO = new SkillDAO(emf);
        SecurityDAO securityDAO = new SecurityDAO(emf);

        try{
            User admin = securityDAO.createUser("admin", "admin123");
            securityDAO.createRole("ADMIN");
            securityDAO.addUserRole(admin.getUsername(), "ADMIN");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Opretter skills
        Skill java = Skill.builder()
                .name("Java")
                .category(Category.PROG_LANG)
                .description("Java backend development")
                .build();

        Skill sql = Skill.builder()
                .name("SQL")
                .category(Category.DATA)
                .description("Database querying")
                .build();

        Skill html = Skill.builder()
                .name("HTML")
                .category(Category.FRONTEND)
                .description("Markup for web structure")
                .build();

        Skill devops = Skill.builder()
                .name("Docker")
                .category(Category.DEVOPS)
                .description("DevOps tools")
                .build();

        skillDAO.create(java);
        skillDAO.create(sql);
        skillDAO.create(html);
        skillDAO.create(devops);

        // Opretere kandidater
        Candidate candidate1 = Candidate.builder()
                .name("Mariam El-Mir")
                .phone("12345678")
                .educationBackground("Datamatiker")
                .build();

        Candidate candidate2 = Candidate.builder()
                .name("Jonas Hansen")
                .phone("87654321")
                .educationBackground("Software Engineer")
                .build();

        // Gemmer kandidater i db
        candidateDAO.create(candidate1);
        candidateDAO.create(candidate2);

        // Tilføjer skills til kandidater
        candidateDAO.addSkillToCandidate(candidate1.getId(), java.getId());
        candidateDAO.addSkillToCandidate(candidate1.getId(), sql.getId());
        candidateDAO.addSkillToCandidate(candidate2.getId(), html.getId());
        candidateDAO.addSkillToCandidate(candidate2.getId(), sql.getId());
        candidateDAO.addSkillToCandidate(candidate1.getId(), devops.getId());

        System.out.println("Testdata oprettet med kandidater og skills!");
    }

    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        populate(emf);
    }
}
