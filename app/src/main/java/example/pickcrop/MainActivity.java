package example.pickcrop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ImageView iv;
    CardView cvSelectImage,cvSelectImage2;
    private static final int REQUEST_CODE_CHOOSE = 23;
    RecyclerView rc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv=findViewById(R.id.iv);

        rc=findViewById(R.id.rc);
        rc.setLayoutManager(new GridLayoutManager(this,3));

        cvSelectImage=findViewById(R.id.cvSelectImage);
        cvSelectImage2=findViewById(R.id.cvSelectImage2);
        cvSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .start(MainActivity.this);
            }
        });

        cvSelectImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Pix.start(MainActivity.this, Options.init().setCount(3).setRequestCode(REQUEST_CODE_CHOOSE).setExcludeVideos(true));
                Pix.start(MainActivity.this, Options.init().setCount(6).setRequestCode(REQUEST_CODE_CHOOSE).setExcludeVideos(true));
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
//            mAdapter.addImage(returnValue);
            for(int i=0;i<returnValue.size();i++)
                Log.e("/*pick:",returnValue.get(i));

            ImageAdapter imageAdapter=new ImageAdapter(MainActivity.this,returnValue);
            rc.setAdapter(imageAdapter);
//            iv.setImageBitmap(getBitmap(returnValue.get(0)));
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                iv.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("/*",error.toString());
            }
        }
    }
}

class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{
    Context context;
    ArrayList<String> returnValue;

    public ImageAdapter(Context context, ArrayList<String> returnValue) {
        this.context=context;
        this.returnValue=returnValue;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {

        try {
            ExifInterface ei = new ExifInterface(returnValue.get(position));
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.e("/*pick","orientation:"+String.valueOf(orientation));
            Bitmap bitmap=getBitmap(returnValue.get(position));
            bitmap = resizeBitmap(bitmap, 2048);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap=rotate(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap= rotate(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap= rotate(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    bitmap= flip(bitmap, true, false);
                    break;

                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    bitmap= flip(bitmap, false, true);
                    break;
            }

            holder.rcIV.setImageBitmap(bitmap);
        } catch (IOException e) {
            Log.e("/*pick",e.toString());
            e.printStackTrace();
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public Bitmap resizeBitmap(final Bitmap temp, final int size) {
        if (temp != null && size > 0) {
            int width = temp.getWidth();
            int height = temp.getHeight();
            float ratioBitmap = (float) width / (float) height;
            int finalWidth = size;
            int finalHeight = size;
            if (ratioBitmap < 1) {
                finalWidth = (int) ((float) size * ratioBitmap);
            } else {
                finalHeight = (int) ((float) size / ratioBitmap);
            }
            return Bitmap.createScaledBitmap(temp, finalWidth, finalHeight, true);
        } else {
            return temp;
        }
    }

    @Override
    public int getItemCount() {
        return returnValue.size();
    }

    private Bitmap getBitmap(String path) {
        Bitmap bitmap = null;
        try {
            File f = new File(path);
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, bmOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView rcIV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rcIV=itemView.findViewById(R.id.rcIV);
        }
    }
}

