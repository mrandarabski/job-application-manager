package nl.andarabski.dto;

public class MotivationRequestDto {

    private String motivation;

    public MotivationRequestDto() {
    }

    public MotivationRequestDto(String motivation) {
        this.motivation = motivation;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }
}
