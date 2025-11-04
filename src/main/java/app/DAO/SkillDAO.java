package app.DAO;

import app.entities.Candidate;
import app.entities.Skill;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.List;

public class SkillDAO implements IDAO<Skill, Integer>{

    private final EntityManagerFactory emf;

    public SkillDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }


    @Override
    public Skill create(Skill skill) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(skill);
            em.getTransaction().commit();
            return skill;
        }
    }

    @Override
    public Skill getById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT s FROM Skill s LEFT JOIN FETCH s.candidateSkills WHERE s.id = :id", Skill.class
                    )
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Skill update(Skill skill) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.merge(skill);
            em.getTransaction().commit();
        }
        return skill;
    }

    @Override
    public List<Skill> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT s FROM Skill s LEFT JOIN FETCH s.candidateSkills", Skill.class
            ).getResultList();
        }
    }

    @Override
    public boolean delete(Integer id) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();

            Skill toDelete = em.find(Skill.class,id);
            if(toDelete != null){
                em.remove(toDelete);
                em.getTransaction().commit();
                return true;
            } else {
                em.getTransaction().rollback();
                return false;
            }
        }
    }
}
