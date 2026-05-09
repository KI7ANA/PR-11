<div align="center">

# Отчёт

</div>

<div align="center">

## Практическая работа №11

</div>

<div align="center">

## Многопоточность в Android. Асинхронная загрузка данных

</div>

**Выполнил:**  
Ткачев Сергей Юрьевич  
**Курс:** 2  
**Группа:** ИНС-б-о-24-2  
**Направление:** ИПИНЖ (Институт перспективной инженерии)  
**Профиль:** Информационные системы и технологии  

---

### Цель работы

Изучить принципы многопоточного программирования в Android. Научиться выполнять длительные операции в фоновых потоках, чтобы избежать блокировки пользовательского интерфейса. Освоить способы обновления UI из фоновых потоков, а также реализовать асинхронную загрузку изображений из интернета с отображением прогресса выполнения.

---

### Ход работы

#### Задание 1: Создание проекта и подготовка интерфейса

1. Был открыт Android Studio и создан новый проект с шаблоном **Empty Views Activity**.
2. Проекту было дано имя `PW_11`.
3. В качестве языка программирования был выбран **Java**.
4. Package name проекта был задан как:

```text
com.ncfu.pw_11
```

5. В файл `AndroidManifest.xml` было добавлено разрешение для доступа к интернету, так как в приложении выполняется загрузка изображений по URL.

#### Листинг 1. Содержимое файла `AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.PW_11">

        <activity
            android:name=".MainActivity"
            android:exported="true">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

        </activity>

    </application>

</manifest>
```

6. В файле `activity_main.xml` был создан интерфейс приложения.
7. На экран были добавлены:
- кнопка **Вычислить без потока**;
- кнопка **Вычислить в фоновом потоке**;
- кнопка **Загрузить изображения**;
- горизонтальный `ProgressBar`;
- `TextView` для отображения результата вычислений;
- `LinearLayout` для динамического добавления загруженных изображений.

#### Листинг 2. Содержимое файла `activity_main.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center_horizontal">

        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Практическая работа №11"
            android:textSize="24sp"
            android:textStyle="bold"
            android:gravity="center"
            android:layout_marginBottom="16dp" />

        <Button
            android:id="@+id/btnCalculate"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Вычислить без потока"
            android:layout_marginBottom="8dp" />

        <Button
            android:id="@+id/btnCalculateThread"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Вычислить в фоновом потоке"
            android:layout_marginBottom="8dp" />

        <Button
            android:id="@+id/btnLoadImages"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Загрузить изображения"
            android:layout_marginBottom="16dp" />

        <ProgressBar
            android:id="@+id/progressBar"
            style="?android:attr/progressBarStyleHorizontal"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="100"
            android:progress="0"
            android:visibility="gone"
            android:layout_marginBottom="16dp" />

        <TextView
            android:id="@+id/tvResult"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Результат появится здесь"
            android:textSize="16sp"
            android:layout_marginBottom="16dp" />

        <LinearLayout
            android:id="@+id/imagesContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />

    </LinearLayout>

