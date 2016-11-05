package co.devhack.machinelearningapis.fragments;

import java.util.List;

import co.devhack.machinelearningapis.models.FaceSentiment;
import co.devhack.machinelearningapis.models.LabelDetection;

/**
 * Created by jggomez on 04-Nov-16.
 */

public interface ILoadImageView {

    void mostrarProgress();

    void ocultarProgress();

    void getEtiquetas();

    void getCaras();

    void getCarasSentimientos();

    void addImagen();

    void mostrarResultados(List<LabelDetection> lstLabelDetection);

    void mostrarResultados(String result);

    void mostrarResultadosFaceSent(List<FaceSentiment> lstFaceSentiment);

    void textToSpeech(String texto);

}
