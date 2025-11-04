package app.entities;

import app.enums.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "candidateSkills")
@Builder

@Entity
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String slug;

    @Enumerated(EnumType.STRING)
    private Category category;
    private String description;

    @OneToMany(mappedBy = "skill",cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<CandidateSkill> candidateSkills = new HashSet<>();;
}
