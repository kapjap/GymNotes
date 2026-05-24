package com.example.gymnotes;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class News extends AppCompatActivity {

    private RecyclerView recyclerNews;
    private NewsAdapter newsAdapter;
    private ArrayList<NewsItem> newsList;
    private EditText searchNews;
    private ImageView buttonBack;
    private ImageView buttonRefresh;

    private static final String API_KEY = "db6869493bee4044794b461584755043";
    private static final String TAG = "NewsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_news);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerNews = findViewById(R.id.recyclerNews);
        searchNews = findViewById(R.id.searchNews);
        buttonBack = findViewById(R.id.buttonBack);
        buttonRefresh = findViewById(R.id.buttonRefresh);

        recyclerNews.setLayoutManager(new LinearLayoutManager(this));

        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(this, newsList);
        recyclerNews.setAdapter(newsAdapter);

        buttonBack.setOnClickListener(v -> finish());

        buttonRefresh.setOnClickListener(v -> {
            v.animate()
                    .rotationBy(180f)
                    .setDuration(300)
                    .start();

            loadNews();
        });

        loadNews();
    }

    private void loadNews() {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                Log.d("NEWS_DEBUG", "GNews started");

                String query = "фитнес OR тренировки OR спортзал OR бодибилдинг OR gym OR workout";
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());

                // страницы 1..8
                int randomPage = new Random().nextInt(8) + 1;
                String urlString = "https://gnews.io/api/v4/search?q=" + encodedQuery
                        + "&lang=ru"
                        + "&country=ru"
                        + "&max=10"
                        + "&page=" + randomPage
                        + "&sortby=publishedAt"
                        + "&apikey=" + API_KEY;

                Log.d("NEWS_PAGE", "Page: " + randomPage);
                Log.d("NEWS_URL", urlString);

                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                int responseCode = connection.getResponseCode();
                Log.d("NEWS_CODE", "Response code: " + responseCode);

                BufferedReader reader;
                if (responseCode >= 200 && responseCode < 300) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                String responseText = result.toString();
                Log.d("API_RESPONSE", responseText);

                JSONObject jsonObject = new JSONObject(responseText);

                if (!jsonObject.has("articles")) {
                    Log.e("NEWS_ERROR", "В ответе нет articles: " + responseText);
                    return;
                }

                JSONArray articles = jsonObject.getJSONArray("articles");
                ArrayList<NewsItem> loadedNews = new ArrayList<>();

                for (int i = 0; i < articles.length(); i++) {
                    JSONObject article = articles.getJSONObject(i);

                    String title = article.optString("title", "Без названия");
                    String date = article.optString("publishedAt", "");
                    String imageUrl = article.optString("image", "");
                    String articleUrl = article.optString("url", "");

                    JSONObject sourceObject = article.optJSONObject("source");
                    String source = "Unknown";
                    if (sourceObject != null) {
                        source = sourceObject.optString("name", "Unknown");
                    }

                    if (date.length() >= 10) {
                        date = date.substring(0, 10);
                    }

                    loadedNews.add(new NewsItem(title, source, date, imageUrl, articleUrl));
                }

                runOnUiThread(() -> newsAdapter.updateList(loadedNews));

            } catch (Exception e) {
                Log.e("NEWS_ERROR", "Ошибка загрузки GNews", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}