</ScrollView>
```

<div align="center">

<img width="374" height="730" alt="Снимок экрана 2026-05-09 194929" src="https://github.com/user-attachments/assets/8d087bb0-f8f1-4d05-9f92-cbd82dceb9f6" />

*Рисунок 1. Главный экран приложения*

</div>

---

#### Задание 2: Демонстрация вычислений без фонового потока

1. В приложении была добавлена кнопка **Вычислить без потока**.
2. При нажатии на кнопку выполняется генерация массива случайных вещественных чисел.
3. После этого выполняются вычисления согласно варианту 5.
4. Результат отображается в `TextView`.

Согласно варианту 5 необходимо вычислить:

- максимальный элемент массива;
- сумму элементов массива, расположенных до последнего положительного элемента.

#### Листинг 3. Фрагмент кода вычисления результата

```java
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
```

<div align="center">

<img width="365" height="730" alt="Снимок экрана 2026-05-09 194944" src="https://github.com/user-attachments/assets/8df9b4e4-4578-4eda-bed7-bad5e50d3d09" />

*Рисунок 2. Результат вычислений без использования фонового потока*

</div>

---

#### Задание 3: Выполнение вычислений в фоновом потоке

1. В приложении была добавлена кнопка **Вычислить в фоновом потоке**.
2. При нажатии на кнопку вычисления запускаются в отдельном потоке с помощью `new Thread()`.
3. На время выполнения вычислений отображается `ProgressBar`.
4. Для обновления интерфейса из фонового потока используется метод `runOnUiThread()`.
5. После завершения вычислений результат выводится в `TextView`.

#### Листинг 4. Фрагмент кода выполнения вычислений в фоновом потоке

```java
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
```

<div align="center">

<img width="327" height="247" alt="Снимок экрана 2026-05-09 195515" src="https://github.com/user-attachments/assets/6b5a4167-ed3f-4f34-ab3a-bca18fbcae84" />

*Рисунок 3. Выполнение вычислений в фоновом потоке с отображением ProgressBar*

</div>

<div align="center">

<img width="363" height="735" alt="Снимок экрана 2026-05-09 195008" src="https://github.com/user-attachments/assets/2b474051-4eae-4142-82de-2cc763b8c85b" />

*Рисунок 4. Результат вычислений после завершения фонового потока*

</div>

---

#### Задание 4: Загрузка изображений из интернета

1. В приложении была добавлена кнопка **Загрузить изображения**.
2. Для загрузки изображений был создан массив URL-адресов.
3. Загрузка изображений выполняется последовательно в фоновом потоке.
4. После загрузки каждого изображения обновляется общий прогресс.
5. Загруженные изображения добавляются на экран динамически в `LinearLayout`.
6. Для загрузки используется `HttpURLConnection`, а для преобразования потока данных в изображение используется `BitmapFactory`.

#### Листинг 5. Фрагмент кода загрузки изображений

```java
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
```

<div align="center">

<img width="362" height="738" alt="Снимок экрана 2026-05-09 195044" src="https://github.com/user-attachments/assets/536dabd0-8f32-4970-a402-4e25442a4e18" />

*Рисунок 5. Результат загрузки изображений из интернета*

</div>

---

#### Задание 5: Полный код MainActivity.java

В файле `MainActivity.java` была реализована вся логика приложения: вычисления без потока, вычисления в фоновом потоке, обновление `ProgressBar`, загрузка изображений из интернета и динамическое добавление изображений на экран.

#### Листинг 6. Код файла `MainActivity.java`

```java
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
```

---

#### Задание 6: Тестирование приложения

1. Приложение было запущено на эмуляторе.
2. Проверена работа кнопки **Вычислить без потока**.
3. Проверена работа кнопки **Вычислить в фоновом потоке**.
4. Проверено отображение `ProgressBar` во время выполнения фоновой операции.
5. Проверена загрузка изображений из интернета.
6. После загрузки изображения были динамически добавлены на экран приложения.

---

### Вывод

В результате выполнения практической работы были изучены принципы многопоточного программирования в Android. Было реализовано приложение, в котором длительные операции выполняются как в основном потоке, так и в фоновом потоке.

В самостоятельной части по варианту 5 был создан одномерный массив вещественных чисел. Для него были вычислены максимальный элемент массива и сумма элементов, расположенных до последнего положительного элемента. Вычисления были реализованы в фоновом потоке с использованием `Thread`, а результат выводился на экран через обновление пользовательского интерфейса.

Также была реализована асинхронная загрузка изображений из интернета. Изображения загружаются в фоновом потоке, прогресс отображается с помощью `ProgressBar`, а сами изображения добавляются на экран динамически.

Таким образом, цель практической работы была достигнута.

---

### Ответы на контрольные вопросы

1. **Что такое главный UI-поток? Почему нельзя выполнять длительные операции в нём?**

   Главный UI-поток — это основной поток приложения, который отвечает за отображение интерфейса и обработку действий пользователя. В нём выполняется обновление элементов экрана, обработка нажатий кнопок и других событий.

   Длительные операции нельзя выполнять в главном потоке, потому что интерфейс перестанет реагировать на действия пользователя. Если операция занимает много времени, приложение может зависнуть.

---

2. **Что такое ANR? При каких условиях возникает?**

   `ANR` означает `Application Not Responding`, то есть «приложение не отвечает».

   Такое состояние возникает, когда приложение слишком долго не отвечает на действия пользователя. Например, если в главном потоке выполняется тяжёлое вычисление или длительная загрузка данных, Android может показать системное окно с сообщением, что приложение не отвечает.

---

3. **Как создать новый поток в Java? Как запустить выполнение кода в этом потоке?**

   Новый поток в Java можно создать с помощью класса `Thread`.

   Пример:

   ```java
   Thread thread = new Thread(new Runnable() {
       @Override
       public void run() {
           // Код выполняется в фоновом потоке
       }
   });

   thread.start();
   ```

   Метод `start()` запускает новый поток и выполняет код из метода `run()`.

---

4. **Почему нельзя обновлять UI из фонового потока напрямую? Как правильно обновить интерфейс из другого потока?**

   Обновлять интерфейс напрямую из фонового потока нельзя, потому что элементы UI должны изменяться только в главном потоке приложения. Если менять интерфейс из другого потока, приложение может работать нестабильно или завершиться с ошибкой.

   Правильный способ — использовать `runOnUiThread()`:

   ```java
   runOnUiThread(new Runnable() {
       @Override
       public void run() {
           textView.setText("Готово");
       }
   });
   ```

---

5. **Для чего используется класс Handler? Как с его помощью отправить сообщение в UI-поток?**

   `Handler` используется для передачи задач между потоками. С его помощью можно отправить код на выполнение в главный UI-поток.

   Пример:

   ```java
   Handler handler = new Handler(Looper.getMainLooper());

   handler.post(new Runnable() {
       @Override
       public void run() {
           textView.setText("Обновление UI");
       }
   });
   ```

   Такой код выполнится в главном потоке приложения.

---

6. **Что такое ExecutorService? В чём его преимущество перед созданием потоков вручную?**

   `ExecutorService` — это инструмент Java для управления фоновыми задачами и потоками. Он позволяет запускать задачи без ручного создания каждого потока.

   Преимущества `ExecutorService`:
- можно переиспользовать потоки;
- проще управлять несколькими задачами;
- можно ограничить количество одновременно выполняемых потоков;
- есть возможность корректно завершить работу через `shutdown()`.

---

7. **Почему AsyncTask считается устаревшим? Какие альтернативы рекомендуется использовать?**

   `AsyncTask` считается устаревшим, потому что он плохо подходит для современных Android-приложений. У него есть проблемы с жизненным циклом Activity, возможны утечки памяти, а также сложнее контролировать выполнение задач.

   Вместо `AsyncTask` рекомендуется использовать:
- `Thread`;
- `ExecutorService`;
- `Handler`;
- `WorkManager`;
- Kotlin Coroutines, если проект написан на Kotlin.

---

8. **Как отобразить прогресс выполнения длительной операции с помощью ProgressBar?**

   Для отображения прогресса используется `ProgressBar`.

   В XML можно добавить горизонтальный `ProgressBar`:

   ```xml
   <ProgressBar
       android:id="@+id/progressBar"
       style="?android:attr/progressBarStyleHorizontal"
       android:layout_width="match_parent"
       android:layout_height="wrap_content"
       android:max="100"
       android:progress="0" />
   ```

   В коде можно изменять его видимость и прогресс:

   ```java
   progressBar.setVisibility(View.VISIBLE);
   progressBar.setProgress(50);
   progressBar.setVisibility(View.GONE);
   ```

   Если операция выполняется в фоновом потоке, обновлять `ProgressBar` нужно через `runOnUiThread()`.
