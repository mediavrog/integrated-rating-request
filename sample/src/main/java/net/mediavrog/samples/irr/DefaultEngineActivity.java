package net.mediavrog.samples.irr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.mediavrog.irr.DefaultRuleEngine;
import net.mediavrog.irr.IrrLayout;

public class DefaultEngineActivity extends AppCompatActivity {
    public static final String TAG = DefaultEngineActivity.class.getSimpleName();

    protected TextView dump;
    protected IrrLayout irr;
    protected DefaultRuleEngine engine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // simulate an app start; this should really go into a custom Application#onStart
        DefaultRuleEngine.trackAppStart(this);

        // now we let the layout handle all the default setup using attributes
        setContentView(getLayoutResId());

        // status textfield for debug info
        dump = (TextView) findViewById(R.id.dump);

        initialize();
        setupControls();
        evaluateRules(true);
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

    /**
     * @return
     */
    protected int getLayoutResId() {
        return R.layout.activity_default_engine;
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
    }

    /**
     * Those controls allow us to manipulate the data backing the rule engine.
     */
    void setupControls() {
        findViewById(R.id.incrAppStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DefaultRuleEngine.trackAppStart(v.getContext());
                evaluateRules();
            }
        });

        findViewById(R.id.incrDaysUsed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences p = DefaultRuleEngine.getPreferences(v.getContext());
                p
                        .edit()
                        .putInt(DefaultRuleEngine.PREF_KEY_DAYS_USED, p.getInt(DefaultRuleEngine.PREF_KEY_DAYS_USED, 0) + 1)
                        .apply();

                evaluateRules();
            }
        });

        findViewById(R.id.incrDismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DefaultRuleEngine.trackDismissal(v.getContext());
                evaluateRules();
            }
        });

        findViewById(R.id.resetDismisAt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences p = DefaultRuleEngine.getPreferences(v.getContext());
                p
                        .edit()
                        .putString(DefaultRuleEngine.PREF_KEY_LAST_DISMISSED_AT, "")
                        .apply();

                evaluateRules();
            }
        });

        findViewById(R.id.toggleRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences p = DefaultRuleEngine.getPreferences(v.getContext());
                p
                        .edit()
                        .putBoolean(DefaultRuleEngine.PREF_KEY_DID_RATE, !p.getBoolean(DefaultRuleEngine.PREF_KEY_DID_RATE, false))
                        .apply();

                evaluateRules();
            }
        });

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DefaultRuleEngine.reset(v.getContext());
                evaluateRules();
            }
        });
    }

    void evaluateRules() {
        evaluateRules(false);
    }

    void evaluateRules(boolean onlyDump) {
        if (!onlyDump) engine.evaluate();

        dump.setText(engine.toString(!onlyDump));
    }
}
