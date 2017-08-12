package com.project.ada.silversparro;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.project.ada.silversparro.R.id.save;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity implements View.OnClickListener {


    private DrawableView drawbleView;
    private PhotoView touchImageView;

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

    private ArrayList<RectF> screenRectF = new ArrayList<>();
    private ArrayList<RectF> savedScreenRectF = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO:if the corresponding text file exists, display the saved boxes

        drawbleView = (DrawableView) findViewById(R.id.drawble_view);
        touchImageView = (PhotoView) findViewById(R.id.zoom_iv);

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
                screenRectF.add(new RectF(rect.left,rect.top,rect.right,rect.bottom));
                System.out.println("-------_________screenRectF__________-------- "+ screenRectF);
                //iteration over only those boxes which are saved.
                for(int i=0; i<drawbleView.savedRectF.size(); i++) {
                    System.out.println("-------------loop working for savedRectF.get(i)--------------- " + drawbleView.savedRectF.get(i));
                    RectF newRect = originalCoordinates(rect,savedScreenRectF.get(i), drawbleView.savedRectF.get(i), drawbleView.savedScales.get(i),touchImageView.getScale());
                    drawbleView.changingRectF.set(i,newRect);
                    drawbleView.rect = newRect;
                }
                
                for(int i=drawbleView.allRectF.size()-1;i>=0;i--){
                    RectF r = drawbleView.allRectF.get(i);
                    if(!drawbleView.changingRectF.contains(r)){
                        drawbleView.allRectF.remove(r);
                    }
                    else {break;}
                }
                System.out.println(drawbleView.allRectF);

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
        System.out.println("################# onClick #################");
//        System.out.println("___________touchImageView.getScale()__________ " + touchImageView.getScale());
//        RectF rect = displayRect();
//        System.out.println("_________________touchImageView.getTranslationY() " + touchImageView.getTranslationY() + "____________");
//        System.out.println("___________rect______" + rect + "____________");
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
                if (drawbleView.savedRectF.contains(drawbleView.rect)) {
                    break;
                } else {
                    drawbleView.onClickUndo();
//                    drawbleView.rect.setEmpty();
                    if(drawbleView.allRectF.size()!=0) {
                        drawbleView.rect = drawbleView.allRectF.get(drawbleView.allRectF.size()-1);
//                        drawbleView.rect = drawbleView.allRectF.get(drawbleView.allRectF.size() - 1);       //sets rect to the last rect which was made before the current rect.
                    }                                                                                        // allRectF stores all the rect (even those which are not saved)
                    else {drawbleView.rect.setEmpty();}
//                    System.out.println("-------allRectF------- " + drawbleView.allRectF);
                }
//                System.out.println("drawbleView.rect ============= " + drawbleView.rect);
//                System.out.println("drawbleView.savedRectF ============ " + drawbleView.savedRectF);
                break;

            case R.id.gallery:
                drawbleView.onClickReset();
                loadImagefromGallery();
                System.out.println("************************" + Environment.getExternalStorageDirectory());
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
                    int index = screenRectF.size()-1;
                    RectF r = screenRectF.get(index);
                    savedScreenRectF.add(r);
                    System.out.println("-------------------------rect to be added-------------" + r);
                    System.out.println("-------------------------screenRectF------------------------ " + screenRectF);
                    System.out.println("-------------------------savedScreenRectF--------------------------- " + savedScreenRectF);
                    System.out.println("-------------------------savedRectF------------------------ " + drawbleView.savedRectF);
                }
                break;
            
            case R.id.reset:
                drawbleView.onClickReset();
                savedScreenRectF.removeAll(savedScreenRectF);
                screenRectF.removeAll(screenRectF);
                System.out.println("-------------------------screenRectF------------------------ " + screenRectF);
                System.out.println("-------------------------savedScreenRectF--------------------------- " + savedScreenRectF);
                break;

            case R.id.done:
                ArrayList<RectF> originalCoordinates = new ArrayList<>();
                for(int i=0;i<=drawbleView.savedRectF.size()-1;i++){
                    System.out.println("DONE LOOP WORKING");
                    RectF rect = originalCoordinates(screenRectF.get(0),savedScreenRectF.get(i),drawbleView.savedRectF.get(i),drawbleView.savedScales.get(i),1);
                    System.out.println("originalRect ======"+rect);
                    originalCoordinates.add(rect);
                    System.out.println("originalCoordinates ======"+originalCoordinates);
                }
                String body = convertArrayToString(originalCoordinates);
                System.out.println("body---------------------> " + body.length());
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
        System.out.println(filepath);
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



    public RectF originalCoordinates(RectF currentScreenDimension, RectF oldScreenDimension, RectF box, float oldScale, float newScale){

        float diffLeft = box.left - oldScreenDimension.left;
//        System.out.println("oldScreenDimension============ " + oldScreenDimension);
//        System.out.println("diffLeft============ " + diffLeft);
        float diffRight = box.right -oldScreenDimension.right;
        float diffTop = box.top - oldScreenDimension.top;
        float diffBottom = box.bottom - oldScreenDimension.bottom;


        float newLeft = diffLeft * newScale/oldScale + currentScreenDimension.left;
//        System.out.println("newScale/oldScale ============ " + newScale/oldScale);
//        System.out.println("currentScreenDimension.left ============ " + currentScreenDimension);
//        System.out.println("newLeft ============ " + newLeft);
        float newRight = diffRight * newScale/oldScale + currentScreenDimension.right;
        float newTop = diffTop * newScale/oldScale + currentScreenDimension.top;
        float newBottom = diffBottom * newScale/oldScale + currentScreenDimension.bottom ;

        RectF newRect = new RectF(newLeft,newTop,newRight,newBottom);
        System.out.println("_________newRect_________ " + newRect);

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
            System.out.println("Contents of file:");
            System.out.println(stringBuffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//[RectF(376.90726, 850.0, 721.0, 584.2018), RectF(269.56836, 1068.0, 565.0, 817.0), RectF(374.70007, 1310.0, 680.0, 1050.0)]

}