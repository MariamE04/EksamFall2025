package app.DAO;

import app.entities.Candidate;
import app.entities.Skill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.List;

public class CandidateDAO implements IDAO<Candidate, Integer>{

    private final EntityManagerFactory emf;

    public CandidateDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Candidate create(Candidate candidate) {
        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(candidate);
            em.getTransaction().commit();
        }
        return candidate;
    }

    @Override
    public Candidate getById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT c FROM Candidate c LEFT JOIN FETCH c.candidateSkills cs LEFT JOIN FETCH cs.skill WHERE c.id = :id",
                            Candidate.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Candidate update(Candidate candidate) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.merge(candidate);
            em.getTransaction().commit();
        }
        return candidate;
    }

    @Override
    public List<Candidate> getAll() {
       try(EntityManager em = emf.createEntityManager()){
           return em.createQuery(
                           "SELECT DISTINCT c FROM Candidate c LEFT JOIN FETCH c.candidateSkills cs LEFT JOIN FETCH cs.skill",
                           Candidate.class)
                   .getResultList();
       }
    }

    @Override
    public boolean delete(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            Candidate toDelete = em.find(Candidate.class, id);
            if (toDelete != null) {

                // Fjern alle tilknyttede CandidateSkill-rækker først (foreign key constraint error.)
                em.createQuery("DELETE FROM CandidateSkill cs WHERE cs.candidate.id = :cid")
                        .setParameter("cid", id)
                        .executeUpdate();

                // Slet kandidaten
                em.remove(toDelete);

                em.getTransaction().commit();
                return true;
            } else {
                em.getTransaction().rollback();
                return false;
            }
        }
    }

    public void addSkillToCandidate(int candidateId, int skillId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Candidate candidate = em.find(Candidate.class, candidateId);
            Skill skill = em.find(Skill.class, skillId);

            if (candidate != null && skill != null) {
                candidate.addSkill(skill);
                em.merge(candidate); // sørger for at ændringen bliver gemt
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
