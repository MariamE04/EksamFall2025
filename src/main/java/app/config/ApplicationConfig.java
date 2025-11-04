package app.config;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import app.routes.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

// starter serveren og fortæller den, hvor endpoints er.

public class ApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

   // private static ISecurityController securityController = new SecurityController();

    private static Routes routes = new Routes(); // instans af Routes, som indeholder alle endpoints
    private static ApplicationConfig appConfig;
    private Javalin app;


    private ApplicationConfig() {
    }

    public static ApplicationConfig getInstance() {
        if (appConfig == null) {
            appConfig = new ApplicationConfig();
        }
        return appConfig;
    }


   /* public ApplicationConfig checkSecurityRoles() {
        app.beforeMatched(securityController.authenticate()); // check if there is a valid token in the header
        app.beforeMatched(securityController.authorize()); // check if the user has the required role
        return appConfig;
    } */

    public static void configuration(JavalinConfig config) {
        config.showJavalinBanner = false;
        config.bundledPlugins.enableRouteOverview("/routes");
        config.router.contextPath = "/api/candidateMatch";
        config.http.defaultContentType = "application/json";
        config.router.apiBuilder(routes.getRoutes());
    }

    public Javalin startServer(int port) {
        if (this.app != null) {
            logger.warn("Server is already running on port {}", this.app.port());
            return this.app;
        }

        this.app = Javalin.create(ApplicationConfig::configuration);

        // log requests
        this.app.before(ctx -> { // en before-handler som kører før hver request-handler.
            logger.info("REQUEST | Method: {} | Path: {} | Body: {}",
                    ctx.method(), ctx.path(), ctx.body());
        });

        // log responses
        this.app.after(ctx -> {
            String result = ctx.result();
            logger.info("RESPONSE | Status: {} | Path: {} | Result: {}",
                    ctx.status(), ctx.path(), result != null ? result : "empty");
        });

        // CORS
        this.app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            ctx.header("Access-Control-Allow-Credentials", "true");
        });
        this.app.options("/*", ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            ctx.header("Access-Control-Allow-Credentials", "true");
        });


        // exception handling
        // logger IllegalStateException fejlen (inkl. stacktrace pga. , e) og sender HTTP 400 samt en JSON-body
        this.app.exception(IllegalStateException.class, (e, ctx) -> {
            logger.error("IllegalStateException | Path: {} | Message: {}", ctx.path(), e.getMessage(), e);
            ctx.status(400)
                    .json(Map.of(
                            "error", "Invalid input",
                            "message", e.getMessage()
                    ));
        });

        // En generel fallback-exception handler: logger og returnerer 500 + JSON med error og message
        this.app.exception(Exception.class, (e, ctx) -> {
            logger.error("Exception | Path: {} | Message: {}", ctx.path(), e.getMessage(), e);
            ctx.status(500)
                    .json(Map.of(
                            "error", "Something went wrong",
                            "message", e.getMessage()
                    ));
        });

       // this.checkSecurityRoles();

        this.app.start(port); // starter Javalin på den givne port (binder socket og starter worker-tråde).
        return app;

    }

    public Javalin getApp() {
        return app;
    }

    public static void stopServer(Javalin app) {
        if (app != null) {
            try {
                app.stop();
                System.out.println("Server stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        appConfig = null;
    }
}