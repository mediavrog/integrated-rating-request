package net.mediavrog.irr;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import net.mediavrog.ruli.RuleEngine;

/**
 * Created by maikvlcek on 1/26/16.
 */
// TODO: persist across orientation changes etc
public class IrrLayout extends FrameLayout {
    public static final String TAG = IrrLayout.class.getSimpleName();

    /**
     * Current state of the rating request view.
     */
    public enum State {
        HIDDEN,
        NUDGE,
        RATE,
        FEEDBACK
    }

    public interface OnUserDecisionListener {
        void onAccept(Context ctx, State s);

        void onDismiss(Context ctx, State s);
    }

    public interface OnUserActionListener {
        void onRate(Context ctx);

        void onFeedback(Context ctx);
    }

    public interface OnToggleVisibilityListener {
        void onShow(IrrLayout irr);

        void onHide(IrrLayout irr);
    }

    /**
     * Default attributes for layout
     */
    private static final int[] DEFAULT_ATTRS = new int[]{
            android.R.attr.animateLayoutChanges
    };

    private static final int mNudgeLayoutResId = R.id.irr_nudge_layout;

    private static final int mRateLayoutResId = R.id.irr_rate_layout;

    private static final int mFeedbackLayoutResId = R.id.irr_feedback_layout;

    private static final int mNudgeAcceptBtnResId = R.id.irr_nudge_accept_btn;

    private static final int mNudgeDeclineBtnResId = R.id.irr_nudge_decline_btn;

    private static final int mRateAcceptBtnResId = R.id.irr_rate_accept_btn;

    private static final int mRateDeclineBtnResId = R.id.irr_rate_decline_btn;

    private static final int mFeedbackAcceptBtnResId = R.id.irr_feedback_accept_btn;

    private static final int mFeedbackDeclineBtnResId = R.id.irr_feedback_decline_btn;

    /**
     * Flag to check whether we can just go ahead performing operations with the default rule engine
     * or should rather wait for the user to provide a rule engine to the layout.
     */
    private boolean mUseCustomEngine;

    private String mRatingUrl = null;

    private String mFeedbackUrl = null;

    private View mNudgeView = null;

    private View mRateView = null;

    private View mFeedbackView = null;

    private RuleEngine mRuleEngine = null;

    /**
     *
     */
    private OnUserDecisionListener mDecisionListener = null;

    /**
     * Fires when user decides to take action to either rate the app or give feedback.
     */
    private OnUserActionListener mActionListener = null;

    /**
     * Fires when irr layout should be shown or hidden.
     */
    private OnToggleVisibilityListener mVisibilityListener = null;

    /**
     * Keeps track if view is attached to window.
     */
    private boolean mIsAttached;

    public IrrLayout(Context context) {
        this(context, null);
    }

    public IrrLayout(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public IrrLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            // default layout attrs
            TypedArray defAttrs = context.obtainStyledAttributes(attrs, DEFAULT_ATTRS);

            if (defAttrs != null) {
                // enable layout transition to provide nice effects out of the box
                boolean animateLayoutChanges = defAttrs.getBoolean(0, true);
                if (animateLayoutChanges) setLayoutTransition(new LayoutTransition());

                defAttrs.recycle();
            }

            // custom view attributes
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IrrLayout);

