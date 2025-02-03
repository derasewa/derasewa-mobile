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

public class ForgotPasswordActivity extends AppCompatActivity {

    Button resetPasswordButton;
    EditText emailInput;
    EditText newPasswordInput;
    TextView errorMessage;

    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailInput = findViewById(R.id.email_input);
        newPasswordInput = findViewById(R.id.new_password_input);
        resetPasswordButton = findViewById(R.id.reset_password_button);
        errorMessage = findViewById(R.id.error_message);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String newPassword = newPasswordInput.getText().toString().trim();

                validateForgotPassword(email, newPassword);
            }
        });
    }

    private void validateForgotPassword(String email, String newPassword) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("newPassword", newPassword);
        String jsonString = gson.toJson(jsonObject);

        RequestBody body = RequestBody.Companion.create(jsonString, JSON);
        Request request = new Request.Builder()
                .url(BuildConfig.SERVER_IP + "/validate-forgot-password") // Replace with your actual API endpoint
                .addHeader("x-api-key", BuildConfig.API_KEY) // Ensure API key is set correctly
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    errorMessage.setText("Request failed: " + e.getMessage());
                    errorMessage.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.e("ServerResponse", responseBody); // Log the server response

                try {
                    JsonElement jsonElement = JsonParser.parseString(responseBody);
                    if (jsonElement.isJsonObject()) {
                        JsonObject responseJson = jsonElement.getAsJsonObject();
                        String type = responseJson.has("type") ? responseJson.get("type").getAsString() : "error";
                        String message = responseJson.has("message") ? responseJson.get("message").getAsString() : "Unknown error";

                        if (response.isSuccessful() && "success".equals(type)) {
                            runOnUiThread(() -> {
                                Toast.makeText(ForgotPasswordActivity.this, "Request successful: " + message, Toast.LENGTH_SHORT).show();
                                errorMessage.setVisibility(View.GONE);

                                // Redirect to OTP Verification Activity
                                Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                                intent.putExtra("type", "change-password");
                                intent.putExtra("email", email);
                                intent.putExtra("newPassword", newPassword);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                errorMessage.setText("Request failed: " + message);
                                errorMessage.setVisibility(View.VISIBLE);
                            });
                        }
                    } else {
                        throw new JsonSyntaxException("Expected a JsonObject but was " + jsonElement.getClass());
                    }
                } catch (JsonSyntaxException e) {
                    Log.e("ForgotPasswordActivity", "Error parsing JSON", e);
                    runOnUiThread(() -> {
                        errorMessage.setText("Request failed: Invalid server response");
                        errorMessage.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }
}
