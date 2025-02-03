package com.example.derasewa.HostProperty;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.derasewa.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ThirdStepActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView selectedImage;
    private TextView errorMessage;
    private Button nextButton;
    private Uri imageUri;
    private List<String> base64Images = new ArrayList<>();

    private String title;
    private String type;
    private int numberOfRooms;
    private boolean hasBedroom;
    private boolean hasKitchen;
    private boolean hasBathroom;
    private String address;
    private String phoneNumber;
    private double price;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_step);

        selectedImage = findViewById(R.id.selected_image);
        errorMessage = findViewById(R.id.error_message);
        nextButton = findViewById(R.id.next_button);

        // Retrieve the state from previous activities
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        type = intent.getStringExtra("type");
        numberOfRooms = intent.getIntExtra("numberOfRooms", 0);
        hasBedroom = intent.getBooleanExtra("hasBedroom", false);
        hasKitchen = intent.getBooleanExtra("hasKitchen", false);
        hasBathroom = intent.getBooleanExtra("hasBathroom", false);
        address = intent.getStringExtra("address");
        phoneNumber = intent.getStringExtra("phoneNumber");
        price = intent.getDoubleExtra("price", 0);
        description = intent.getStringExtra("description");

        findViewById(R.id.select_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndProceed();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    convertToBase64AndAddToList(imageUri);
                }
            } else if (data.getData() != null) {
                imageUri = data.getData();
                convertToBase64AndAddToList(imageUri);
            }
        }
    }

    private void convertToBase64AndAddToList(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            selectedImage.setImageBitmap(bitmap);
            selectedImage.setVisibility(View.VISIBLE);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
            base64Images.add(base64Image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void validateAndProceed() {
        if (base64Images.isEmpty()) {
            errorMessage.setText("Property images are required.");
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        // If validation succeeds, proceed to the next step
        proceedToNextStep(base64Images);
    }

    private void proceedToNextStep(List<String> base64Images) {
        Intent intent = new Intent(ThirdStepActivity.this, FourthStepActivity.class);
        // Pass along the state from the previous steps
        intent.putExtra("title", title);
        intent.putExtra("type", type);
        intent.putExtra("numberOfRooms", numberOfRooms);
        intent.putExtra("hasBedroom", hasBedroom);
        intent.putExtra("hasKitchen", hasKitchen);
        intent.putExtra("hasBathroom", hasBathroom);
        intent.putExtra("address", address);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("price", price);
        intent.putExtra("description", description);
        // Pass along the state from the third step
        intent.putStringArrayListExtra("base64Images", new ArrayList<>(base64Images));
        startActivity(intent);
    }
}