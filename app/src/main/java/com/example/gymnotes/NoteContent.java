package com.example.gymnotes;

public class NoteContent {
    public String Content;

    public NoteContent() {} // пустой конструктор обязателен для Firebase

    public NoteContent(String content) {
        this.Content = content;
    }
}
