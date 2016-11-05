package co.devhack.machinelearningapis.models;

/**
 * Created by jggomez on 04-Nov-16.
 */

public class LabelDetection {

    private String score;
    private String descripcion;

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
