package Security.daos;


import Security.entities.Role;
import Security.entities.User;
import Security.exceptions.ValidationException;
import app.config.HibernateConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;

public class SecurityDAO implements ISecurityDAO {

    private static ISecurityDAO instance;
    private static EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory _emf) {
        emf = _emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }


    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException {
        try(EntityManager em = emf.createEntityManager()){
            User foundUser =  em.find(User.class,username);
            if(foundUser.checkPassword(password)){
                return foundUser;
            } else {
                throw new ValidationException("User or password is incorrect");
            }
        }
    }

    @Override
    public User createUser(String username, String password) {
        try(EntityManager em = emf.createEntityManager()){
            User user = new User(username, password);
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();

            return user;
        }
    }

    @Override
    public Role createRole(String rolename) {
        try(EntityManager em = emf.createEntityManager()) {
            Role role = new Role(rolename);
            em.getTransaction().begin();
            em.persist(role);
            em.getTransaction().commit();

            return role;
        }
    }

    @Override
    public User addUserRole(String username, String role) throws EntityNotFoundException {
        try(EntityManager em = emf.createEntityManager()) {
            User foundUser = em.find(User.class, username);
            Role foundRole = em.find(Role.class, role);

            if (foundRole == null || foundUser == null) {
                throw new EntityNotFoundException("User or Role does not eksist");
            }
            em.getTransaction().begin();

            foundUser.addRole(foundRole);

            em.getTransaction().commit();

            return foundUser;
        }
    }

    public User getUserByUsername(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    public static void main(String[] args) throws EntityNotFoundException {
        ISecurityDAO dao= new SecurityDAO(HibernateConfig.getEntityManagerFactory());
        dao.createUser("user2", "pass124");
        dao.createRole("Admin");
        dao.addUserRole("user2", "Admin");
    }

}

