package Security.rest;

import Security.enums.Role;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class SecurtiyRoutes {
    ISecurityController securityController = new SecurityController();
    private static ObjectMapper jsonMapper = new Utils().getObjectMapper();

    public EndpointGroup getOpenRoutes() {
        return () -> {
            post("/login", securityController.login());
            post("/register", securityController.register());
            get("/healthcheck", securityController::healthCheck, Role.ANYONE);
            get("/test", ctx->ctx.json(jsonMapper.createObjectNode().put("msg",  "Hello from Open Deployment")), Role.ANYONE);

        };
    }

    public EndpointGroup getSecuredRoutes(){
        return ()->{
                get("/user_demo",(ctx)->ctx.json(jsonMapper.createObjectNode().put("msg",  "Hello from USER Protected")),Role.USER);
                get("/admin_demo",(ctx)->ctx.json(jsonMapper.createObjectNode().put("msg",  "Hello from ADMIN Protected")),Role.ADMIN);
        };
    }
}
