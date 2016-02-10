package net.mediavrog.irr;

import android.util.Log;
import android.view.View;

/**
 * Default implementation of a {@link IrrLayout.OnUserActionListener}.
 * <p>
 * created by maik_vlcek
 */
public class DefaultOnToggleVisibilityListener implements IrrLayout.OnToggleVisibilityListener {
    public static final String TAG = DefaultOnToggleVisibilityListener.class.getSimpleName();

    @Override
    public void onShow(IrrLayout irr) {
        Log.d(TAG, "Show rating request");
        irr.setVisibility(View.VISIBLE);
    }

    @Override
    public void onHide(IrrLayout irr) {
        Log.d(TAG, "Hide rating request");
        irr.setVisibility(View.GONE);
    }
}