package com.example.gymnotes;

public class NewsItem {
    private String title;
    private String source;
    private String date;
    private String imageUrl;
    private String articleUrl;

    public NewsItem(String title, String source, String date, String imageUrl, String articleUrl) {
        this.title = title;
        this.source = source;
        this.date = date;
        this.imageUrl = imageUrl;
        this.articleUrl = articleUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public String getDate() {
        return date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getArticleUrl() {
        return articleUrl;
    }
}