package com.project.ada.silversparro.core;

/**
 * Created by ankitmaheshwari on 8/15/17.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.Type;


/**
 Singleton Class for accessing SharedPreferences,
 should be initialized once in the beginning by any application component using static
 method initialize(applicationContext)

 @author ankitmaheshwari */
public class SharedPrefsManager {

    private static final String TAG = "SharedPrefsManager";
    private SharedPreferences prefs;
    private static SharedPrefsManager uniqueInstance;
    public static final String PREF_NAME = "com.jt.weather.prefs";

    private SharedPrefsManager(Context appContext) {
        prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     Throws IllegalStateException if this class is not initialized

     @return unique SharedPrefsManager instance
     */
    public static SharedPrefsManager getInstance() {
        if (uniqueInstance == null) {
            throw new IllegalStateException(
                    "SharedPrefsManager is not initialized, call initialize(applicationContext) " +
                            "static method first");
        }
        return uniqueInstance;
    }

    /**
     Initialize this class using application Context,
     should be called once in the beginning by any application Component

     @param appContext application context
     */
    public static void initialize(Context appContext) {
        if (appContext == null) {
            throw new NullPointerException("Provided application context is null");
        }
        if (uniqueInstance == null) {
            synchronized (SharedPrefsManager.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new SharedPrefsManager(appContext);
                }
            }
        }
    }

    private SharedPreferences getPrefs() {
        return prefs;
    }

    /**
     Clears all data in SharedPreferences
     */
    public void clearPrefs() {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.clear();
        editor.commit();
    }

    public void removeKey(String key) {
        getPrefs().edit().remove(key).commit();
    }

    public boolean containsKey(String key) {
        return getPrefs().contains(key);
    }

    public String getString(String key, String defValue) {
        return getPrefs().getString(key, defValue);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(key, value);
        editor.apply();
    }

    public int getInt(String key, int defValue) {
        return getPrefs().getInt(key, defValue);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public long getLong(String key, long defValue) {
        return getPrefs().getLong(key, defValue);
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public void setLong(String key, long value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return getPrefs().getBoolean(key, defValue);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public float getFloat(String key) {
        return getFloat(key, 0f);
    }

    public float getFloat(String key, float defValue) {
        return getPrefs().getFloat(key, defValue);
    }

    public void setFloat(String key, Float value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    /**
     Persists an Object in prefs at the specified key, class of given Object must implement Model
     interface

     @param key String
     @param modelObject Object to persist
     @param <M> Generic for Object
     */
    public <M> void setObject(String key, M modelObject) {
        String value = Utils.createJSONStringFromObject(modelObject);
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     Fetches the previously stored Object of given Class from prefs

     @param key String
     @param classOfModelObject Class of persisted Object
     @param <M> Generic for Object

     @return Object of given class
     */
    public <M> M getObject(String key, Class<M> classOfModelObject) {
        String jsonData = getPrefs().getString(key, null);
        if (null != jsonData) {
            try {
                Gson gson = new Gson();
                M customObject = gson.fromJson(jsonData, classOfModelObject);
                return customObject;
            } catch (ClassCastException cce) {
                Log.e(TAG, "Cannot convert string obtained from prefs into collection of type " +
                        classOfModelObject.getName(), cce);
            }
        }
        return null;
    }

    /**
     Persists a Collection object in prefs at the specified key

     @param key String
     @param dataCollection Collection Object
     @param <C> Generic for Collection object
     */
    public <C> void setCollection(String key, C dataCollection) {
        SharedPreferences.Editor editor = getPrefs().edit();
        String value = Utils.createJSONStringFromObject(dataCollection);
        editor.putString(key, value);
        editor.apply();
    }

    /**
     Fetches the previously stored Collection Object of given type from prefs

     @param key String
     @param typeOfC Type of Collection Object
     @param <C> Generic for Collection Object

     @return Collection Object which can be casted
     */
    public <C> C getCollection(String key, Type typeOfC) {
        String jsonData = getPrefs().getString(key, null);
        if (null != jsonData) {
            try {
                Gson gson = new Gson();
                C arrFromPrefs = gson.fromJson(jsonData, typeOfC);
                return arrFromPrefs;
            } catch (ClassCastException cce) {
                Log.e(TAG, "Cannot convert string obtained from prefs into collection of type " +
                        typeOfC.toString(), cce);
            }
        }
        return null;
    }


    public SharedPreferences.Editor getEditor() {
        return getPrefs().edit();
    }

}

