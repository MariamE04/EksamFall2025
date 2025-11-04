package app.dtos;

import app.enums.Category;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class SkillDTO {
    private int id;
    private String name;
    private Category category;
    private String description;
    private List<Integer> candidateSkills;

}
