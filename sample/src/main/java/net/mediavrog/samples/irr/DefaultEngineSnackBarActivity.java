package net.mediavrog.samples.irr;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import net.mediavrog.irr.DefaultOnToggleVisibilityListener;
import net.mediavrog.irr.DefaultRuleEngine;
import net.mediavrog.irr.IrrLayout;

public class DefaultEngineSnackBarActivity extends DefaultEngineActivity {
    public static final String TAG = DefaultEngineSnackBarActivity.class.getSimpleName();

    /**
     * @return
     */
    protected int getLayoutResId() {
        return R.layout.activity_default_engine_snackbar;
    }

    protected void initialize() {
        irr = (IrrLayout) findViewById(R.id.irr_layout);
        engine = (DefaultRuleEngine) irr.getRuleEngine();
        engine.setListener(new DefaultRuleEngine.DefaultOnUserDecisionListener() {
            @Override
            public void onAccept(Context ctx, IrrLayout.State s) {
                super.onAccept(ctx, s);
                evaluateRules(true);
            }

            @Override
            public void onDismiss(Context ctx, IrrLayout.State s) {
                super.onDismiss(ctx, s);
                evaluateRules(true);
            }
        });

        irr.setOnToggleVisibilityListener(new DefaultOnToggleVisibilityListener() {

            @Override
            public void onShow(final IrrLayout irr) {
                if (irr.getVisibility() != View.VISIBLE) {
                    TranslateAnimation anim = new TranslateAnimation(0, 0, -300, 0);
                    anim.setDuration(200);
                    anim.setFillAfter(true);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            irr.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    irr.startAnimation(anim);
                }
            }

            @Override
            public void onHide(final IrrLayout irr) {
                if (irr.getVisibility() != View.GONE) {
                    TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -300);
                    anim.setDuration(150);
                    anim.setFillAfter(true);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            irr.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    irr.startAnimation(anim);
                }
            }
        });
    }
}