            if (ta != null) {
                mRatingUrl = ta.getString(R.styleable.IrrLayout_ratingUrl);

                mFeedbackUrl = ta.getString(R.styleable.IrrLayout_feedbackUrl);

                // set default listener if at least one of rating or feedback url was given
                if (mRatingUrl != null || mFeedbackUrl != null) {
                    mActionListener = new DefaultOnUserActionListener(mRatingUrl, mFeedbackUrl);
                }

                // check if user wants to use a custom rule engine
                mUseCustomEngine = ta.getBoolean(R.styleable.IrrLayout_useCustomRuleEngine, false);

                // .. if not, we use the default engine provided by the library
                if (!mUseCustomEngine) setupDefaultRuleEngine(context, ta);

                ta.recycle();
            }
        }

        mVisibilityListener = new DefaultOnToggleVisibilityListener();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        initializeStates();
        enableFlowControls();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mIsAttached) {
            mIsAttached = true;

            // toggle immediately if rule engine did already calculate the result
            toggleTo(mRuleEngine != null && mRuleEngine.isReady() && mRuleEngine.isValid());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
    }

    public void setOnUserDecisionListener(OnUserDecisionListener l) {
        mDecisionListener = l;
    }

    public OnUserDecisionListener getOnUserDecisionListener() {
        return this.mDecisionListener;
    }

    public void setOnToggleVisibilityListener(OnToggleVisibilityListener l) {
        mVisibilityListener = l;
    }

    public OnToggleVisibilityListener getOnToggleVisibilityListener() {
        return mVisibilityListener;
    }

    public void setOnUserActionListener(OnUserActionListener l) {
        mActionListener = l;
    }

    public OnUserActionListener getOnUserActionListener() {
        return mActionListener;
    }

    public void setRuleEngine(RuleEngine b) {
        if (!mUseCustomEngine)
            throw new RuntimeException("Cannot set a custom rule engine unless " +
                    "irr:seCustomRuleEngine is set to true," +
                    " because the default rule engine was already loaded.");

        configureEngine(b);
    }

    public RuleEngine getRuleEngine() {
        return mRuleEngine;
    }

    /**
     * Sets up the default rule engine with attributes passed via layout params.
     *
     * @param ctx
     * @param ta
     */
    void setupDefaultRuleEngine(Context ctx, TypedArray ta) {
        int appStartCount = ta.getInt(R.styleable.IrrLayout_defaultRuleAppStartCount, DefaultRuleEngine.DEFAULT_APP_START_COUNT);

        int distinctDays = ta.getInt(R.styleable.IrrLayout_defaultRuleDistinctDays, DefaultRuleEngine.DEFAULT_DISTINCT_DAYS);

        int postponeDays = ta.getInt(R.styleable.IrrLayout_defaultRuleDismissPostponeDays, DefaultRuleEngine.DEFAULT_POSTPONE_DAYS);

        int maxDismissCount = ta.getInt(R.styleable.IrrLayout_defaultRuleDismissMaxCount, DefaultRuleEngine.DEFAULT_MAX_DISMISS_COUNT);

        // default rule engine evaluation should be fast enough to run on ui thread by default
        boolean autoEval = ta.getBoolean(R.styleable.IrrLayout_autoEvaluateDefaultRuleEngine, true);

        DefaultRuleEngine engine = DefaultRuleEngine.newInstance(ctx, appStartCount, distinctDays, postponeDays, maxDismissCount);
        setOnUserDecisionListener(engine.getListener());

        configureEngine(engine);

        if (autoEval) mRuleEngine.evaluate();
    }

    private void configureEngine(RuleEngine b) {
        mRuleEngine = b;

        // listen to engine state changes
        mRuleEngine.setOnRulesEvaluatedListener(new RuleEngine.OnRulesEvaluatedListener() {
            public void onResult(boolean isValid) {
                if (!mIsAttached) return;
                toggleTo(isValid);
            }
        });
    }

    void toggleTo(boolean shouldShow) {
        if (shouldShow) {
            show();
        } else {
            hide();
        }
    }

    void show() {
        if (mVisibilityListener != null) mVisibilityListener.onShow(this);
        setState(State.NUDGE);
    }

    void hide() {
        if (mVisibilityListener != null) mVisibilityListener.onHide(this);
    }

    void dismiss(State s) {
        if (mDecisionListener != null) mDecisionListener.onDismiss(getContext(), s);
        hide();
    }

    /**
     * Initializes visibility of the 3 steps container.
     */
    void initializeStates() {
        mNudgeView = findViewById(mNudgeLayoutResId);
        mRateView = findViewById(mRateLayoutResId);
        mFeedbackView = findViewById(mFeedbackLayoutResId);

        if (mNudgeView == null || mRateView == null || mFeedbackView == null) {
            throw new RuntimeException("Please provide all 3 state container views using 'android:id=\"@id/irr_nudge_layout\"' etc.");
        }

        setState(State.NUDGE);
    }

    /**
     * Adds functionality to buttons to move through the various nudge states.
     */
    void enableFlowControls() {
        //
        // State: Nudge
        //
        findViewById(mNudgeAcceptBtnResId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDecisionListener != null)
                    mDecisionListener.onAccept(getContext(), State.NUDGE);
                setState(State.RATE);
            }
        });

        findViewById(mNudgeDeclineBtnResId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDecisionListener != null)
                    mDecisionListener.onDismiss(getContext(), State.NUDGE);
                setState(State.FEEDBACK);
            }
        });

        //
        // State: Rate
        //
        findViewById(mRateAcceptBtnResId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDecisionListener != null) mDecisionListener.onAccept(getContext(), State.RATE);
                if (mActionListener != null) mActionListener.onRate(getContext());
                hide();
            }
        });

        findViewById(mRateDeclineBtnResId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(State.RATE);
            }
        });

        //
        // State: Feedback
        //
        findViewById(mFeedbackAcceptBtnResId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDecisionListener != null)
                    mDecisionListener.onAccept(getContext(), State.FEEDBACK);
                if (mActionListener != null) mActionListener.onFeedback(getContext());
                hide();
            }
        });

        findViewById(mFeedbackDeclineBtnResId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(State.FEEDBACK);
            }
        });
    }

    /**
     * View changes for current step
     *
     * @param step
     */
    void setState(State step) {
        switch (step) {
            case NUDGE:
                mNudgeView.setVisibility(VISIBLE);
                mRateView.setVisibility(INVISIBLE);
                mFeedbackView.setVisibility(INVISIBLE);
                break;
            case RATE:
                mNudgeView.setVisibility(INVISIBLE);
                mRateView.setVisibility(VISIBLE);
                mFeedbackView.setVisibility(INVISIBLE);
                break;
            case FEEDBACK:
                mNudgeView.setVisibility(INVISIBLE);
                mRateView.setVisibility(INVISIBLE);
                mFeedbackView.setVisibility(VISIBLE);
                break;
        }
    }
}
