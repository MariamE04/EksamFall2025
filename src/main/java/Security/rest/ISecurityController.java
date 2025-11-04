package Security.rest;

import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public interface ISecurityController {
    Handler login(); // to get a token after checking username and password
    Handler register(); // to make a new User and get a token
    Handler authenticate(); // to verify that a token was sent with the request and that it is a valid, non-expired token
    Handler authorize();
    String createToken(UserDTO user) throws Exception;
    void healthCheck(@NotNull Context context);
}
