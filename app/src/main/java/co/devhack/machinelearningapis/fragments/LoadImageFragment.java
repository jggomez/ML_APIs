package co.devhack.machinelearningapis.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.devhack.machinelearningapis.R;
import co.devhack.machinelearningapis.fragments.Presenter.CloudVisionPresenter;
import co.devhack.machinelearningapis.fragments.Presenter.ICloudVisionPresenter;
import co.devhack.machinelearningapis.models.FaceSentiment;
import co.devhack.machinelearningapis.models.LabelDetection;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoadImageFragment.OnLoadImageFragListener} interface
 * to handle interaction events.
 * Use the {@link LoadImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoadImageFragment extends Fragment implements ILoadImageView {

    private OnLoadImageFragListener mListener;
    private CharSequence[] opciones;
    private static final int SELECT_PICTURE = 200;
    private ICloudVisionPresenter cloudVisionPresenter;
    private ProgressDialog progressDialog;
    private TextToSpeech textToSpeech;


    @Bind(R.id.imgvw)
    ImageView imgvw;

    public LoadImageFragment() {
        // Required empty public constructor
    }


    public static LoadImageFragment newInstance() {
        LoadImageFragment fragment = new LoadImageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_load_image, container, false);
        ButterKnife.bind(this, view);

        opciones = new CharSequence[]{getResources().getString(R.string.seleccionar_imagen),
                getResources().getString(R.string.tomar_foto)};

        Picasso.with(getContext()).load(R.drawable.icon_person).into(imgvw);

        cloudVisionPresenter = new CloudVisionPresenter(getContext(), this);

        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR){
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        return view;
    }

    @OnClick(R.id.fbAddImage)
    public void addImagen() {

        new MaterialDialog.Builder(getActivity())
                .title(getResources().getString(R.string.origen_imagen))
                .items(opciones)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        if (opciones[position].equals(getResources().getString(R.string.seleccionar_imagen))) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,
                                    getResources().getString(R.string.seleccionar_imagen)), SELECT_PICTURE);
                        }
                    }
                })
                .show();

    }

    @Override
    public void mostrarProgress() {
        progressDialog = ProgressDialog.show(getContext(), "", "Cargando...");
    }

    @Override
    public void ocultarProgress() {
        progressDialog.dismiss();
    }

    @OnClick(R.id.btnEtiquetas)
    public void getEtiquetas() {
        cloudVisionPresenter.callCloudVision(((BitmapDrawable) imgvw.getDrawable()).getBitmap(),
                CloudVisionPresenter.TipoEnum.LABELS);
    }

    @OnClick(R.id.btnCaras)
    public void getCaras() {
        cloudVisionPresenter.callCloudVision(((BitmapDrawable) imgvw.getDrawable()).getBitmap(),
                CloudVisionPresenter.TipoEnum.FACES);
    }

    @OnClick(R.id.btnAnalisisSent)
    public void getCarasSentimientos() {
        cloudVisionPresenter.callCloudVision(((BitmapDrawable) imgvw.getDrawable()).getBitmap(),
                CloudVisionPresenter.TipoEnum.SENTIMENT);
    }

    @OnClick(R.id.btnOCR)
    public void getOCR() {
        cloudVisionPresenter.callCloudVision(((BitmapDrawable) imgvw.getDrawable()).getBitmap(),
                CloudVisionPresenter.TipoEnum.OCR);
    }

    public void mostrarResultados(List<LabelDetection> lstLabelDetection) {

        if (lstLabelDetection != null && lstLabelDetection.size() != 0) {

            StringBuilder result = new StringBuilder();

            for (LabelDetection labelDetection : lstLabelDetection) {
                result.append(String.format("Score: %s <--> Desc: %s \n",
                        labelDetection.getScore(), labelDetection.getDescripcion()));
            }

            new MaterialDialog.Builder(getActivity())
                    .title(getResources().getString(R.string.objetos_imagen))
                    .content(result.toString()).show();

        }

    }


    @Override
    public void mostrarResultados(String result) {
        new MaterialDialog.Builder(getActivity())
                .title(getResources().getString(R.string.objetos_imagen))
                .content(result).show();
    }

    @Override
    public void mostrarResultadosFaceSent(List<FaceSentiment> lstFaceSentiment) {
        if (lstFaceSentiment != null && lstFaceSentiment.size() != 0) {

            StringBuilder result = new StringBuilder();

            int i = 1;

            for (FaceSentiment faceSentiment : lstFaceSentiment) {
                result.append(String.format("Cara %s \n => Feliz: %s, Triste:%s, Enfadado: %s \n",
                        String.valueOf(i),
                        faceSentiment.getProbabilidadAlegria(),
                        faceSentiment.getProbabilidadTristeza(),
                        faceSentiment.getProbabilidadEnfadado()));
                i++;
            }

            new MaterialDialog.Builder(getActivity())
                    .title(getResources().getString(R.string.sentimientos_imagen))
                    .content(result.toString()).show();

        }
    }

    @Override
    public void textToSpeech(String texto) {
        textToSpeech.speak(texto, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SELECT_PICTURE:
                if (resultCode == RESULT_OK) {
                    Uri pathImgPerfil = data.getData();
                    Picasso.with(getActivity()).load(pathImgPerfil).into(imgvw);
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoadImageFragListener) {
            mListener = (OnLoadImageFragListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnLoadImageFragListener {
        // TODO: Update argument type and name

    }
}
