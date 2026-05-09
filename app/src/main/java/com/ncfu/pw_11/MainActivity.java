package com.ncfu.pw_11;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button btnCalculate;
    private Button btnCalculateThread;
    private Button btnLoadImages;
    private ProgressBar progressBar;
    private TextView tvResult;
    private LinearLayout imagesContainer;

    private final String[] imageUrls = {
            "https://picsum.photos/400/250?random=1",
            "https://picsum.photos/400/250?random=2",
            "https://picsum.photos/400/250?random=3"
    };

    private final Random random = new Random();

    private double[] numbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnCalculateThread = findViewById(R.id.btnCalculateThread);
        btnLoadImages = findViewById(R.id.btnLoadImages);
        progressBar = findViewById(R.id.progressBar);
        tvResult = findViewById(R.id.tvResult);
        imagesContainer = findViewById(R.id.imagesContainer);

        btnCalculate.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);

            generateArray();
            String result = calculateArrayData();

            progressBar.setProgress(100);
            progressBar.setVisibility(View.GONE);
            tvResult.setText(result);

            Toast.makeText(this, "Вычисления без потока завершены", Toast.LENGTH_SHORT).show();
        });

        btnCalculateThread.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            tvResult.setText("Выполняются вычисления в фоновом потоке...");

            new Thread(() -> {
                generateArray();

                for (int i = 0; i <= 100; i += 10) {
                    final int progress = i;

                    runOnUiThread(() -> progressBar.setProgress(progress));

                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String result = calculateArrayData();

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvResult.setText(result);
                    Toast.makeText(
                            MainActivity.this,
                            "Вычисления в потоке завершены",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }).start();
        });

        btnLoadImages.setOnClickListener(v -> loadImagesInBackground());
    }

    private void generateArray() {
        numbers = new double[20];

        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = -50 + random.nextDouble() * 100;
        }
    }

    private String calculateArrayData() {
        double maxElement = numbers[0];
        int lastPositiveIndex = -1;

        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] > maxElement) {
                maxElement = numbers[i];
            }

            if (numbers[i] > 0) {
                lastPositiveIndex = i;
            }
        }

        double sumBeforeLastPositive = 0;

        if (lastPositiveIndex > 0) {
            for (int i = 0; i < lastPositiveIndex; i++) {
                sumBeforeLastPositive += numbers[i];
            }
        }

        StringBuilder arrayText = new StringBuilder();

        for (int i = 0; i < numbers.length; i++) {
            arrayText.append(String.format("%.2f", numbers[i]));

            if (i < numbers.length - 1) {
                arrayText.append("; ");
            }
        }

        return "Вариант 5\n\n"
                + "Массив:\n"
                + arrayText + "\n\n"
                + "Максимальный элемент массива: "
                + String.format("%.2f", maxElement) + "\n\n"
                + "Индекс последнего положительного элемента: "
                + lastPositiveIndex + "\n\n"
                + "Сумма элементов до последнего положительного элемента: "
                + String.format("%.2f", sumBeforeLastPositive);
    }

    private void loadImagesInBackground() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        imagesContainer.removeAllViews();

        new Thread(() -> {
            for (int i = 0; i < imageUrls.length; i++) {
                try {
                    Bitmap bitmap = loadImage(imageUrls[i]);

                    int progress = ((i + 1) * 100) / imageUrls.length;
                    int imageNumber = i + 1;

                    runOnUiThread(() -> {
                        ImageView imageView = new ImageView(MainActivity.this);
                        imageView.setImageBitmap(bitmap);
                        imageView.setAdjustViewBounds(true);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                500
                        );
                        params.setMargins(0, 0, 0, 16);
                        imageView.setLayoutParams(params);

                        imagesContainer.addView(imageView);

                        progressBar.setProgress(progress);
                        tvResult.setText("Загружено изображений: "
                                + imageNumber + " из " + imageUrls.length);
                    });

                } catch (Exception e) {
                    e.printStackTrace();

                    runOnUiThread(() -> Toast.makeText(
                            MainActivity.this,
                            "Ошибка загрузки изображения",
                            Toast.LENGTH_SHORT
                    ).show());
                }
            }

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(
                        MainActivity.this,
                        "Загрузка изображений завершена",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }).start();
    }

    private Bitmap loadImage(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoInput(true);
        connection.connect();

        InputStream inputStream = connection.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        inputStream.close();
        connection.disconnect();

        return bitmap;
    }
}