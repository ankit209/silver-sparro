package com.project.ada.silversparro.core;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.project.ada.silversparro.R;
import com.project.ada.silversparro.views.DrawableView;
import com.project.ada.silversparro.views.ResizableRectangleView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static com.project.ada.silversparro.R.id.save;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";


    private DrawableView drawbleView;
    private PhotoView touchImageView;
    private ResizableRectangleView rectangleView;

    private Button enableZoomBtn;
    private Button setBoundingBox;
    private Button saveButton;
    private Button undoButton;
    private Button openGallery;
    private Button resetButton;
    private Button doneButton;

    /*  this is the action code we use in our intent,
        this way we know we're looking at the response from our own action
    */
    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath;
    private String filemanagerstring;

    private RectF latestScreenRect;
    private RectF initialScreenRect;

    /**
     * Array containing a screen rectangle for every bounding rectangle present in drawbleView.savedRectF
     */
    private ArrayList<RectF> savedScreenRectF = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO:if the corresponding text file exists, display the saved boxes

        drawbleView = (DrawableView) findViewById(R.id.drawble_view);
        touchImageView = (PhotoView) findViewById(R.id.zoom_iv);
        rectangleView = (ResizableRectangleView) findViewById(R.id.rectangle_view);

//        screenRectF.add()

        /*
        * gives the relative coordinates of the image with respect to the current view
        */
        touchImageView.setOnMatrixChangeListener(new OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rect) {
                Log.d(TAG, "onMatrixChanged:" + rect);
                //TODO:intialize screenRcectF with the original coordinates of pic
                //bug: crashes sometimes----- Disable zooming -> make a bound -> undo.
                if (initialScreenRect == null){
                    initialScreenRect = new RectF(rect.left,rect.top,rect.right,rect.bottom);
                }
                latestScreenRect = new RectF(rect.left,rect.top,rect.right,rect.bottom);
                //iteration over only those boxes which are saved.
                for(int i=0; i<drawbleView.savedRectF.size(); i++) {
                    Log.d(TAG, "-------------loop working for savedRectF.get(i)--------------- " + drawbleView.savedRectF.get(i));
                    RectF newRect = originalCoordinates(rect,savedScreenRectF.get(i),
                            drawbleView.savedRectF.get(i), drawbleView.savedScales.get(i),
                            touchImageView.getScale());
                    drawbleView.changingRectF.set(i,newRect);
                    drawbleView.setRect(newRect);
                }
                
                for(int i=drawbleView.allRectF.size()-1;i>=0;i--){
                    RectF r = drawbleView.allRectF.get(i);
                    if(!drawbleView.changingRectF.contains(r)){
                        drawbleView.allRectF.remove(r);
                    }
                    else {break;}
                }
                Log.d(TAG, "onMatrixChanged: allRectF" + drawbleView.allRectF);

                //TODO: remove the unsaved boxes

            }
        });


        enableZoomBtn = (Button) findViewById(R.id.enable_zoom);
        setBoundingBox = (Button) findViewById(R.id.bound);
        saveButton = (Button) findViewById(save);
        undoButton = (Button) findViewById(R.id.undo);
        openGallery = (Button) findViewById(R.id.gallery);
        resetButton = (Button) findViewById(R.id.reset);
        doneButton = (Button) findViewById(R.id.done);

        enableZoomBtn.setOnClickListener(this);
        undoButton.setOnClickListener(this);
        setBoundingBox.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);
        openGallery.setOnClickListener(this);
        drawbleView.setDrawingEnabled(false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.d(TAG, "################# onClick #################");
        switch (id) {
            case R.id.enable_zoom:
                if (enableZoomBtn.getText().equals("disable zoom")) {
                    drawbleView.setDrawingEnabled(true);
                    enableZoomBtn.setText("enable zoom");
                } else {
                    drawbleView.setDrawingEnabled(false);
                    enableZoomBtn.setText("disable zoom");
                }
                break;

            case R.id.undo:
                /*TODO: toast the user in if condition that the previous rectangle has been added to recFs, user will have to reset the image in order to change*/
                if (drawbleView.savedRectF.contains(drawbleView.getRect())) {
                    break;
                } else {
                    drawbleView.onClickUndo();
//                    drawbleView.rect.setEmpty();
                    if(drawbleView.allRectF.size()!=0) {
                        drawbleView.setRect(drawbleView.allRectF.get(drawbleView.allRectF.size()-1));
//                        drawbleView.rect = drawbleView.allRectF.get(drawbleView.allRectF.size() - 1);       //sets rect to the last rect which was made before the current rect.
                    }
                    else {drawbleView.getRect().setEmpty();}
                }
                break;

            case R.id.gallery:
                drawbleView.onClickReset();
                loadImagefromGallery();
                Log.d(TAG, "************************" + Environment.getExternalStorageDirectory());
                break;
            
            case R.id.bound:
                drawbleView.onClickBound();
                break;
            
            case save:
                if(drawbleView.allRectF.size()==0){
                    Toast.makeText(getApplicationContext(), "Invalid Operation", Toast.LENGTH_SHORT).show();
                }
                else{
                    drawbleView.onClickSave(touchImageView.getScale());
                    RectF r = latestScreenRect;
                    savedScreenRectF.add(r);
                    Log.d(TAG, "-------------------------rect to be added-------------" + r);
                    Log.d(TAG, "-------------------------savedScreenRectF--------------------------- " + savedScreenRectF);
                    Log.d(TAG, "-------------------------savedRectF------------------------ " + drawbleView.savedRectF);
                }
                break;
            
            case R.id.reset:
                drawbleView.onClickReset();
                savedScreenRectF.removeAll(savedScreenRectF);
                initialScreenRect = null;
                latestScreenRect = null;
                Log.d(TAG, "-------------------------savedScreenRectF--------------------------- " + savedScreenRectF);
                break;

            case R.id.done:
                ArrayList<RectF> originalCoordinates = new ArrayList<>();
                for(int i=0;i<=drawbleView.savedRectF.size()-1;i++){
                    Log.d(TAG, "DONE LOOP WORKING");
                    RectF rect = originalCoordinates(initialScreenRect,savedScreenRectF.get(i),drawbleView.savedRectF.get(i),drawbleView.savedScales.get(i),1);
                    Log.d(TAG, "originalRect ======"+rect);
                    originalCoordinates.add(rect);
                    Log.d(TAG, "originalCoordinates ======"+originalCoordinates);
                }
                String body = convertArrayToString(originalCoordinates);
                Log.d(TAG, "body---------------------> " + body.length());
                createTextFile(getApplicationContext(),body);
                //since we want all the arrays to reset
                drawbleView.onClickReset();
                break;
            default:
                break;
        }
    }

    //TODO: opens only SilverSparro folder to get image rather than showing all image directories.
    private void loadImagefromGallery() {
        if (verifyStoragePermissions(this)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,
                    "Select Picture"), SELECT_PICTURE);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                loadImageFromFilepath(selectedImagePath);
            }
        }
    }


    private void loadImageFromFilepath(String filepath) {
        Log.d(TAG, filepath);
        Bitmap myBitmap = BitmapFactory.decodeFile(filepath);
        touchImageView.setImageBitmap(myBitmap);
    }


    /*
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            /*
                HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
                THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            */
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else return null;
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static boolean verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            return false;
        } else {
            return true;
        }
    }



    public RectF originalCoordinates(RectF currentScreenRect, RectF savedScreenRect, RectF savedBox,
                                     float savedScale, float currentScale){

        float diffLeft = savedBox.left - savedScreenRect.left;
        float diffTop = savedBox.top - savedScreenRect.top;
        float diffRight = savedBox.right -savedScreenRect.right;
        float diffBottom = savedBox.bottom - savedScreenRect.bottom;


        float newLeft = diffLeft * currentScale/savedScale + currentScreenRect.left;
        float newRight = diffRight * currentScale/savedScale + currentScreenRect.right;
        float newTop = diffTop * currentScale/savedScale + currentScreenRect.top;
        float newBottom = diffBottom * currentScale/savedScale + currentScreenRect.bottom ;

        RectF newRect = new RectF(newLeft,newTop,newRight,newBottom);
        Log.d(TAG, "_________newRect_________ " + newRect);

        return newRect;
    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    public String convertArrayToString(ArrayList<RectF> rect){
        String output="";
        for(int i=0;i<=rect.size()-1;i++){
            output += rect.get(i).left + ", " + rect.get(i).bottom + ", "+ rect.get(i).right + ", "+ rect.get(i).top + "\n";
        }
        return output;
    }

    public void createTextFile(Context context, String sBody) {
        int idx = selectedImagePath.lastIndexOf('/');
        String imageName = selectedImagePath.substring(idx +1, selectedImagePath.length()-4) + ".txt";
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "SilverSparroText");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, imageName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void readFromText(){
        int idx = selectedImagePath.lastIndexOf('/');
        String imageName = selectedImagePath.substring(idx +1, selectedImagePath.length()-4) + "txt";
        try {
            File file = new File(imageName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
                stringBuffer.append("\n");
            }
            fileReader.close();
            Log.d(TAG, "Contents of file: " + stringBuffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//[RectF(376.90726, 850.0, 721.0, 584.2018), RectF(269.56836, 1068.0, 565.0, 817.0), RectF(374.70007, 1310.0, 680.0, 1050.0)]

}