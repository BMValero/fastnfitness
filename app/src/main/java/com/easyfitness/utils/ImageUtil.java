package com.easyfitness.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.easyfitness.BuildConfig;
import com.easyfitness.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ccombes on 18/04/14.
 */

public class ImageUtil {

    static public void setThumb(ImageView mImageView, String pPath) {
        try {
            if (pPath == null || pPath.isEmpty()) return;
            File f = new File(pPath);
            if(!f.exists() || f.isDirectory()) return;

            // Get the dimensions of the View
            float targetW = 128;//mImageView.getWidth();
            //float targetH = mImageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pPath, bmOptions);
            float photoW = bmOptions.outWidth;
            float photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = (int)(photoW/targetW); //Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
            mImageView.setImageBitmap(bitmap);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public String saveThumb(String pPath) {
        if (pPath == null || pPath.isEmpty()) return null;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pPath, bmOptions);
        float photoW = bmOptions.outWidth;
        float photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        float scaleFactor = photoW/photoH; //Math.min(photoW/targetW, photoH/targetH);

        Bitmap ThumbImage =null;
        //if (photoW < photoH)
        ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(pPath), 128, (int)(128/scaleFactor));
        //else
        //	ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(pPath), (int)(96/scaleFactor), 96);

        // extract path without the .jpg
        String nameOfOutputImage = pPath.substring(pPath.lastIndexOf('/') + 1, pPath.lastIndexOf('.'));
        String pathOfOutputFolder = pPath.substring(0, pPath.lastIndexOf('/'));
        File pathThumbFolder = new File(pathOfOutputFolder + "/thumb/");
        if (!pathThumbFolder.exists()) {
            pathThumbFolder.mkdirs();
        }
        String pathOfThumbImage = pathOfOutputFolder + "/thumb/" + nameOfOutputImage + "_TH.jpg";

        try {
            FileOutputStream out = new FileOutputStream(pathOfThumbImage);
            ThumbImage.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (Exception e) {
            Log.e("Image", e.getMessage(), e);
        }

        return pathOfThumbImage;
    }

    /**
     * Return path to the thumb of a picture if it is not already a thumb.
     * If necessary, it creates the thumb file.
     *
     * @param pPath Path to the full size picture
     * @return path to the thumb file
     */
    public String getThumbPath(String pPath) {
        if (pPath == null || pPath.isEmpty()) return null;
        // extract path without the .jpg
        String nameOfOutputImage = "";
        nameOfOutputImage = pPath.substring(pPath.lastIndexOf('/') + 1, pPath.lastIndexOf('.'));
        String pathOfOutputFolder = pPath.substring(0, pPath.lastIndexOf('/'));

        // If it is already a thumb do nothing
        if (nameOfOutputImage.substring(nameOfOutputImage.length() - 3) == "_TH") {
            return pPath;
            // else check if it already exists
        } else {
            // extract path without the .jpg
            String pathOfThumbImage = "";
            pathOfThumbImage = pathOfOutputFolder + "/thumb/" + nameOfOutputImage + "_TH.jpg";
            File f = new File(pathOfThumbImage);
            if (!f.exists())
                return saveThumb(pPath); // create thumb file
            else {
                return pathOfThumbImage;
            }
        }
    }


    Fragment mF=null;
    String mFilePath = null;

    public String getFilePath() {
        return mFilePath;
    }

    public boolean CreatePhotoSourceDialog(Fragment pF) {

        mF = pF;

        String[] optionListArray = new String[2];
        optionListArray[0] = mF.getResources().getString(R.string.camera);
        optionListArray[1] = mF.getResources().getString(R.string.gallery);
        //profilListArray[2] = "Remove Image";

        requestPermissionForWriting(pF);

        AlertDialog.Builder itemActionbuilder = new AlertDialog.Builder(mF.getActivity());
        itemActionbuilder.setTitle("").setItems(optionListArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ListView lv = ((AlertDialog)dialog).getListView();

                switch (which) {
                    // Galery
                    case 1 :
                        getGaleryPict(mF);
                        break;
                    // Camera
                    case 0:
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(mF.getContext(), mF);
                        break;
                    case 2: // Delete picture

                        break;
                    // Camera
                    default:
                }
            }
        });
        itemActionbuilder.show();

        return true;
    }

    static public void setPic(ImageView mImageView, String pPath) {
        try {
            if (pPath == null) return;
            File f = new File(pPath);
            if(!f.exists() || f.isDirectory()) return;

            // Get the dimensions of the View
            int targetW = mImageView.getWidth();
            int targetH = mImageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = photoW/targetW; //Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
            mImageView.setImageBitmap(bitmap);

            //mImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            mImageView.setAdjustViewBounds(true);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_PICK_GALERY_PHOTO = 2;
    public static final int REQUEST_DELETE_IMAGE = 3;

    private void dispatchTakePictureIntent(Fragment pF) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mF.getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(pF);
                mFilePath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(mF.getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                mF.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void getGaleryPict(Fragment pF) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        pF.startActivityForResult(photoPickerIntent, REQUEST_PICK_GALERY_PHOTO);
    }

    public void galleryAddPic(Fragment pF, String file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        pF.getActivity().sendBroadcast(mediaScanIntent);
    }

    private File createImageFile(Fragment pF) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = null;

        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return null;
        } else {
            //We use the FastNFitness directory for saving our .csv file.
            storageDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/DCIM/");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
        }
        /*File storageDir =  pF.getActivity().getExternalFilesDir(
                Environment.DIRECTORY_DCIM);*/
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    private void requestPermissionForWriting(Fragment pF) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(pF.getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(pF.getActivity(),
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(pF.getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }


    public File moveFile(File file, File dir) throws IOException {
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            file.delete();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

        return newFile;
    }
}
