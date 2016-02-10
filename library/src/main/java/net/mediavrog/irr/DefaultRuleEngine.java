package net.mediavrog.irr;

import android.content.Context;
import android.content.SharedPreferences;

import net.mediavrog.irr.IrrLayout.OnUserDecisionListener;
import net.mediavrog.ruli.Rule;
import net.mediavrog.ruli.RuleEngine;
import net.mediavrog.ruli.RuleSet;
import net.mediavrog.ruli.SimpleRule;
import net.mediavrog.ruli.Value;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static net.mediavrog.ruli.SimpleRule.Comparator.EQ;
import static net.mediavrog.ruli.SimpleRule.Comparator.GT_EQ;
import static net.mediavrog.ruli.SimpleRule.Comparator.LT;
import static net.mediavrog.ruli.SimpleRule.Comparator.LT_EQ;

/**
 * Created by maikvlcek on 1/27/16.
 */
public class DefaultRuleEngine extends RuleEngine {
    public static final String TAG = DefaultRuleEngine.class.getSimpleName();

    /**
     * Start nudging the user after this amount of app starts (== limit to engaged users)
     */
    public static final int DEFAULT_APP_START_COUNT = 10;

    /**
     * Start nudging the user after this amount of days (== engaged user in combination with app starts)
     */
    public static final int DEFAULT_DISTINCT_DAYS = 3;

    /**
     * Postpone next nudge by this amount of days.
     */
    public static final int DEFAULT_POSTPONE_DAYS = 6;

    /**
     * Stop nudging after with amount of dismissals. At one point you gotta give up ^^.
     */
    public static final int DEFAULT_MAX_DISMISS_COUNT = 3;

    private static final String PREF_FILE_NAME_SUFFIX = ".irr_default_rule_engine";

    public static final String PREF_KEY_DID_RATE = "didRate";

    public static final String PREF_KEY_APP_STARTS = "appStarts";

    public static final String PREF_KEY_LAST_APP_START = "lastAppStart";

    public static final String PREF_KEY_DAYS_USED = "daysUsedApp";

    public static final String PREF_KEY_DISMISSAL_COUNT = "dismissCount";

    public static final String PREF_KEY_LAST_DISMISSED_AT = "lastDismissedAt";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static class DefaultOnUserDecisionListener implements OnUserDecisionListener {

        @Override
        public void onAccept(Context ctx, IrrLayout.State s) {
            switch (s) {
                case RATE:
                    trackRated(ctx);
                    break;
                case FEEDBACK:
                    trackFeedback(ctx);
                    break;
            }
        }

        @Override
        public void onDismiss(Context ctx, IrrLayout.State s) {
            switch (s) {
                // we don't track the first no thanks as dismissal, only from the last steps
                case RATE:
                case FEEDBACK:
                    trackDismissal(ctx);
                    break;
            }
        }
    }

    private static SharedPreferences sPrefs;

    private DefaultOnUserDecisionListener mListener;
    private Context mContext;

    public static DefaultRuleEngine newInstance(final Context ctx, int appStartCount, int distinctDays, final int postponeDays, int maxDismissCount) {
        PreferenceValue.PreferenceProvider pp = new PreferenceValue.PreferenceProvider() {
            SharedPreferences prefs;

            @Override
            public SharedPreferences getPreferences() {
                if (prefs == null) prefs = DefaultRuleEngine.getPreferences(ctx);
                return prefs;
            }
        };

        RuleSet rule = new RuleSet.Builder()
                .addRule(new SimpleRule<>(PreferenceValue.b(pp, PREF_KEY_DID_RATE), EQ, false))
                .addRule(new SimpleRule<>(PreferenceValue.i(pp, PREF_KEY_APP_STARTS), GT_EQ, appStartCount))
                .addRule(new SimpleRule<>(PreferenceValue.i(pp, PREF_KEY_DAYS_USED), GT_EQ, distinctDays))
                .addRule(new SimpleRule<>(PreferenceValue.i(pp, PREF_KEY_DISMISSAL_COUNT), LT, maxDismissCount))
                .addRule(new SimpleRule<>(PreferenceValue.s(pp, PREF_KEY_LAST_DISMISSED_AT), LT_EQ, new Value<String>() {
                    @Override
                    public String get() {
                        // compare to postpone days before today; current value should be smaller than that
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.DATE, -1 * postponeDays);
                        return new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault()).format(c.getTime());
                    }
                })).build();

        ArrayList<Rule> rules = new ArrayList<>();
        rules.add(rule);

        return new DefaultRuleEngine(ctx, rules);
    }

    public DefaultRuleEngine(Context ctx, List<Rule> rules) {
        super(rules);
        mContext = ctx;
    }

    public void setListener(DefaultOnUserDecisionListener l) {
        mListener = l;
    }

    public DefaultOnUserDecisionListener getListener() {
        if (mListener == null) mListener = new DefaultOnUserDecisionListener();
        return mListener;
    }

    @Override
    public String toString(boolean evaluate) {
        StringBuilder s = new StringBuilder();
        // meta info
        s.append("DefaultRuleEngine").append("\n");

        // dump rules
        s.append(super.toString(evaluate));

        return s.toString();
    }

    public void reset() {
        reset(mContext);
    }

    public static void reset(Context ctx) {
        getPreferences(ctx).edit().clear().apply();
    }

    public void trackAppStart() {
        trackAppStart(mContext);
    }

    public static void trackAppStart(Context ctx) {
        SharedPreferences s = getPreferences(ctx);
        int appStarts = s.getInt(PREF_KEY_APP_STARTS, 0) + 1;
        int daysUsed = s.getInt(PREF_KEY_DAYS_USED, 1);

        String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault()).format(new Date());
        String lastAppStart = s.getString(PREF_KEY_LAST_APP_START, today);
        if (!lastAppStart.equals(today)) daysUsed++;

        s.edit()
                .putInt(PREF_KEY_APP_STARTS, appStarts)
                .putString(PREF_KEY_LAST_APP_START, today)
                .putInt(PREF_KEY_DAYS_USED, daysUsed)
                .apply();
    }

    public void trackDismissal() {
        trackDismissal(mContext);
    }

    public static void trackDismissal(Context ctx) {
        SharedPreferences s = getPreferences(ctx);
        int dismissalCount = s.getInt(PREF_KEY_DISMISSAL_COUNT, 0) + 1;
        String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault()).format(new Date());

        s.edit()
                .putInt(PREF_KEY_DISMISSAL_COUNT, dismissalCount)
                .putString(PREF_KEY_LAST_DISMISSED_AT, today)
                .apply();
    }

    public void trackRated() {
        trackRated(mContext);
    }

    public static void trackRated(Context ctx) {
        SharedPreferences s = getPreferences(ctx);
        s.edit()
                .putBoolean(PREF_KEY_DID_RATE, true)
                .apply();
    }

    public void trackFeedback() {
        trackFeedback(mContext);
    }

    public static void trackFeedback(Context ctx) {
        trackDismissal(ctx);
    }

    public static SharedPreferences getPreferences(Context ctx) {
        if (sPrefs == null)
            sPrefs = ctx.getSharedPreferences(getPrefFileName(ctx), Context.MODE_PRIVATE);
        return sPrefs;
    }

    public static String getPrefFileName(Context ctx) {
        return ctx.getPackageName() + PREF_FILE_NAME_SUFFIX;
    }
}
