package com.example.derasewa.HostProperty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.derasewa.R;

public class FirstStepActivity extends AppCompatActivity {

    private EditText titleInput;
    private RadioGroup typeGroup;
    private EditText numberOfRoomsInput;
    private CheckBox hasBedroom;
    private CheckBox hasKitchen;
    private CheckBox hasBathroom;
    private TextView errorMessage;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_step);

        titleInput = findViewById(R.id.title_input);
        typeGroup = findViewById(R.id.type_group);
        numberOfRoomsInput = findViewById(R.id.number_of_rooms_input);
        hasBedroom = findViewById(R.id.has_bedroom);
        hasKitchen = findViewById(R.id.has_kitchen);
        hasBathroom = findViewById(R.id.has_bathroom);
        errorMessage = findViewById(R.id.error_message);
        nextButton = findViewById(R.id.next_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndProceed();
            }
        });
    }

    private void validateAndProceed() {
        String title = titleInput.getText().toString().trim();
        boolean isApartment = typeGroup.getCheckedRadioButtonId() == R.id.type_apartment;
        String numberOfRoomsStr = numberOfRoomsInput.getText().toString().trim();
        int numberOfRooms = numberOfRoomsStr.isEmpty() ? 0 : Integer.parseInt(numberOfRoomsStr);
        boolean hasBedroomChecked = hasBedroom.isChecked();
        boolean hasKitchenChecked = hasKitchen.isChecked();
        boolean hasBathroomChecked = hasBathroom.isChecked();

        // Local validation
        if (title.isEmpty() || typeGroup.getCheckedRadioButtonId() == -1 || numberOfRooms <= 0) {
            errorMessage.setText("All fields are required.");
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        // Title length validation
        if (title.length() < 3 || title.length() > 100) {
            errorMessage.setText("Title must be between 3 and 100 characters.");
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        // If validation succeeds, proceed to the next step
        proceedToNextStep(title, isApartment, numberOfRooms, hasBedroomChecked, hasKitchenChecked, hasBathroomChecked);
    }


    private void proceedToNextStep(String title, boolean isApartment, int numberOfRooms, boolean hasBedroom, boolean hasKitchen, boolean hasBathroom) {
        Intent intent = new Intent(FirstStepActivity.this, SecondStepActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("type", isApartment ? "Apartment" : "Flat");
        intent.putExtra("numberOfRooms", numberOfRooms);
        intent.putExtra("hasBedroom", hasBedroom);
        intent.putExtra("hasKitchen", hasKitchen);
        intent.putExtra("hasBathroom", hasBathroom);
        startActivity(intent);
    }
}
