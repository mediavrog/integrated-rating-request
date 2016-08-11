package net.mediavrog.samples.irr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.mediavrog.irr.DefaultOnUserActionListener;
import net.mediavrog.irr.IrrLayout;
import net.mediavrog.ruli.Rule;
import net.mediavrog.ruli.RuleEngine;
import net.mediavrog.ruli.RuleSet;

import java.util.ArrayList;
import java.util.Calendar;

public class CustomEngineActivity extends AppCompatActivity {
    public static final String TAG = CustomEngineActivity.class.getSimpleName();

    private TextView dump;
    private IrrLayout irr;
    private RuleEngine engine;
    private Calendar cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_engine);

        // status text field for debug info
        dump = (TextView) findViewById(R.id.dump);

        // keep a calendar instance to effectively modify the date
        cal = Calendar.getInstance();

        // a custom rule
        RuleSet evenDayRule = new RuleSet.Builder()
                .addRule(new Rule() {
                    @Override
                    public boolean evaluate() {
                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
                    }

                    @Override
                    public String toString(boolean evaluate) {
                        return "Is today Saturday or Sunday?" + (evaluate ? " => " + this.evaluate() : "");
                    }
                }).build();

        // .. put into a custom rule engine
        ArrayList<Rule> rules = new ArrayList<>();
        rules.add(evenDayRule);
        engine = new RuleEngine(rules);

        // Developer can decide the timing to start rule evaluation or leave it up to IRR.
        // Depending on your rule implementation, use Threads to offload long running operations
        // to the background to avoid blocking the UI
        engine.evaluate();

        irr = (IrrLayout) findViewById(R.id.irr_layout);
        irr.setRuleEngine(engine); // layout attr irr:useCustomRuleEngine must be set to true!
        irr.setOnUserDecisionListener(new IrrLayout.OnUserDecisionListener() {
            @Override
            public void onAccept(Context ctx, IrrLayout.State s) {
                Toast.makeText(ctx, "Accepted in state " + s.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDismiss(Context ctx, IrrLayout.State s) {
                Toast.makeText(ctx, "Dismissed in state " + s.toString(), Toast.LENGTH_LONG).show();
            }
        });

        setupControls();
        evaluateRules(true);
    }

    /**
     * Those controls allow us to manipulate the data backing the rule engine.
     */
    void setupControls() {
        findViewById(R.id.setMonday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                evaluateRules();
            }
        });

        findViewById(R.id.setSaturday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                evaluateRules();
            }
        });

        findViewById(R.id.setSunday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                evaluateRules();
            }
        });

        findViewById(R.id.setToday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
                evaluateRules();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                Toast.makeText(this, R.string.help, Toast.LENGTH_LONG).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    void evaluateRules() {
        evaluateRules(false);
    }

    void evaluateRules(boolean onlyDump) {
        if (!onlyDump) engine.evaluate();

        dump.setText(engine.toString(!onlyDump));
    }

}
