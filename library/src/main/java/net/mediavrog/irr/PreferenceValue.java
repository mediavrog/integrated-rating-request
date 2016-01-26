package net.mediavrog.irr;

import android.content.SharedPreferences;

import net.mediavrog.ruli.Value;

/**
 * Created by maikvlcek on 1/27/16.
 */
public abstract class PreferenceValue<T> extends Value<T> {

    public interface PreferenceProvider {
        SharedPreferences getPreferences();
    }

    PreferenceProvider mProvider;
    String mPrefKey;

    public static PreferenceValue<String> s(PreferenceProvider p, String prefKey) {
        return new PreferenceValue<String>(p, prefKey) {
            @Override
            public String get() {
                return mProvider.getPreferences().getString(mPrefKey, "");
            }
        };
    }

    public static PreferenceValue<Integer> i(PreferenceProvider p, String prefKey) {
        return new PreferenceValue<Integer>(p, prefKey) {
            @Override
            public Integer get() {
                return mProvider.getPreferences().getInt(mPrefKey, 0);
            }
        };
    }

    public static PreferenceValue<Boolean> b(PreferenceProvider p, String prefKey) {
        return new PreferenceValue<Boolean>(p, prefKey) {
            @Override
            public Boolean get() {
                return mProvider.getPreferences().getBoolean(mPrefKey, false);
            }
        };
    }

    private PreferenceValue(PreferenceProvider p, String k) {
        this.mProvider = p;
        this.mPrefKey = k;
    }

    @Override
    public String describe() {
        return super.describe() + " " + this.mPrefKey;
    }
}