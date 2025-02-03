package com.example.derasewa.HostProperty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.derasewa.R;

public class SecondStepActivity extends AppCompatActivity {

    private EditText addressInput;
    private EditText phoneNumberInput;
    private EditText priceInput;
    private EditText descriptionInput;
    private TextView errorMessage;
    private Button nextButton;

    private String title;
    private String type;
    private int numberOfRooms;
    private boolean hasBedroom;
    private boolean hasKitchen;
    private boolean hasBathroom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_step);

        addressInput = findViewById(R.id.address_input);
        phoneNumberInput = findViewById(R.id.phone_number_input);
        priceInput = findViewById(R.id.price_input);
        descriptionInput = findViewById(R.id.description_input);
        errorMessage = findViewById(R.id.error_message);
        nextButton = findViewById(R.id.next_button);

        // Retrieve the state from FirstStepActivity
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        type = intent.getStringExtra("type");
        numberOfRooms = intent.getIntExtra("numberOfRooms", 0);
        hasBedroom = intent.getBooleanExtra("hasBedroom", false);
        hasKitchen = intent.getBooleanExtra("hasKitchen", false);
        hasBathroom = intent.getBooleanExtra("hasBathroom", false);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndProceed();
            }
        });
    }

    private void validateAndProceed() {
        String address = addressInput.getText().toString().trim();
        String phoneNumber = phoneNumberInput.getText().toString().trim();
        String priceStr = priceInput.getText().toString().trim();
        double price = priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr);
        String description = descriptionInput.getText().toString().trim();

        // Local validation
        if (address.isEmpty() || address.length() < 5) {
            errorMessage.setText("Address is required and must be at least 5 characters.");
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        if (phoneNumber.isEmpty()) {
            errorMessage.setText("Phone number is required.");
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        if (price <= 0) {
            errorMessage.setText("Price must be greater than zero.");
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        if (description.isEmpty() || description.length() < 10) {
            errorMessage.setText("Description is required and must be at least 10 characters.");
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        // If validation succeeds, proceed to the next step
        proceedToNextStep(address, phoneNumber, price, description);
    }


    private void proceedToNextStep(String address, String phoneNumber, double price, String description) {
        Intent intent = new Intent(SecondStepActivity.this, ThirdStepActivity.class);
        // Pass along the state from the first step
        intent.putExtra("title", title);
        intent.putExtra("type", type);
        intent.putExtra("numberOfRooms", numberOfRooms);
        intent.putExtra("hasBedroom", hasBedroom);
        intent.putExtra("hasKitchen", hasKitchen);
        intent.putExtra("hasBathroom", hasBathroom);
        // Pass along the state from the second step
        intent.putExtra("address", address);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("price", price);
        intent.putExtra("description", description);
        startActivity(intent);
    }
}
