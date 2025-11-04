package app.config;

import app.entities.Candidate;
import app.entities.CandidateSkill;
import app.entities.Skill;
import app.utils.Utils;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateConfig {
    private static EntityManagerFactory emf; // Forbindelsen til den rigtige database (bruges i udvikling og produktion).
    private static EntityManagerFactory emfTest; // Forbindelsen til test-databasen, så man ikke ødelægger den rigtige database.
    private static Boolean isTest = false; // Fortæller om man kører i test-tilstand eller ej.

    //Getter og setter bruges bare til at ændre og hente isTest.
    public static void setTest(Boolean test) {
        isTest = test;
    }

    public static Boolean getTest() {
        return isTest;
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) // vis emf ikke eksisterer endnu (null), skal den oprettes.
            emf = createEMF(getTest()); // kalder createEMF(getTest()), som laver forbindelsen til databasen.
        return emf; // returneres forbindelsen (emf), så andre klasser kan bruge den.
    }

    public static EntityManagerFactory getEntityManagerFactoryForTest() {
        if (emfTest == null){ // Hvis emfTest ikke findes endnu, så
            setTest(true); // Kaldes setTest(true) → nu er vi i test-tilstand.
            emfTest = createEMF(getTest()); // opretter en testdatabase (midlertidig).
        }
        return emfTest;
    }

    // Denne metode fortæller Hibernate hvilke klasser (entities) der skal laves som tabeller i databasen.
    private static void getAnnotationConfiguration(Configuration configuration) {
        configuration.addAnnotatedClass(Candidate.class);
        configuration.addAnnotatedClass(Skill.class);
        configuration.addAnnotatedClass(CandidateSkill.class);
        //configuration.addAnnotatedClass(User.class);
        //configuration.addAnnotatedClass(Role.class);
    }

    // opretter selve forbindelsen til databasen og vælger det rigtige miljø.
    private static EntityManagerFactory createEMF(boolean forTest) {
        try {
            Configuration configuration = new Configuration(); // et Hibernate-objekt, hvor vi samler alle indstillinger.
            Properties props = new Properties(); // en samling af nøgle–værdi par
            setBaseProperties(props);  // Denne metode tilføjer de grundlæggende Hibernate-indstillinge
            if (forTest) {
                props = setTestProperties(props);
            } else if (System.getenv("DEPLOYED") != null) {
                setDeployedProperties(props);
            } else {
                props = setDevProperties(props);
            }
            configuration.setProperties(props); // Tilføjer alle props (indstillingerne) til configuration
            getAnnotationConfiguration(configuration); // Tilføjer også alle entity-klasser (så Hibernate ved, hvad den skal lave tabeller af).

            //er “byggeren” som indeholder alle Hibernate-services (f.eks. databaseforbindelser, caching, osv.).
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();
            SessionFactory sf = configuration.buildSessionFactory(serviceRegistry); // selve forbindelsen mellem Hibernate og databasen.
            EntityManagerFactory emf = sf.unwrap(EntityManagerFactory.class);
            return emf;
        }
        catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static String getDBName() {
        return Utils.getPropertyValue("db.name", "from-pom.properties");
    }

    private static Properties setBaseProperties(Properties props) {
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.hbm2ddl.auto", "create");  // set to "update" when in production
        props.put("hibernate.current_session_context_class", "thread");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.use_sql_comments", "false");
        return props;
    }

    private static Properties setDeployedProperties(Properties props) {
        String DBName = System.getenv("DB_NAME");
        props.setProperty("hibernate.connection.url", System.getenv("CONNECTION_STR") + DBName);
        props.setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"));
        props.setProperty("hibernate.connection.password", System.getenv("DB_PASSWORD"));
        return props;
    }

    private static Properties setDevProperties(Properties props){
        props.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/"+ getDBName());
        props.put("hibernate.connection.username", "postgres");
        props.put("hibernate.connection.password", "postgres");
        return props;
    }

    private static Properties setTestProperties(Properties props) {
        props.put("hibernate.connection.driver_class", "org.testcontainers.jdbc.ContainerDatabaseDriver");
        props.put("hibernate.connection.url", "jdbc:tc:postgresql:16.2:///test_db");
        props.put("hibernate.archive.autodetection", "hbm,class");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        return props;
    }
}