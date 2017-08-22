package com.project.ada.silversparro.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.project.ada.silversparro.core.HttpClientManager;
import com.project.ada.silversparro.core.MainApplication;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.io.IOException;

/**
 * Created by ankitmaheshwari on 8/18/17.
 */

public class SilverImageLoader {

    private static final String TAG = "SilverImageLoader";

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;
    private static final boolean USE_MEMORY_CACHE = true;
    private static final float LOW_MEMORY_THRESHOLD_PERCENTAGE = 5;
    private final Picasso picasso;
    private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB


    private static SilverImageLoader instance;

    private SilverImageLoader() {
        picasso = constructImageLoader();
    }

    public static synchronized SilverImageLoader getInstance() {
        if (instance == null) {
            instance = new SilverImageLoader();
        }
        return instance;
    }

    private static File createDefaultCacheDir(Context context) {
        File cache = new File(context.getApplicationContext().getCacheDir(), "picasso-cache");
        if (!cache.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cache.mkdirs();
        }
        return cache;
    }

    private Picasso constructImageLoader() {

        OkHttpClient httpClient = HttpClientManager.getDefaultHttpClient();

        httpClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request newRequest = chain.request().newBuilder().addHeader("Accept", "image/webp")
                        .build();
                return chain.proceed(newRequest);
            }
        });

        httpClient.networkInterceptors().add(REWRITE_CACHE_CONTROL_INTERCEPTOR);
        httpClient.setCache(new Cache(createDefaultCacheDir(MainApplication.getContext()), MAX_DISK_CACHE_SIZE));

        Picasso.Builder builder =  new Picasso.Builder(MainApplication.getContext());
        builder.downloader(new OkHttpDownloader(httpClient));
        Picasso built = builder.build();
//        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
        return built;

    }


    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .removeHeader("Cache-Control")
                    .removeHeader("Pragma")
                    .header("Cache-Control", "max-age="+60*60*24*7 +", public") // 1 week cache.
                    .build();
        }
    };


    /**
     * Downloads image from given URL and load it in ImageView
     *
     * @param url       URL string
     * @param imageView ImageView for loading image
     */
    public void loadImage(String url, ImageView imageView) {
        loadImage(url, imageView, null, null);
    }

    /**
     * Downloads image from given URL and load it in ImageView
     *
     * @param url            URL string
     * @param imageView      ImageView for loading image
     * @param backupDrawable displayed while image is downloading and if download fails
     */
    public void loadImage(String url, ImageView imageView, Drawable backupDrawable) {
        loadImage(url, imageView, backupDrawable, backupDrawable);
    }


    /**
     * Downloads image from given URL and load it in ImageView
     *
     * @param url                  URL string
     * @param imageView            ImageView for loading image
     * @param drawableWhileLoading displayed while image is downloading
     * @param drawableOnFailure    displayed if download fails
     */
    public void loadImage(final String url, final ImageView imageView, final Drawable drawableWhileLoading,
                          final Drawable drawableOnFailure) {

        if (isAdequateMemoryAvailable()) {
            if (imageView != null && !TextUtils.isEmpty(url)) {
                RequestCreator request = picasso.load(url);
                if (drawableWhileLoading != null) {
                    request.placeholder(drawableWhileLoading);
                }
                if (drawableOnFailure != null) {
                    request.error(drawableOnFailure);
                }
                request.config(BITMAP_CONFIG);
                request.networkPolicy(NetworkPolicy.OFFLINE);
                if (!USE_MEMORY_CACHE) {
                    request.memoryPolicy(MemoryPolicy.NO_CACHE);
                }
                request.into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Image loaded successfully from Disk, url = " + url);
                    }

                    @Override
                    public void onError() {
                        Log.d(TAG, "Image could not be loaded from Disk, lets try network. url = " + url);
                        RequestCreator secondRequest = picasso.load(url);
                        if (drawableWhileLoading != null) {
                            secondRequest.placeholder(drawableWhileLoading);
                        }
                        if (drawableOnFailure != null) {
                            secondRequest.error(drawableOnFailure);
                        }
                        secondRequest.config(BITMAP_CONFIG);
                        if (!USE_MEMORY_CACHE) {
                            secondRequest.memoryPolicy(MemoryPolicy.NO_CACHE);
                        }
                        secondRequest.into(imageView);
                    }
                });
                return;
            }
        }
        if (imageView != null && drawableOnFailure != null) {
            imageView.setImageDrawable(drawableOnFailure);
        }

    }

    /**
     * Downloads image from given URL and load it in square ImageView with given dimension
     *
     * @param url       URL string
     * @param imageView ImageView for loading image
     */
    public void loadSquareImage(final String url, final ImageView imageView, final Drawable backupDrawable,
                                int dimension) {
        if (isAdequateMemoryAvailable()) {
            if (imageView != null && !TextUtils.isEmpty(url)) {
                RequestCreator request = picasso.load(url).resize(dimension, dimension)
                        .centerCrop();
                if (backupDrawable != null) {
                    request.placeholder(backupDrawable).error(backupDrawable);
                }
                request.config(BITMAP_CONFIG);
                request.networkPolicy(NetworkPolicy.OFFLINE);
                if (!USE_MEMORY_CACHE) {
                    request.memoryPolicy(MemoryPolicy.NO_CACHE);
                }
                request.into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Image loaded successfully from Disk, url = " + url);
                    }

                    @Override
                    public void onError() {
                        Log.d(TAG, "Image could not be loaded from Disk, lets try network. url = " + url);
                        RequestCreator secondRequest = picasso.load(url);
                        if (backupDrawable != null) {
                            secondRequest.placeholder(backupDrawable).error(backupDrawable);
                        }
                        secondRequest.config(BITMAP_CONFIG);
                        if (!USE_MEMORY_CACHE) {
                            secondRequest.memoryPolicy(MemoryPolicy.NO_CACHE);
                        }
                        secondRequest.into(imageView);
                    }
                });
                return;
            }
        }
        if (imageView != null && backupDrawable != null) {
            imageView.setImageDrawable(backupDrawable);
        }
    }

    /**
     * Loads and decodes image synchronously
     * Default display image options from provided configuration will be used
     * @return Result image Bitmap. Can be <b>null</b> if image loading/decoding was failed or
     * cancelled.
     */
    public Bitmap loadImageSync(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }

        Bitmap bitmap = null;
        try {
            bitmap = picasso.load(imageUrl).get();
        } catch (IOException e) {
            Log.e(TAG, "IOException during synchronous image download from URL");
            e.printStackTrace();
        }
        return bitmap;
    }

    private boolean isAdequateMemoryAvailable() {

        boolean isAdequateMemoryAvailable = true;
        // Get app memory info
        long total = Runtime.getRuntime().maxMemory();
        long used = Runtime.getRuntime().totalMemory();
        float percentAvailable = 100f * (1f - ((float) used / total));
        if (percentAvailable <= LOW_MEMORY_THRESHOLD_PERCENTAGE) {
            Log.e(TAG, "LOW MEMORY, ONLY " + percentAvailable + "% AVAILABLE!");
            isAdequateMemoryAvailable = false;
            handleLowMemory();
        }

        return isAdequateMemoryAvailable;
    }

    private void handleLowMemory() {
        // handle low memory
        Log.d(TAG,"HANDLING LOW MEMORY");
        System.gc();
    }

    public Picasso getImageLoader(){
        return picasso;
    }

}
