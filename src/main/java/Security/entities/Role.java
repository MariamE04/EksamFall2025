package Security.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString

@Entity
@Table(name="roles")
public class Role {
    @Id
    @Column(name = "rolename", nullable = false)
    private String rolename;

    @ManyToMany(mappedBy = "roles")
    Set<User> users = new HashSet<>();

    public Role() {
    }

    public  Role(String rolename){
        this.rolename = rolename;
    }

    public String getRolename() {
        return rolename;
    }
}
