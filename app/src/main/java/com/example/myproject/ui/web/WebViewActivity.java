package com.example.myproject.ui.web;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myproject.databinding.ActivityWebViewBinding;

import java.util.Objects;

public class WebViewActivity extends AppCompatActivity {
    ActivityWebViewBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //在app中打开网址
        WebView webView=binding.webView;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(Objects.requireNonNull(getIntent().getStringExtra("Link")));

    }
}