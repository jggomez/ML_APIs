package co.devhack.machinelearningapis.fragments.Presenter;

import android.graphics.Bitmap;

/**
 * Created by jggomez on 04-Nov-16.
 */

public interface ICloudVisionPresenter {

    void callCloudVision(Bitmap imagen, final CloudVisionPresenter.TipoEnum tipo);

}
