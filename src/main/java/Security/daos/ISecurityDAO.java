package Security.daos;

import Security.entities.Role;
import Security.entities.User;
import Security.exceptions.ValidationException;
import jakarta.persistence.EntityNotFoundException;


public interface ISecurityDAO {
    User getVerifiedUser(String username, String password) throws ValidationException; // used for login
    User createUser(String username, String password); // used for register
    Role createRole(String role);
    User addUserRole(String username, String role) throws EntityNotFoundException;
}