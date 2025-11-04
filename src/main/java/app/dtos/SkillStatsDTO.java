package app.dtos;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class SkillStatsDTO {
    private String slug;
    private int popularityScore;
    private int averageSalary;

}
