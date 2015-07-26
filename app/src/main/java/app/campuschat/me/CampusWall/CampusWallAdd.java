package app.campuschat.me.CampusWall;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import app.campuschat.me.MainActivity;
import app.campuschat.me.R;
import app.campuschat.me.Utility.Config;
import app.campuschat.me.Utility.UtilityClass;

public class CampusWallAdd extends Fragment {

    private ImageView picture;
    private Button sendPicture, addTextImage, topWallCancel;
    private ImageButton browseButton, picCamera;

    private ProgressDialog progressDialog;
    private String encodedString;
    private String imgPath;

    private Bitmap correctedImage;

    private final String TAG = "CampusWallAdd";

    private static int RESULT_LOAD_IMAGE = 1;
    private Canvas canvas;
    private Bitmap bitmapToSend;

    private String randomName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.campus_wall_add_layout, container, false);

        picture = (ImageView) view.findViewById(R.id.addImage);

        browseButton = (ImageButton) view.findViewById(R.id.browseButton);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.instance().sendTracker("CampusWallAdd", "Load image", "Clicked to browse images", "Button click");
                loadImagefromGallery(v);
            }
        });

        sendPicture = (Button) view.findViewById(R.id.sendPicture);
        sendPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.instance().sendTracker("CampusWallAdd", "Send image", "Clicked to send image", "Button click");
                if (correctedImage != null) {
                    if (bitmapToSend != null) {
                        correctedImage = bitmapToSend;
                    }
                    encodeImage();
                } else {
                    Toast.makeText(getActivity(), "Please add an image", Toast.LENGTH_LONG).show();
                }
            }
        });

        topWallCancel = (Button) view.findViewById(R.id.topWallCancel);
        topWallCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        addTextImage = (Button) view.findViewById(R.id.addTextImage);
        addTextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (correctedImage != null) {
                    MainActivity.instance().sendTracker("CampusWallAdd", "Text image add", "Clicked to add text to image", "Button click");
                    addTextDialog();
                } else {
                    Toast.makeText(getActivity(), "Please add an image", Toast.LENGTH_LONG).show();
                }
            }
        });

        picCamera = (ImageButton) view.findViewById(R.id.picCamera);
        picCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.instance().sendTracker("CampusWallAdd", "Load camera", "Clicked to load camera for image", "Button click");
                loadCamera(v);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
    }

    public void loadCamera(View v) {
        final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, RESULT_LOAD_IMAGE);
    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
    }

    int orientation;
    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == getActivity().RESULT_OK && null != data) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        // Get the Image from data
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };

                        // Get the cursor
                        Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);
                        // Move to first row
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imgPath = cursor.getString(columnIndex);
                        cursor.close();

                        // For larger files, we fix orientation
                        File pictureFile = new File(imgPath);
                        try {
                            orientation = resolveBitmapOrientation(pictureFile);
                        } catch (IOException e) {
//                            e.printStackTrace();
                        }

                        Bitmap resizedBitmap = resizeBitmap(640, imgPath);
                        correctedImage = applyOrientation(resizedBitmap, orientation);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        picture.setImageBitmap(correctedImage);

                        // Put file name in Async Http Post Param which will used in Php web app
                        // use fileName for path or create own
                        randomName = UtilityClass.randomString(8) + ".jpg";
                    }
                }.execute();

            } else {
//                Toast.makeText(getActivity(), "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void encodeImage() {
        progressDialog.setMessage("Sending");
        progressDialog.show();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                correctedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                uploadImage();
            }
        }.execute();
    }

    Response response;
    public void uploadImage() {
        final OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("filename", randomName)
                .add("image", encodedString).build();

        final Request request = new Request.Builder().url(Config.queryPrefix + "uploadwallimage.php").post(formBody).build();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(response.isSuccessful()) {
                    writeToDB();
                    Toast.makeText(getActivity(), "Image sent", Toast.LENGTH_LONG).show();
                } else {
                    progressDialog.hide();
                    Toast.makeText(getActivity(), "Unable to send image", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public void writeToDB() {
        final OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormEncodingBuilder()
                .add("trueid", randomName.replace(".jpg", ""))
                .add("date", UtilityClass.getCurrentDateAndTime())
                .build();

        final Request request = new Request.Builder().url(Config.queryPrefix + "writewallimage.php").post(formBody).build();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(response.isSuccessful()) {
                    // Reload campus wall upon leaving this fragment
                    MainActivity.loadOnce3 = true;
                    imageSentDialog();
                }
                progressDialog.hide();
            }
        }.execute();
    }

    public Bitmap resizeBitmap(int desiredWidth, String STRING_PATH_TO_FILE) {
        // Get the source image's dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(STRING_PATH_TO_FILE, options);

        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

        // Only scale if the source is big enough. This code is just trying to fit a image into a certain width.
        if(desiredWidth > srcWidth) {
            desiredWidth = srcWidth;
        }

        // Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
        int inSampleSize = 1;
        while(srcWidth / 2 > desiredWidth){
            srcWidth /= 2;
            srcHeight /= 2;
            inSampleSize *= 2;
        }

        float desiredScale = (float) desiredWidth / srcWidth;

        // Decode with inSampleSize
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = inSampleSize;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap sampledSrcBitmap = BitmapFactory.decodeFile(STRING_PATH_TO_FILE, options);

        // Resize
        Matrix matrix = new Matrix();
        matrix.postScale(desiredScale, desiredScale);
        Bitmap scaledBitmap = Bitmap.createBitmap(sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);
        sampledSrcBitmap = null;
        return scaledBitmap;
    }

    public void imageSentDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

        dialog.setTitle("CampusChat");
        dialog.setMessage("Image has been posted");

        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().getSupportFragmentManager().popBackStack();

            }
        });

        dialog.show();
    }

    public void addTextDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        final EditText messageText = new EditText(getActivity());
        dialog.setTitle("Picture caption");
        dialog.setMessage("Enter text");
        dialog.setView(messageText);

//        Log.e(TAG, "Image width: " + correctedImage.getWidth());
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String message = messageText.getText().toString();
                addBitmapText2(message, correctedImage);
            }
        });

        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private int resolveBitmapOrientation(File bitmapFile) throws IOException {
        ExifInterface exif = null;
        exif = new ExifInterface(bitmapFile.getAbsolutePath());
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }

    private Bitmap applyOrientation(Bitmap bitmap, int orientation) {
        int rotate = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            default:
                return bitmap;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public void addBitmapText2(String text, Bitmap bitmap) {
        // Clears any previous drawings
        if(canvas != null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }

        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(bitmap);

        int bWidth = bitmap.getWidth();
        int bHeight = (bitmap.getHeight()/4) * 3;

        // START
        TextView textView = new TextView(getActivity());
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(getResources().getColor(R.color.bg_trans));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
        textView.setTextColor(Color.WHITE);
        textView.setText(text);

        Rect bounds = new Rect();
        textView.getPaint().getTextBounds(textView.getText().toString(), 0, textView.getText().toString().length(), bounds);

        double doubleBWidth = bitmap.getWidth();
        int textWidth = (int) (Math.ceil(bounds.width()/doubleBWidth) * 30);
        textView.layout(0, 0, bitmap.getWidth(), textWidth);

        textView.setDrawingCacheEnabled(true);
        canvas.drawBitmap(textView.getDrawingCache(), 0, bHeight, null); //text box top left position 50,50
        // END

        bitmapToSend = bitmap;

        picture.setImageBitmap(bitmap);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
