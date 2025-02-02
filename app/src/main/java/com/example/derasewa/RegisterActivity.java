package com.example.derasewa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    Button registerButton;
    EditText firstNameInput;
    EditText lastNameInput;
    EditText emailInput;
    EditText passwordInput;
    CheckBox useReferralCodeCheckbox;
    EditText referralCodeInput;
    TextView errorMessage;

    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstNameInput = findViewById(R.id.first_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        useReferralCodeCheckbox = findViewById(R.id.use_referral_code_checkbox);
        referralCodeInput = findViewById(R.id.referral_code_input);
        registerButton = findViewById(R.id.register_button);
        errorMessage = findViewById(R.id.error_message);

        useReferralCodeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                referralCodeInput.setVisibility(View.VISIBLE);
            } else {
                referralCodeInput.setVisibility(View.GONE);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = firstNameInput.getText().toString();
                String lastName = lastNameInput.getText().toString();
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                boolean usingReferralCode = useReferralCodeCheckbox.isChecked();
                String referralCode = referralCodeInput.getText().toString();

                validateAccount(firstName, lastName, email, password, usingReferralCode, referralCode);
            }
        });
    }

    private void validateAccount(String firstName, String lastName, String email, String password, boolean usingReferralCode, String referralCode) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("firstName", firstName);
        jsonObject.addProperty("lastName", lastName);
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("password", password);
        jsonObject.addProperty("usingReferralCode", usingReferralCode);
        jsonObject.addProperty("referralCode", referralCode);
        String jsonString = gson.toJson(jsonObject);

        RequestBody body = RequestBody.Companion.create(jsonString, JSON);
        Request request = new Request.Builder()
                .url(BuildConfig.SERVER_IP + "/validate-user-account") // Replace with your actual API endpoint
                .addHeader("x-api-key", BuildConfig.API_KEY) // Ensure API key is set correctly
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    errorMessage.setText("Registration failed: " + e.getMessage());
                    errorMessage.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                try {
                    JsonElement jsonElement = JsonParser.parseString(responseBody);
                    if (jsonElement.isJsonObject()) {
                        JsonObject responseJson = jsonElement.getAsJsonObject();
                        String type = responseJson.has("type") ? responseJson.get("type").getAsString() : "error";
                        String message = responseJson.has("message") ? responseJson.get("message").getAsString() : "Unknown error";

                        if (response.isSuccessful() && "success".equals(type)) {
                            runOnUiThread(() -> {
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                errorMessage.setVisibility(View.GONE);

                                Intent intent = new Intent(RegisterActivity.this, OtpVerificationActivity.class);

                                intent.putExtra("firstName", firstName);
                                intent.putExtra("lastName", lastName);
                                intent.putExtra("email", email);
                                intent.putExtra("password", password);
                                intent.putExtra("usingReferralCode", usingReferralCode);
                                intent.putExtra("referralCode", referralCode);
                                intent.putExtra("type", "register-account");
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                errorMessage.setText("Registration failed: " + message);
                                errorMessage.setVisibility(View.VISIBLE);
                            });
                        }
                    } else {
                        throw new JsonSyntaxException("Expected a JsonObject but was " + jsonElement.getClass());
                    }
                } catch (JsonSyntaxException e) {
                    Log.e("RegisterActivity", "Error parsing JSON", e);
                    runOnUiThread(() -> {
                        errorMessage.setText("Registration failed: Invalid server response");
                        errorMessage.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }
}