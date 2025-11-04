package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "candidateSkills") // undgå loop
@Builder

@Entity
@Table(
        name = "candidate",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "phone")
        }
)
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    @Column(name = "phone", nullable = false)
    private String phone;
    private String  educationBackground;


    @OneToMany(mappedBy = "candidate", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<CandidateSkill> candidateSkills = new HashSet<>();

    public void addSkill(Skill skill) {
        if (skill == null) return;
        boolean alreadyHas = candidateSkills.stream()
                .anyMatch(cs -> cs.getSkill().equals(skill));
        if (alreadyHas) return;

        CandidateSkill candidateSkill = new CandidateSkill(this, skill);
        candidateSkills.add(candidateSkill);
        skill.getCandidateSkills().add(candidateSkill);
    }

    public void removeSkill(Skill skill) {
        if (skill == null) return;
        candidateSkills.removeIf(cs -> cs.getSkill().equals(skill));
        skill.getCandidateSkills().removeIf(cs -> cs.getCandidate().equals(this));
    }

}
