package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

import static app.Populater.populate;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        System.out.println("test");

        ApplicationConfig config = ApplicationConfig.getInstance();
        config.startServer(7071);

        populate(emf);

    }

}