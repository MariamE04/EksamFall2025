package app.routes;

import app.controllers.CandidateController;
import app.entities.Candidate;
import app.mappers.CandidateMapper;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes {

    private CandidateRoutes candidateRoutes = new CandidateRoutes();
    private SkillRoutes skillRoutes = new SkillRoutes();

    public EndpointGroup getRoutes() {
        return () -> {
            // root endpoint
            get("/", ctx -> ctx.result("Welcome Candidate Matcher to API!"));

            path("/candidates", candidateRoutes.getRoutes());
            path("/skills", skillRoutes.getRoutes());

        };
    }
}
