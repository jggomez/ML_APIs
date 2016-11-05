package co.devhack.machinelearningapis.fragments.Presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import co.devhack.machinelearningapis.R;
import co.devhack.machinelearningapis.fragments.ILoadImageView;
import co.devhack.machinelearningapis.models.FaceSentiment;
import co.devhack.machinelearningapis.models.LabelDetection;

/**
 * Created by jggomez on 04-Nov-16.
 */

public class CloudVisionPresenter implements ICloudVisionPresenter {

    public enum TipoEnum {
        LABELS, FACES, SENTIMENT, OCR
    }

    private Context context;
    private ILoadImageView view;
    private final String TAG = CloudVisionPresenter.this.getClass().getSimpleName();

    public CloudVisionPresenter(Context context, ILoadImageView view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void callCloudVision(final Bitmap imagen, final TipoEnum tipo) {

        view.mostrarProgress();

        new AsyncTask<Object, Void, BatchAnnotateImagesResponse>() {

            @Override
            protected BatchAnnotateImagesResponse doInBackground(Object... params) {

                try {

                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(
                            new VisionRequestInitializer(context.getResources().getString(R.string.vision_api_key)));

                    final Vision vision = builder.build();

                    final BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Adiciona la imagen
                        Image base64EncodedImage = new Image();
                        // Convierte la imagen a JPEG
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        imagen.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();
                        // JPEG codificado a base64
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{

                            Feature labelDetection = new Feature();
                            if (tipo == TipoEnum.FACES || tipo == TipoEnum.SENTIMENT) {
                                labelDetection.setType("FACE_DETECTION");
                            }

                            if (tipo == TipoEnum.LABELS) {
                                labelDetection.setType("LABEL_DETECTION");
                            }

                            if (tipo == TipoEnum.OCR) {
                                labelDetection.setType("TEXT_DETECTION");
                            }

                            labelDetection.setMaxResults(10);
                            add(labelDetection);

                        }});

                        add(annotateImageRequest);

                    }});

                    Vision.Images.Annotate annotateRequest = null;

                    annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);

                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse response = annotateRequest.execute();

                    return response;

                } catch (Exception e) {
                    Log.e(TAG, "failed to make API request because " + e.toString());
                }

                return null;

            }

            @Override
            protected void onPostExecute(BatchAnnotateImagesResponse response) {
                super.onPostExecute(response);

                view.ocultarProgress();

                List<EntityAnnotation> entityAnnotations;
                List<FaceAnnotation> faceAnnotations;

                if (tipo == TipoEnum.SENTIMENT) {

                    faceAnnotations = response.getResponses().get(0).getFaceAnnotations();

                    if (faceAnnotations != null && faceAnnotations.size() > 0) {
                        FaceSentiment faceSentiment;
                        List<FaceSentiment> lstFaceSent = new ArrayList<>();

                        for (FaceAnnotation faceAnnotation : faceAnnotations) {
                            faceSentiment = new FaceSentiment();
                            faceSentiment.setProbabilidadAlegria(faceAnnotation.getJoyLikelihood().toString());
                            faceSentiment.setProbabilidadEnfadado(faceAnnotation.getAngerLikelihood());
                            faceSentiment.setProbabilidadTristeza(faceAnnotation.getSorrowLikelihood());
                            lstFaceSent.add(faceSentiment);
                        }

                        view.mostrarResultadosFaceSent(lstFaceSent);
                    } else {
                        view.mostrarResultados("Número de caras => 0");
                    }
                }

                if (tipo == TipoEnum.FACES) {

                    faceAnnotations = response.getResponses().get(0).getFaceAnnotations();

                    if (faceAnnotations != null && faceAnnotations.size() > 0) {
                        view.mostrarResultados(String.format("Número de caras => %s", faceAnnotations.size()));
                    } else {
                        view.mostrarResultados("Número de caras => 0");
                    }
                }

                if (tipo == TipoEnum.LABELS) {

                    List<LabelDetection> lstLabels = new ArrayList<>();
                    entityAnnotations = response.getResponses().get(0).getLabelAnnotations();

                    if (entityAnnotations != null) {
                        LabelDetection labelDetection;

                        for (EntityAnnotation entityAnnotation : entityAnnotations) {
                            labelDetection = new LabelDetection();
                            labelDetection.setScore(entityAnnotation.getScore().toString());
                            labelDetection.setDescripcion(entityAnnotation.getDescription());
                            lstLabels.add(labelDetection);
                        }
                        
                    }

                    view.mostrarResultados(lstLabels);
                }

                if (tipo == TipoEnum.OCR) {

                    entityAnnotations = response.getResponses().get(0).getTextAnnotations();

                    if (entityAnnotations != null && entityAnnotations.size() > 0) {
                        EntityAnnotation entityAnnotation = entityAnnotations.get(0);
                        view.mostrarResultados(entityAnnotation.getDescription());
                        view.textToSpeech(entityAnnotation.getDescription());

                        // Mostrar todos en depuración
                        for (EntityAnnotation entityAnnotationtmp : entityAnnotations) {
                            Log.d(TAG, "Texto => " + entityAnnotationtmp.getDescription());
                        }

                    }
                }

                Log.i(TAG, response.toString());

            }

        }.execute();

    }
}
