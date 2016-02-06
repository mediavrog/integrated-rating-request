package net.mediavrog.samples.irr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import net.mediavrog.irr.DefaultOnUserActionListener;
import net.mediavrog.irr.DefaultRuleEngine;
import net.mediavrog.irr.IntegratedRatingRequestLayout;
import net.mediavrog.ruli.Rule;
import net.mediavrog.ruli.RuleEngine;
import net.mediavrog.ruli.RuleSet;

import java.util.ArrayList;
import java.util.Calendar;

public class CustomRuleEngineActivity extends AppCompatActivity {
    public static final String TAG = CustomRuleEngineActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // simulate an app start; this should really go into a custom Application#onStart
        DefaultRuleEngine.trackAppStart(this);

        setContentView(R.layout.activity_custom_rule_engine);

        // AND
        RuleSet evenDayRule = new RuleSet.Builder()
                .addRule(new Rule() {
                    @Override
                    public boolean evaluate() {
                        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
                        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
                    }
                }).build();

        // OR
        ArrayList<Rule> rules = new ArrayList<>();
        rules.add(evenDayRule);
        RuleEngine irrRuleEngine = new RuleEngine(rules);

        // you can decide the timing to start rule evaluation or leave it up to irr
        // beware that certain types of rule might perform a long running operation to evaluate,
        // so call evaluate on a background thread to avoid blocking the UI
        boolean shouldNudge = irrRuleEngine.evaluate();
        Log.d(TAG, "Rule evaluation result? " + shouldNudge);

        IntegratedRatingRequestLayout irr = (IntegratedRatingRequestLayout) findViewById(R.id.irr_layout);
        irr.setRuleEngine(irrRuleEngine);
        irr.setOnUserDecisionListener(new IntegratedRatingRequestLayout.OnUserDecisionListener() {
            @Override
            public void onAccept(Context ctx, IntegratedRatingRequestLayout.State s) {
                Toast.makeText(ctx, "Accepted sth in state " + s.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDismiss(Context ctx, IntegratedRatingRequestLayout.State s) {
                Toast.makeText(ctx, "Dismissed sth in state " + s.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        Log.d(TAG, "Got a result " + resultCode + " for request code " + requestCode + " / " + data);

        if (requestCode == DefaultOnUserActionListener.RATE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Thanks for your rating", Toast.LENGTH_LONG).show();
                if (data != null) Log.d(TAG, data.toUri(Intent.URI_INTENT_SCHEME));
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show();
            }
        }
    }

}
