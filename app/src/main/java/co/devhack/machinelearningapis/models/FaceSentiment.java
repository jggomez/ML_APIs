package co.devhack.machinelearningapis.models;

/**
 * Created by jggomez on 04-Nov-16.
 */

public class FaceSentiment {

    private String probabilidadAlegria;
    private String probabilidadTristeza;
    private String probabilidadEnfadado;

    public String getProbabilidadAlegria() {
        return probabilidadAlegria;
    }

    public void setProbabilidadAlegria(String probabilidadAlegria) {
        this.probabilidadAlegria = probabilidadAlegria;
    }

    public String getProbabilidadTristeza() {
        return probabilidadTristeza;
    }

    public void setProbabilidadTristeza(String probabilidadTristeza) {
        this.probabilidadTristeza = probabilidadTristeza;
    }

    public String getProbabilidadEnfadado() {
        return probabilidadEnfadado;
    }

    public void setProbabilidadEnfadado(String probabilidadEnfadado) {
        this.probabilidadEnfadado = probabilidadEnfadado;
    }
}
