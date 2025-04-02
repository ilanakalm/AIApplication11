package com.example.aiapplication1;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.util.concurrent.FutureCallback;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class ImgActivity extends AppCompatActivity {

    private ImageButton imagebtn;
    private Button analyzeBtn;
    private EditText etPrompt;
    private TextView tvGemini;
    ActivityResultLauncher<Uri> arlBig;
    ActivityResultLauncher<String> arlFromGallery;
    Uri imageUri;

    private Bitmap capturedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_img);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imagebtn = findViewById(R.id.imageBtn);
        analyzeBtn = findViewById(R.id.analyseBtn);
        etPrompt = findViewById(R.id.etPrompt);
        tvGemini = findViewById(R.id.tvGemini);

        // permitions for camera from user
        ActivityCompat.requestPermissions(ImgActivity.this,
                new String[]{android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        imagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ImgActivity.this);
                builder.setTitle("בחר תמונה");
                String[] options = {"צלם", "גלריה", "יציאה"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // פעולה עבור "צלם"
                                try{
                                    imageUri=createUri();
                                }catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                arlBig.launch(imageUri);
                                break;
                            case 1:
                                // פעולה עבור "גלריה"
                                // פותח תיקיית מכשיר בה יש כל קבצי תמונה מכל הסוגים
                                arlFromGallery.launch("image/*");
                                break;
                            case 2:
                                // פעולה עבור "יציאה"
                                dialog.dismiss();
                                break;
                        }
                    }
                });

                builder.show();

            }
        });

        // צילום תמונה בגודל מלא - נשמרת בזיכרון של הטלפון
        // בצילום תמונה בגודל אמיתי מתבצע צילום, שמירה בזיכרון ורק אז הצגה עג המסך
        arlBig=registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        try {
                            if (result) {
                                capturedImage = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                int angle = 90;
                                Bitmap sourceBitmap = capturedImage;
                                Matrix matrix = new Matrix();
                                matrix.postRotate(angle);
                                Bitmap rotatedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
                                capturedImage = resizeImageByPercentage(rotatedBitmap, 0.5f);
                                imagebtn.setImageBitmap(capturedImage);
//תמונהסיבוב
                            }
                        } catch (Exception e) {
                            Toast.makeText(ImgActivity.this,
                                    "Photo Not found!!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // קבלת תיקיית תמונות בחירת תמונה והצבתה בתוך imagebtn
        arlFromGallery=registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result!=null)  {
                            Toast.makeText(ImgActivity.this, "Gallery",
                                    Toast.LENGTH_SHORT).show();
                            // המרת התמונה ל-Bitmap
                            try {
                                capturedImage = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), result);
                                capturedImage = resizeImageByPercentage(capturedImage, 0.5f);
                                imagebtn.setImageBitmap(capturedImage);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
        );


        analyzeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog pd = new ProgressDialog(ImgActivity.this);
                pd.setTitle("Connecting");
                pd.setMessage("Wait to Gemini...");
                pd.show();
                String prompt = etPrompt.getText().toString().trim();
                GeminiManager.getInstance().sendMessageWithPhoto(
                        prompt, capturedImage, new FutureCallback<String>() {
                            @Override
                            public void onSuccess(String s) {
                                pd.dismiss();
                                tvGemini.setText(s);
                            }
                            @Override
                            public void onFailure(Throwable throwable) {
                                tvGemini.setText("Error: " + throwable.getMessage());
                            }
                        });
            }
        });

    }

    public static Bitmap resizeImageByPercentage(Bitmap sourceBitmap, float percentage) {
        int originalWidth = sourceBitmap.getWidth();
        int originalHeight = sourceBitmap.getHeight();
        int newWidth = (int) (originalWidth * percentage);
        int newHeight = (int) (originalHeight * percentage);

        // חישוב גודל מקסימלי למסך
        int maxDimension = 800; // גודל מקסימלי למסך
        float scale = Math.min(1.0f, (float) maxDimension / Math.max(newWidth, newHeight));

        newWidth = (int) (newWidth * scale);
        newHeight = (int) (newHeight * scale);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(sourceBitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }

    private Uri createUri() throws IOException {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String currentDate = day + "_" + (month+1) + "_" + year +
                "_" + hour + ":" + minute + ":" + second;
        //יצירת שם תמונה עם תאריך ניתן גם להוסיף מספר count
        String imageFileName = "MyAppImage_" + currentDate;
        //count++;
        //this line for real phone
        //File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //this line for emulator
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir     /* directory */
        );
        // אם מספר תמונה קיים יוצרים מספר חדש
        /*while (image.exists()) {
            image = new File(storageDir, imageFileName + "_" + count + ".jpg");
            count++;
        }*/
        return FileProvider.getUriForFile(getApplicationContext(),
                "com.example.myaiapp.fileProvider",
                image);
    }
}