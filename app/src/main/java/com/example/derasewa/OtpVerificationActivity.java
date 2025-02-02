package com.example.derasewa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class OtpVerificationActivity extends AppCompatActivity {

    EditText otpInput;
    Button verifyOtpButton;
    TextView errorMessage;

    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        otpInput = findViewById(R.id.otp_input);
        verifyOtpButton = findViewById(R.id.verify_otp_button);
        errorMessage = findViewById(R.id.error_message);

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpInput.getText().toString().trim();

                // Get data from intent
                Intent intent = getIntent();
                String type = intent.getStringExtra("type");

                if ("register-account".equals(type)) {
                    String firstName = intent.getStringExtra("firstName");
                    String lastName = intent.getStringExtra("lastName");
                    String email = intent.getStringExtra("email");
                    String password = intent.getStringExtra("password");
                    boolean usingReferralCode = intent.getBooleanExtra("usingReferralCode", false);
                    String referralCode = intent.getStringExtra("referralCode");
                    verifyOtpForAccountRegistration(firstName, lastName, email, password, otp, usingReferralCode, referralCode);
                }
            }
        });
    }

    private void verifyOtpForAccountRegistration(String firstName, String lastName, String email, String password, String otp, boolean usingReferralCode, String referralCode) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("firstName", firstName);
        jsonObject.addProperty("lastName", lastName);
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("password", password);
        jsonObject.addProperty("otp", otp);
        jsonObject.addProperty("usingReferralCode", usingReferralCode);
        jsonObject.addProperty("referralCode", referralCode);
        String jsonString = gson.toJson(jsonObject);

        RequestBody body = RequestBody.Companion.create(jsonString, JSON);
        Request request = new Request.Builder()
                .url(BuildConfig.SERVER_IP + "/register-user-account") // Replace with your actual API endpoint
                .addHeader("x-api-key", BuildConfig.API_KEY) // Ensure API key is set correctly
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    errorMessage.setText("OTP verification failed: " + e.getMessage());
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
                                Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_SHORT).show();
                                errorMessage.setVisibility(View.GONE);
                                // Redirect to login or main activity
                                Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                errorMessage.setText("OTP verification failed: " + message);
                                errorMessage.setVisibility(View.VISIBLE);
                            });
                        }
                    } else {
                        throw new JsonSyntaxException("Expected a JsonObject but was " + jsonElement.getClass());
                    }
                } catch (JsonSyntaxException e) {
                    Log.e("OtpVerificationActivity", "Error parsing JSON", e);
                    runOnUiThread(() -> {
                        errorMessage.setText("OTP verification failed: Invalid server response");
                        errorMessage.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }
}
