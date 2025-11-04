package app.routes;

import Security.enums.Role;
import app.controllers.CandidateController;
import app.entities.Candidate;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CandidateRoutes {
   CandidateController candidateController = new CandidateController();

   public EndpointGroup getRoutes() {
        return () -> {
            get(candidateController::getAllCandidates);
            post(candidateController::createCandidate);
            path("/{id}", () -> {
                get(candidateController::getCandidateById);
                put(candidateController::updateCandidate);
                delete(candidateController::deleteCandidate);
            });
            path("{candidateId}/skills/{skillId}", () -> put(candidateController::addSkillToCandidate));
        };
    }
}