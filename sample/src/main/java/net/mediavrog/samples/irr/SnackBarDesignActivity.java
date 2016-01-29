package net.mediavrog.samples.irr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.mediavrog.irr.DefaultOnToggleVisibilityListener;
import net.mediavrog.irr.DefaultRuleEngine;
import net.mediavrog.irr.IntegratedRatingRequestLayout;

public class SnackBarDesignActivity extends DefaultRuleEngineActivity {
    public static final String TAG = SnackBarDesignActivity.class.getSimpleName();

    /**
     * @return
     */
    protected int getLayoutResId() {
        return R.layout.activity_snackbar_design;
    }

    protected void initialize() {
        engine = (DefaultRuleEngine) irr.getRuleEngine();
        engine.setListener(new DefaultRuleEngine.DefaultOnUserDecisionListener() {
            @Override
            public void onAccept(Context ctx, IntegratedRatingRequestLayout.State s) {
                super.onAccept(ctx, s);
                evaluateRules(true);
            }

            @Override
            public void onDismiss(Context ctx, IntegratedRatingRequestLayout.State s) {
                super.onDismiss(ctx, s);
                evaluateRules(true);
            }
        });

        irr.setOnToggleVisibilityListener(new DefaultOnToggleVisibilityListener() {

            @Override
            public void onShow(IntegratedRatingRequestLayout irr) {
                super.onShow(irr);
                // TODO: do some fance show animation
            }
        });
    }
}
