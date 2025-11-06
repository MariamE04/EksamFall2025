package app.controllers;

import Security.daos.SecurityDAO;
import Security.entities.Role;
import Security.entities.User;
import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.entities.Candidate;
import app.entities.Skill;
import app.enums.Category;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class SkillControllerTest {
    private static Javalin app;
    private static EntityManagerFactory emf;
    private String token;
    private SecurityDAO securityDAO = new SecurityDAO(emf);

    @BeforeAll
    static void setup() {
        emf = HibernateConfig.getEntityManagerFactoryForTest();

        // Start Javalin-serveren
        app = ApplicationConfig.getInstance().startServer(7071);

        // REST-Assured base setup
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7071;
        RestAssured.basePath = "/api/candidateMatch/skills";
    }


    @BeforeEach
    void prepareTestData() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Sletter gamle data først
            em.createQuery("DELETE FROM CandidateSkill").executeUpdate();
            em.createQuery("DELETE FROM CandidateSkill").executeUpdate();
            em.createQuery("DELETE FROM Candidate ").executeUpdate();
            em.createQuery("DELETE FROM Skill").executeUpdate();
            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE candidate_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE skill_id_seq RESTART WITH 1").executeUpdate();

            // Opret admin-bruger og role
            User admin = new User("admin", "admin123");
            Role adminRole = new Role("ADMIN");
            admin.addRole(adminRole);

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

            // Opretter kandidater
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

            // // Knytter skills
            candidate1.addSkill(java);
            candidate1.addSkill(sql);
            candidate2.addSkill(html);

            // Persist alt i korrekt rækkefølge
            em.persist(adminRole);
            em.persist(admin);
            em.persist(java);
            em.persist(sql);
            em.persist(html);
            em.persist(candidate1);
            em.persist(candidate2);

            tx.commit();

        } finally {
            em.close();
        }

        // Hent token fra login-endpointet EFTER commit
        token = given()
                .contentType("application/json")
                .body("{\"username\": \"admin\", \"password\": \"admin123\"}")
                .when()
                .post("http://localhost:7071/api/candidateMatch/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");

    }

    @Test
    void getAllSkills() {
        given()
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2))
                .body("[0].name", notNullValue());
    }

    @Test
    void getSkillById() {
        given()
                .when()
                .get("/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Java"))
                .body("category", equalTo("PROG_LANG"))
                .body("description", equalTo("Java backend development"));
    }

    @Test
    void createSkill() {
        String newSkill = """
                {
                  "name": "Docker",
                  "category": "DEVOPS",
                  "description": "Containerization tool"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(newSkill)
                .when()
                .post("/")
                .then()
                .statusCode(201)
                .body("name", notNullValue())
                .body("category", notNullValue());
    }

    @Test
    void updateSkill() {
        String json = """
                {
                  "name": "Java -updated",
                  "category": "PROG_LANG",
                  "description": "Java backend development -updated"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(json)
                .when()
                .put("/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Java -updated"))
                .body("description", equalTo("Java backend development -updated"))
                .body("category", equalTo("PROG_LANG"));
    }

    @Test
    void deleteSkill() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/2")
                .then()
                .statusCode(anyOf(is(200), is(204)));

        given()
                .when()
                .get("/2")
                .then()
                .statusCode(404);
    }
}