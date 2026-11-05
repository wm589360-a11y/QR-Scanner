package com.WalterMusa.qrscanner;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {

    public interface Callback {
        void onBarcodeFound(Barcode barcode);
    }

    private final BarcodeScanner scanner = BarcodeScanning.getClient();
    private final Callback callback;
    private volatile boolean paused = false;

    public BarcodeAnalyzer(Callback callback) {
        this.callback = callback;
    }

    public void pause()  { paused = true; }
    public void resume() { paused = false; }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        if (paused || imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty() && !paused) {
                        paused = true;   // stop repeat scans until the dialog closes
                        callback.onBarcodeFound(barcodes.get(0));
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }
}