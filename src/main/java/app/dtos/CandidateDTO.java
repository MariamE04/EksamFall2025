package app.dtos;

import jakarta.persistence.Column;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDTO {
    private int id;
    private String name;
    private String phone;
    private String  educationBackground;
    private List<Integer> candidateSkills;
}
