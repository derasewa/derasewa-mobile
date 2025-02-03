package com.example.derasewa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    EditText emailInput;
    EditText passwordInput;
    TextView errorMessage;
    TextView registerNowLink;
    TextView forgotPasswordLink;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        errorMessage = findViewById(R.id.error_message);
        registerNowLink = findViewById(R.id.register_now_link);
        forgotPasswordLink = findViewById(R.id.forgot_password_link);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                login(email, password);
            }
        });

        registerNowLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void login(String email, String password) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("password", password);
        String jsonString = gson.toJson(jsonObject);

        RequestBody body = RequestBody.Companion.create(jsonString, JSON); // Use RequestBody.Companion.create()
        Request request = new Request.Builder()
                .url(BuildConfig.SERVER_IP + "/login") // Use BuildConfig to get server IP
                .addHeader("x-api-key", BuildConfig.API_KEY) // Use BuildConfig to access API key
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    errorMessage.setText("Login failed: " + e.getMessage());
                    errorMessage.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                try {
                    // Parse the response directly with Gson
                    JsonElement jsonElement = JsonParser.parseString(responseBody);
                    if (jsonElement.isJsonObject()) {
                        JsonObject responseJson = jsonElement.getAsJsonObject();
                        String type = responseJson.get("type").getAsString();
                        String message = responseJson.get("message").getAsString();
                        JsonElement payload = responseJson.get("payload");

                        if (response.isSuccessful() && "success".equals(type)) {
                            String jwtToken = payload.getAsString();

                            // Save JWT token and isLoggedIn flag in SharedPreferences
                            getSharedPreferences("userPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("jwtToken", jwtToken)
                                    .putBoolean("isLoggedIn", true) // Set isLoggedIn to true
                                    .apply();

                            runOnUiThread(() -> {
                                Toast.makeText(LoginActivity.this,  message, Toast.LENGTH_SHORT).show();
                                errorMessage.setText(""); // Clear the error message
                                errorMessage.setVisibility(View.GONE);

                                // Navigate to MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                errorMessage.setText("Login failed: " + message);
                                errorMessage.setVisibility(View.VISIBLE);
                            });
                        }
                    } else {
                        throw new JsonSyntaxException("Expected a JsonObject but was " + jsonElement.getClass());
                    }
                } catch (JsonSyntaxException e) {
                    Log.e("LoginActivity", "Error parsing JSON", e);
                    runOnUiThread(() -> {
                        errorMessage.setText("Login failed: Invalid server response");
                        errorMessage.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }
}