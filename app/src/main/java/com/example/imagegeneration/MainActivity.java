package com.example.imagegeneration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.example.imagegeneration.databinding.ActivityMainBinding;
import com.google.gson.JsonObject;
import com.konfigthis.leap.client.ApiCallback;
import com.konfigthis.leap.client.ApiException;
import com.konfigthis.leap.client.Configuration;
import com.konfigthis.leap.client.Leap;
import com.konfigthis.leap.client.model.InferenceEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setup();
    }

    private Dialog getProgressDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progress_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;
    }

    private void setup() {
        binding.btnGenerate.setEnabled(false);

        progressDialog = getProgressDialog();

        binding.edPrompt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                binding.btnGenerate.setEnabled(!binding.edPrompt.getText().toString().isBlank());
            }
        });

        binding.btnGenerate.setOnClickListener(view -> {
            try {
                generateImage1(binding.edPrompt.getText().toString().trim());
            } catch (JSONException e) {
                Log.e("JSONEx", e.getMessage());
            }
        });
    }

    private void generateImage(String prompt) {
        String token = "5967d425-9b92-48e1-abeb-eaa6ba100d06";

        Configuration configuration = new Configuration();
        configuration.host = "https://api.tryleap.ai";

        configuration.token = "Bearer " + token;

        Leap client = new Leap(configuration);

        String modelId = "eab32df0-de26-4b83-a908-a83f3015e971";
        Double width = 512D; // The width of the image to use for the inference. Must be a multiple of 8. For best results use 1024x1024 for SDXL, and 512x512 for other models.
        Double height = 512D;
        Double numberOfImages = 1D;
        Double promptStrength = 7D;
        Double steps = 50D;

        try {
                client
                    .images
                    .generate(prompt, modelId)
                    .steps(steps)
                    .width(width)
                    .height(height)
                    .numberOfImages(numberOfImages)
                    .promptStrength(promptStrength)
                    .executeAsync(new ApiCallback<>() {
                        @Override
                        public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                            Log.e("onFailure", "Something went wrong!");
                            Log.e("exception", e.getMessage());
                            Log.e("exception", e.getResponseBody());
                        }

                        @Override
                        public void onSuccess(InferenceEntity result, int statusCode, Map<String, List<String>> responseHeaders) {
                            Log.e("onSuccess", "Image Generated Successfully");
                        }
                    });
        } catch (ApiException e) {
            Log.e("ApiException", e.getResponseBody());
        }
    }

    private void generateImage1(String prompt) throws JSONException {
        String modelId = "eab32df0-de26-4b83-a908-a83f3015e971";
        String token = "5967d425-9b92-48e1-abeb-eaa6ba100d06";

        OkHttpClient client = new OkHttpClient();

        JSONObject payload = new JSONObject();
        payload.put("prompt", prompt);
        payload.put("steps", 50);
        payload.put("width", 512);
        payload.put("height", 512);
        payload.put("numberOfImages", 1);
        payload.put("promptStrength", 7);
        payload.put("enhancePrompt", false);
        payload.put("restoreFaces", true);
        payload.put("upscaleBy", "x1");

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, payload.toString());

        Request request = new Request.Builder()
                .url("https://api.tryleap.ai/api/v1/images/models/" + modelId +"/inferences")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + token)
                .build();


        progressDialog.show();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                Log.e("onFailure", "Something went wrong!");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("onSuccess", "First Call executed successfully");


                if (response.isSuccessful()) {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        getImage(modelId, object.getString("id"), prompt);

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                call.cancel();
            }
        });

    }

    private void getImage(String modelId, String id, String prompt) {
        String token = "5967d425-9b92-48e1-abeb-eaa6ba100d06";
        OkHttpClient client = new OkHttpClient();

        Request secondRequest = new Request.Builder()
                .url(String.format("https://api.tryleap.ai/api/v1/images/models/%s/inferences/%s", modelId ,id))
                .get()
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + token)
                .build();

        Callback callback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                Log.e("onFailure", "Something went wrong!");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d("onSuccess", "Call executed successfully");

                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONObject responseObject = new JSONObject(body);
                        String state = responseObject.getString("state");

                        if (!state.equals("finished")) {
                            client.newCall(secondRequest).enqueue(this);
                            call.cancel();
                        }
                        else {
                            call.cancel();
                            progressDialog.dismiss();
                            JSONArray images = responseObject.getJSONArray("images");
                            Log.d("Image URL", images.getJSONObject(0).getString("uri"));

                            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                            intent.putExtra("URL", images.getJSONObject(0).getString("uri"));
                            intent.putExtra("PROMPT", prompt);
                            startActivity(intent);
                        }




                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        };



        client.newCall(secondRequest).enqueue(callback);




    }
}