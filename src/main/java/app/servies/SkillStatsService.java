package app.servies;

import app.dtos.SkillDTO;
import app.dtos.SkillStatsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class SkillStatsService {

    private static final String API_URL = "https://apiprovider.cphbusinessapps.dk/api/v1/skills/stats?slugs=";

    public static List<SkillStatsDTO> skillsWithStats(List<SkillStatsDTO> skills) {
        if (skills.isEmpty()) return Collections.emptyList();

        try {
            // Konfigurer ObjectMapper til ZonedDateTime
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Lav comma-separeret liste af slugs
            String slugs = skills.stream()
                    .map(SkillStatsDTO::getSlug)
                    .collect(Collectors.joining(","));

            // Bygger selve request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(API_URL + slugs))
                    .GET()
                    .build();

            // Sender request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Skill Stats API request failed: " + response.statusCode());
                return skills; // returnér original skills
            }

            // Parser JSON
            SkillStatsResponse statsResponse = objectMapper.readValue(response.body(), SkillStatsResponse.class);

            // Map API data til slugs
            Map<String, SkillStatsDTO> statsMap = statsResponse.getData().stream()
                    .collect(Collectors.toMap(SkillStatsDTO::getSlug, s -> s));

            // Berig skills
            for (SkillStatsDTO s : skills) {
                if (statsMap.containsKey(s.getSlug())) {
                    SkillStatsDTO stats = statsMap.get(s.getSlug());
                    s.setPopularityScore(stats.getPopularityScore());
                    s.setAverageSalary(stats.getAverageSalary());
                }
            }

            return skills;

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return skills; // fallback: returnér uændrede skills
    }

    // Hjælpeklasse til at matche JSON-strukturen
    private static class SkillStatsResponse {
        private List<SkillStatsDTO> data;

        public List<SkillStatsDTO> getData() { return data; }
        public void setData(List<SkillStatsDTO> data) { this.data = data; }
    }
}
