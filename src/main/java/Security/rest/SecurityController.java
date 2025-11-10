package Security.rest;

import Security.daos.ISecurityDAO;
import Security.daos.SecurityDAO;
import Security.entities.User;
import Security.exceptions.ApiException;
import Security.exceptions.ValidationException;
import app.config.HibernateConfig;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.TokenSecurity;
import dk.bugelhartmann.TokenVerificationException;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.*;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;


public class SecurityController implements ISecurityController{
    ISecurityDAO securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
    ObjectMapper objectMapper = new Utils().getObjectMapper();
    TokenSecurity tokenSecurity = new TokenSecurity();

    @Override
    public Handler login() {
        return (Context ctx) -> {
            User user = ctx.bodyAsClass(User.class);
            try {
                    User verifedUser = securityDAO.getVerifiedUser(user.getUsername(), user.getPassword());
                Set<String> stringRoles = verifedUser.getRoles()
                        .stream().map(role -> role.getRolename())
                        .collect(Collectors.toSet());
                UserDTO userDTO = new UserDTO(verifedUser.getUsername(), stringRoles);
                String token = createToken(userDTO);

                ObjectNode on = objectMapper
                        .createObjectNode()
                        .put("token", token)
                        .put("username", userDTO.getUsername());
                ctx.json(on).status(200);
            } catch (ValidationException ex) {
                ObjectNode on = objectMapper.createObjectNode().put("msg","login failed. Wrong username or password");
                ctx.json(on).status(401);
            }
        };
    }

    @Override
    public Handler register() {
        return (Context ctx) -> {
            try{
                User reqUser = ctx.bodyAsClass(User.class);

                if (reqUser.getUsername() == null || reqUser.getUsername().isBlank()
                        || reqUser.getPassword() == null || reqUser.getPassword().isBlank()){
                    throw new ValidationException("Username and password must be provided");
                }

                // Opret bruger (User konstruktøren hasher password)
                User createdUser = securityDAO.createUser(reqUser.getUsername(), reqUser.getPassword());

                try {
                    securityDAO.createRole("User");
                } catch (Exception ignored) {
                    // Rolles kan allerede eksistere -> ignorer
                }

                createdUser = securityDAO.addUserRole(createdUser.getUsername(), "User");

                Set<String> stringRoles = createdUser.getRoles()
                        .stream()
                        .map(role -> role.getRolename())
                        .collect(Collectors.toSet());

                UserDTO userDTO = new UserDTO(createdUser.getUsername(), stringRoles);
                String token = createToken(userDTO);

                ObjectNode on = objectMapper.createObjectNode()
                        .put("token", token)
                        .put("username", createdUser.getUsername());

                ctx.json(on).status(201);
            } catch (ValidationException ve) {
                ctx.status(400).json(objectMapper.createObjectNode().put("msg", ve.getMessage()));
            } catch (Exception e) {
                // Hvis f.eks. brugernavn allerede findes, vil persist kaste en fejl -> returner 400 med besked
                ctx.status(400).json(objectMapper.createObjectNode().put("msg", "Registration failed: " + e.getMessage()));
            }
        };
    }

    @Override
    public Handler authenticate() {
        return (Context ctx) -> {
            if (ctx.method().toString().equals("OPTIONS")){
                ctx.status(200);
                return;
            }

            Set<String> allowedRoles = ctx.routeRoles().stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
            //  Check if the endpoint is open to all
            if (isOpenEndpoint(allowedRoles)) {
                return;
            }
                // If there is no token we do not allow entry
                UserDTO verifiedTokenUser = validateAndGetUserFromToken(ctx);
                ctx.attribute("user", verifiedTokenUser); // -> ctx.attribute("user") in ApplicationConfig beforeMatched filter
        };
    }

    @Override
    public Handler authorize() {
        return (Context ctx) ->{
            Set<String> allowedRoles = ctx.routeRoles()
                    .stream().map(role -> role.toString().toUpperCase())
                    .collect(Collectors.toSet());
            // 1. Check if the endpoint is open to all (either by not having any roles or having the ANYONE role set
            if (isOpenEndpoint(allowedRoles))
                return;
            // 2. Get user and ensure it is not null
            UserDTO user = ctx.attribute("user");
            if (user == null) {
                throw new ForbiddenResponse("No user was added from the token");
            }
            // 3. See if any role matches
            if (!userHasAllowedRole(user, allowedRoles))
                throw new ForbiddenResponse("User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
        };
    }

    @Override
    public String createToken(UserDTO user) throws Exception {
        try{
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if(System.getenv("DEPLOYED") != null){
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e){
            throw new ApiException(500, "Could not create token");
        }
    }

    // hjælpe metoder:
    private static String getToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null) {
            throw new UnauthorizedResponse("Authorization header is missing"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }

        // If the Authorization Header was malformed, then no entry
        String token = header.split(" ")[1];
        if (token == null) {
            throw new UnauthorizedResponse("Authorization header is malformed"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }
        return token;
    }

    private boolean isOpenEndpoint(Set<String> allowedRoles) {
        // If the endpoint is not protected with any roles:
        if (allowedRoles.isEmpty())
            return true;

        // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
        if (allowedRoles.contains("ANYONE")) {
            return true;
        }
        return false;
    }

    private UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            } else {
                throw new UnauthorizedResponse("Token not valid");
            }
        } catch (ParseException | TokenVerificationException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        }
    }

    private UserDTO validateAndGetUserFromToken(Context ctx) {
        String token = getToken(ctx);

        UserDTO verifiedTokenUser = verifyToken(token);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }
        return verifiedTokenUser;
    }

    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }

    // Health check for the API. Used in deployment
    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }

}