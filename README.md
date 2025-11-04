# Candidate Skills API

## Projekt Status
Dette projekt er et REST API til at håndtere kandidater og deres færdigheder, inklusiv berigede markedsdata fra et eksternt Skill Stats API.

**Status på User Stories:**
- **US-1 til US-4**: Implementeret (CRUD for Candidate og Skill)
- **US-5**: igang med den. kan hente fra ekstern API, men mangler at for den til at køre rigtigt og .
- **US-6 og US-7**: Ikke implementeret (nået ikke)

## Design Beslutninger

### 1. Relation mellem Candidate og Skill
- Kandidater og skills har en **Many-to-Many relation**, implementeret via mellem-tabellen `CandidateSkill`.
- Denne relation er **bi-directional**:
    - `Candidate` har `Set<CandidateSkill> candidateSkills`
    - `Skill` har `Set<CandidateSkill> candidateSkills`
- Dette tillader nem adgang til både hvilke skills en kandidat har og hvilke kandidater der har en given skill.

### 2. JPA Settings
- `fetch = FetchType.LAZY`:
    - For at undgå at hente hele skill-listen automatisk hver gang en kandidat hentes.
    - Forbedrer performance og reducerer risiko for store unødvendige queries.
- `cascade = {CascadeType.PERSIST, CascadeType.MERGE}`:
    - Sikrer at nye skills kan tilføjes til en kandidat uden at skulle explicit persistere skill-objekterne først.
    - Fjernede `REMOVE` cascade for at undgå utilsigtet sletning af skills, der bruges af andre kandidater.

### 3. DTO og Low Coupling
- `CandidateDTO` indeholder **kun primitive data og skill IDs**, ikke hele skill-objekter.
- `SkillStatsDTO` bruges til at hente markedsdata fra ekstern API uden at blande det med entity-objekter.
- Dette reducerer coupling mellem database-entities og eksterne services.

### 4. Skill Stats API Integration
- Eksternt API: `https://apiprovider.cphbusinessapps.dk/api/v1/skills/stats?slugs=slug1,slug2,...`
- Metode `SkillStatsService.skillsWithStats`:
    - Modtager en liste af `SkillStatsDTO` med `slug` sat.
    - Henter data fra API og beriger `popularityScore` og `averageSalary`.
    - Returnerer en liste af `SkillStatsDTO` til controlleren, som kan inkluderes i response.

### 5. Controller
- `CandidateController.getCandidateById`:
    - Henter kandidat fra DB.
    - Mapper kandidatens `CandidateSkill` til `SkillStatsDTO` (kun slug).
    - Beriger skills via `SkillStatsService`.
    - Returnerer JSON med kandidatdata + berigede skills.
