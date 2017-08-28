package com.project.ada.silversparro.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.project.ada.silversparro.Constants;
import com.project.ada.silversparro.core.MainApplication;
import com.project.ada.silversparro.data.Annotation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.project.ada.silversparro.Constants.IMAGE_NAME_PREFIX;
import static com.project.ada.silversparro.Constants.IMAGE_NAME_SUFFIX;

/**
 * Created by ankitmaheshwari on 8/15/17.
 */

public class Utils {

    private static final String TAG = "Utils";

    /* a utility to validate Indian phone number example - 03498985532, 5389829422 **/
    public static boolean isValidPhoneNumber(String number) {
        if (!TextUtils.isEmpty(number)) {
            return number.matches("^0?(\\d{10})");
        }
        return false;
    }

    public static boolean isCollectionFilled(Collection<?> collection) {
        return null != collection && collection.isEmpty() == false;
    }

    public static boolean compareLists(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        Collections.sort(list1);
        Collections.sort(list2);
        for (int index = 0; index < list1.size(); index++) {
            if (list1.get(index).equals(list2.get(index)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Format distance in meters to a two decimal KM value, RoundingMode is FLOOR
     * @param distanceInMeters
     * @return
     */
    public static String formatToKmsWithTwoDecimal(float distanceInMeters){
        return getDecimalFormat("0.00").format(distanceInMeters / 1000);
    }

    /**
     * Format the input float to a one decimal String, RoundingMode is FLOOR
     * @param distance
     * @return
     */
    public static String formatWithOneDecimal(float distance){
        return getDecimalFormat("0.0").format(distance);
    }

    /**
     * Format the input double to a one decimal String, RoundingMode is FLOOR
     * @param distance
     * @return
     */
    public static String formatWithOneDecimal(double distance){
        return getDecimalFormat("0.0").format(distance);
    }

    public static String formatCalories(double calories){
        String caloriesString = "";
        if (calories > 10){
            caloriesString = Math.round(calories) + " Cal";
        }else {
            String cals = Utils.formatWithOneDecimal(calories);
            if ("0.0".equals(cals)){
                caloriesString = "--";
            }else {
                caloriesString = cals + " Cal";
            }
        }
        return caloriesString;
    }

    private static DecimalFormat getDecimalFormat(String pattern){
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat(pattern, dfs);
        df.setGroupingUsed(false);
        df.setRoundingMode(RoundingMode.FLOOR);
        return df;
    }

    public static String formatIndianCommaSeparated(float value){
        return NumberFormat.getNumberInstance(Locale.ENGLISH).format((int) value);
    }

    /**
     * gets screen height in pixels, Application Context should be used
     */
    public static int getScreenHeightUsingDisplayMetrics(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    /**
     * gets screen width in pixels, Application Context should be used
     */
    public static int getScreenWidthUsingDisplayMetrics(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    public static float convertDpToPixel(Context context, float dp) {
        Context localContext = context;
        DisplayMetrics displayMetrics = localContext.getResources().getDisplayMetrics();
        return dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String createJSONStringFromObject(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    public static <T> T createObjectFromJSONString(String jsonString, Class<T> clazz)
            throws JsonSyntaxException {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, clazz);

    }


    public static boolean isScreenTooLarge(Context context) {
        int screenLayoutWithMask = context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK;
        switch (screenLayoutWithMask) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return true;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return true;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return false;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return false;
        }
        return false;
    }


    /**
     * Returns time in HH:MM:SS format
     * @param secs time interval in secs
     * @return
     */
    public static final String secondsToHHMMSS(int secs) {

        if (secs >= 3600) {
            int sec = secs % 60;
            int totalMins = secs / 60;
            int hour = totalMins / 60;
            int min = totalMins % 60;
            String formatted = String.format("%02d:%02d:%02d", hour, min, sec);
            if (formatted.startsWith("0")){
                return formatted.substring(1);
            }else {
                return formatted;
            }
        } else {
            return String.format("%02d:%02d", secs / 60, secs % 60);
        }
    }

    public static final long hhmmssToSecs(String hhmmss) {
        Long secs = 0L;
        String[] parts = hhmmss.split(":");
        switch (parts.length){
            case 3:
                String left = parts[0];
                if (left.contains(" ")){
                    String[] leftParts = left.split("\\s+");
                    secs += 86400*Long.parseLong(leftParts[0]);
                    secs += 3600*Long.parseLong(leftParts[1]);
                }else{
                    secs += 3600*Long.parseLong(left);
                }
                secs += 60*Long.parseLong(parts[1]);
                secs += Long.parseLong(parts[2]);
                break;
            case 2:
                secs += 60*Long.parseLong(parts[0]);
                secs += Long.parseLong(parts[1]);
                break;
            case 1:
                secs += Long.parseLong(parts[0]);
        }
        return secs;
    }

    public static final String secondsToHoursAndMins(int secs) {
        if (secs >= 3600) {
            int totalMins = secs / 60;
            int hour = totalMins / 60;
            int min = totalMins % 60;
            return String.format("%d hr %d min", hour, min);
        } else {
            int totalMins = secs / 60;
            return String.format("%d min", totalMins);
        }
    }

    public static String createPrettyJSONStringFromObject(Object object) {
        return new GsonBuilder()
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .create().toJson(object);
    }

    public static final long stringToSec(String time) {

        String[] timeArray = time.split("[:\\s]");
        int j = 1;
        long sec = 0;
        for (int i = timeArray.length - 1; i >= 0; i--) {
            int duration = Integer.parseInt(timeArray[i]);
            sec = duration * j + sec;
            j = j * 60;

        }
        return sec;
    }

    public static Uri getLocalBitmapUri(Bitmap bmp, Context context) {
        Uri bmpUri = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    IMAGE_NAME_PREFIX + new Date().getTime() + IMAGE_NAME_SUFFIX);
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public static Bitmap getBitmapFromLiveView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public static String dedupName(String firstName, String lastName){
        if (TextUtils.isEmpty(lastName)){
            return firstName;
        }else if (TextUtils.isEmpty(firstName)){
            return lastName;
        }
        // If both the names are present
        String name = firstName + " " + lastName;
        String[] parts = name.split("\\s+");
        StringBuilder sb = new StringBuilder();

        // De dup logic
        int len = parts.length;
        if (len > 1){
            sb.append(toCamelCase(parts[0]));
            for (int i=1; i<len; i++){
                if (!parts[i-1].equalsIgnoreCase(parts[i])){
                    sb.append(" ");
                    sb.append(toCamelCase(parts[i]));
                }
            }
            return sb.toString();
        }else if (len == 1){
            return toCamelCase(parts[0]);
        }else {
            return "";
        }
    }

    public static String toCamelCase(final String init) {
        if (init==null)
            return null;

        final StringBuilder ret = new StringBuilder(init.length());

        for (final String word : init.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase());
                ret.append(word.substring(1).toLowerCase());
            }
            if (!(ret.length()==init.length()))
                ret.append(" ");
        }

        return ret.toString();
    }


    public static void hideKeyboard(View view, Context context) {
        if (view == null || context == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);

        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String getAppVersion(){
        String appVersion = SharedPrefsManager.getInstance().getString(Constants.PREF_APP_VERSION);
        if (TextUtils.isEmpty(appVersion)){
            MainApplication.getInstance().updateAppVersionInPrefs();
        }
        return appVersion;
    }

    public static String UNIQUE_ID;

    /**
     * Returns a unique ID of device that is ANDROID_ID
     * @param context
     * @return
     */
    public static String getUniqueId(Context context) {
        if (!TextUtils.isEmpty(UNIQUE_ID)){
            return UNIQUE_ID;
        }
        if (null == context) {
            context = MainApplication.getContext();
        }
        UNIQUE_ID = getAndroidID(context);
        return UNIQUE_ID;
    }

    /**
     * Returns device IMEI, requires READ_PHONE_STATE permission which belongs to Phone group
     * @param context
     * @return device IMEI
     */
    private static String getDeviceIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    /**
     * A 64-bit number (as a hex string) that is randomly generated when the user first sets up the
     * device and should remain constant for the lifetime of the user's device.
     * The value may change if a factory reset is performed on the device
     * @param context
     * @return
     */
    private static String getAndroidID(Context context) {
        String androidId = Settings.Secure
                .getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId != null) {
            return androidId;
        } else {
            return "";
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static Annotation createDummyAnnotation(Bitmap bitmap){
        Annotation annotation = new Annotation();
        String url = getLocalBitmapUri(bitmap, MainApplication.getContext()).toString();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        List<String> classes = new ArrayList<String>(){{
            add("dummy_1");
            add("dummy_2");
            add("dummy_3");
        }};

        annotation.setImageUrl(url);
        annotation.setBoxClasses(classes);
        annotation.setDataSet("Dummy Dataset");
        annotation.setImageWidth(width);
        annotation.setImageHeight(height);

        return annotation;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static RectF convertPointsArrayToRectF(Point[] points){
        if (points.length != 4){
            throw new IllegalArgumentException("Input array of points should be of length four");
        }
        float right = 0;
        float bottom = 0;
        float left = Float.MAX_VALUE;
        float top = Float.MAX_VALUE;

        for (int i=0; i<points.length; i++){
            Point p = points[i];
            if (p.x <= left){
                left = p.x;
            }
            if (p.y <= top){
                top = p.y;
            }
            if (p.x >= right){
                right = p.x;
            }
            if (p.y >= bottom){
                bottom = p.y;
            }
        }
        return new RectF(left, top, right, bottom);
    }

    public static RectF convertPointsListToRectF(List<Point> points){
        if (points.size() != 4){
            throw new IllegalArgumentException("Input list of points should be of size four");
        }
        float right = 0;
        float bottom = 0;
        float left = Float.MAX_VALUE;
        float top = Float.MAX_VALUE;

        for (int i=0; i<points.size(); i++){
            Point p = points.get(i);
            if (p.x <= left){
                left = p.x;
            }
            if (p.y <= top){
                top = p.y;
            }
            if (p.x >= right){
                right = p.x;
            }
            if (p.y >= bottom){
                bottom = p.y;
            }
        }
        return new RectF(left, top, right, bottom);
    }

}
