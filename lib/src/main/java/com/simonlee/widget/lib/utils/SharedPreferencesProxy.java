package com.simonlee.widget.lib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.simonlee.widget.lib.application.ApplicationProxy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

/**
 * SharedPreferences工具类
 *
 * @author Simon Lee
 * @e-mail jmlixiaomeng@163.com
 * @github https://github.com/Simon-Leeeeeeeee/SLWidget
 * @createdTime 2019-07-19
 */
@SuppressWarnings({"unused", "WeakerAccess", "RedundantSuppression"})
public class SharedPreferencesProxy {

    private final SharedPreferences mSharedPreferences;

    private SharedPreferences.Editor mEditor;

    public SharedPreferencesProxy() {
        this(null);
    }

    public SharedPreferencesProxy(@Nullable String name) {
        Context context = ApplicationProxy.getApplication();
        if (TextUtils.isEmpty(name)) {
            name = context.getPackageName();
        }
        mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }

    public String getString(String key, @Nullable String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        Set<String> stringSet = mSharedPreferences.getStringSet(key, null);
        if (stringSet == null) {
            return defValues;
        } else {
            return new HashSet<>(stringSet);
        }
    }

    public int getInt(String key, int defValue) {
        return mSharedPreferences.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return mSharedPreferences.getFloat(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }

    public boolean contains(String key) {
        return mSharedPreferences.contains(key);
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public SharedPreferences.Editor edit() {
        if (mEditor == null) {
            mEditor = mSharedPreferences.edit();
        }
        return mEditor;
    }

    public SharedPreferences.Editor putString(String key, @Nullable String value) {
        if (mEditor == null) {
            mEditor = mSharedPreferences.edit();
        }
        mEditor.putString(key, value);
        return mEditor;
    }

    public SharedPreferences.Editor putStringSet(String key, @Nullable Set<String> values) {
        if (mEditor == null) {
            mEditor = mSharedPreferences.edit();
        }
        mEditor.putStringSet(key, values);
        return mEditor;
    }

    public SharedPreferences.Editor putInt(String key, int value) {
        if (mEditor == null) {
            mEditor = mSharedPreferences.edit();
        }
        mEditor.putInt(key, value);
        return mEditor;
    }

    public SharedPreferences.Editor putLong(String key, long value) {
        if (mEditor == null) {
            mEditor = mSharedPreferences.edit();
        }
        mEditor.putLong(key, value);
        return mEditor;
    }

    public SharedPreferences.Editor putFloat(String key, float value) {
        if (mEditor == null) {
            mEditor = mSharedPreferences.edit();
        }
        mEditor.putFloat(key, value);
        return mEditor;
    }

    public SharedPreferences.Editor putBoolean(String key, boolean value) {
        if (mEditor == null) {
            mEditor = mSharedPreferences.edit();
        }
        mEditor.putBoolean(key, value);
        return mEditor;
    }

    public SharedPreferences.Editor remove(String key) {
        if (mEditor == null) {
            mEditor = mSharedPreferences.edit();
        }
        mEditor.remove(key);
        return mEditor;
    }

    public SharedPreferences.Editor clear() {
        if (mEditor == null) {
            mEditor = mSharedPreferences.edit();
        }
        mEditor.clear();
        return mEditor;
    }

    public boolean commit() {
        if (mEditor != null) {
            return mEditor.commit();
        }
        return false;
    }

    public void apply() {
        if (mEditor != null) {
            mEditor.apply();
        }
    }

}
