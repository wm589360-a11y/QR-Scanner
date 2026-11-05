package com.WalterMusa.qrscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.WalterMusa.qrscanner.databinding.ActivityMainBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ExecutorService cameraExecutor;
    private BarcodeAnalyzer analyzer;
    private Camera camera;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    showPermissionDeniedDialog();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();
        analyzer = new BarcodeAnalyzer(this::onBarcodeFound);

        if (hasCameraPermission()) {
            startCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

                provider.unbindAll();
                camera = provider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void onBarcodeFound(Barcode barcode) {
        String value = barcode.getRawValue();
        if (value == null) {
            analyzer.resume();
            return;
        }

        String message = "Content:\n" + value
                + "\n\nType: " + typeName(barcode.getValueType())
                + "\nFormat: " + formatName(barcode.getFormat());

        new AlertDialog.Builder(this)
                .setTitle("Scan result")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setOnDismissListener(d -> analyzer.resume())
                .show();
    }

    private String typeName(int type) {
        switch (type) {
            case Barcode.TYPE_URL:     return "URL";
            case Barcode.TYPE_WIFI:    return "Wi-Fi";
            case Barcode.TYPE_PHONE:   return "Phone";
            case Barcode.TYPE_EMAIL:   return "Email";
            case Barcode.TYPE_SMS:     return "SMS";
            case Barcode.TYPE_GEO:     return "Location";
            case Barcode.TYPE_PRODUCT: return "Product";
            default:                   return "Text";
        }
    }

    private String formatName(int format) {
        switch (format) {
            case Barcode.FORMAT_QR_CODE:  return "QR Code";
            case Barcode.FORMAT_EAN_13:   return "EAN-13";
            case Barcode.FORMAT_EAN_8:    return "EAN-8";
            case Barcode.FORMAT_UPC_A:    return "UPC-A";
            case Barcode.FORMAT_CODE_128: return "Code 128";
            case Barcode.FORMAT_CODE_39:  return "Code 39";
            case Barcode.FORMAT_PDF417:   return "PDF417";
            case Barcode.FORMAT_DATA_MATRIX: return "Data Matrix";
            default:                      return "Other";
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Camera permission needed")
                .setMessage("This app needs the camera to scan codes. You can enable it in Settings.")
                .setPositiveButton("Open Settings", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}