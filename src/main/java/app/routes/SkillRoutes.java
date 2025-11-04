package app.routes;

import app.controllers.SkillController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.put;

public class SkillRoutes {
    public EndpointGroup getRoutes() {
        SkillController skillController = new SkillController();

        return () -> {
            get(skillController::getAllSkills);
            post(skillController::createSkill);
            path("/{id}", () -> {
                get(skillController::getSkillById);
                put(skillController::updateSkill);
                delete(skillController::deleteSkill);
            });
        };
    }
}
