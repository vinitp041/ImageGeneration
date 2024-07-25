package com.example.imagegeneration;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.imagegeneration.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {

    ActivityResultBinding binding;

    String imageURL, prompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prompt = getIntent().getStringExtra("PROMPT");
        imageURL = getIntent().getStringExtra("URL");

        setup();
    }

    private void setup() {
        binding.cardBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        binding.textPrompt.setText(prompt);

        Glide.with(this)
                .load(imageURL)
                .into(binding.imgResult);

        binding.btnSave.setOnClickListener(view -> saveImage());
    }

    private void saveImage() {
        String fullFileName = System.currentTimeMillis() + ".png";
        Toast.makeText(this, "Downloading Image...", Toast.LENGTH_LONG).show();

        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        DownloadManager.Request downlaodRequest = new DownloadManager.Request(Uri.parse(imageURL))
                .setTitle("Image Downloaded Successfully!")
                .setDescription("")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,  "/Image Generated/" + fullFileName);

        manager.enqueue(downlaodRequest);
    }


}