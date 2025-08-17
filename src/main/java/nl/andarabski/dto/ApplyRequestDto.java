package nl.andarabski.dto;

public class ApplyRequestDto {

    private String motivation;
    private Long userId;
    private Long vacancyId;

    // Constructors
    public ApplyRequestDto() {
    }

    public ApplyRequestDto(String motivation, Long userId, Long vacancyId) {
        this.motivation = motivation;
        this.userId = userId;
        this.vacancyId = vacancyId;
    }

    // Getters & Setters
    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVacancyId() {
        return vacancyId;
    }

    public void setVacancyId(Long vacancyId) {
        this.vacancyId = vacancyId;
    }

    // Optional: toString
    @Override
    public String toString() {
        return "ApplyRequestDto{" +
                "motivation='" + motivation + '\'' +
                ", userId=" + userId +
                ", vacancyId=" + vacancyId +
                '}';
    }
}
