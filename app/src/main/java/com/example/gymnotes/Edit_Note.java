package com.example.gymnotes;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Edit_Note extends AppCompatActivity {
    private EditText titleEditText, noteEditText;
    private TextView textViewQuote;
    private Button EditNoteButton;
    private DatabaseReference notesRef, contentsRef;
    private String noteId, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        titleEditText = findViewById(R.id.titleEditText);
        noteEditText = findViewById(R.id.noteEditText);
        EditNoteButton = findViewById(R.id.EditNoteButton);
        textViewQuote = findViewById(R.id.textViewQuote);

        // массив только с текстами цитат
        String[] quotes = {
                "❝ Залог успеха — готовность работать не покладая рук, независимо от того, какие препятствия стоят на пути. ❞",
                "❝ Вы можете проиграть утреннюю битву, но не проиграть дневную войну! ❞",
                "❝ Если ты думаешь, что ты достиг вершины, помни, твой потолок чей-то пол. Именно таким ты хотел стать? ❞",
                "❝ Большинство тех, кто не верит в нас, не верит лишь потому, что они не представляют, как можно делать то, что делаете вы. ❞",
                "❝ Дерзкое мышление — это мышление, которое знает, что сдаваться не вариант. Оно знает, что должно найти способ обойти все препятствия, чтобы прийти к цели. Будьте сильны! ❞",
                "❝ Иногда нужно вступить в войну с самим собой, чтобы исправить себя. ❞",
                "❝ Мечты умирают во времена страданий! ❞"
        };

        Random random = new Random();
        int index = random.nextInt(quotes.length);

        String quote = quotes[index];
        String author = "— Дэвид Гоггинс";

// формируем полный текст
        String fullText = quote + "\n" + author;

// делаем автора курсивом
        SpannableString styledText = new SpannableString(fullText);
        styledText.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC),
                fullText.indexOf(author), fullText.length(), 0);

        textViewQuote.setText(styledText);

        // получаем данные из Intent
        uid = getIntent().getStringExtra("uid");
        noteId = getIntent().getStringExtra("noteId");
        String title = getIntent().getStringExtra("noteTitle");
        String content = getIntent().getStringExtra("noteContent");

        // инициализируем ссылки
        notesRef = FirebaseDatabase.getInstance().getReference("Notes").child(uid);
        contentsRef = FirebaseDatabase.getInstance().getReference("NoteContents").child(uid);

        // подставляем данные
        titleEditText.setText(title);
        noteEditText.setText(content);

        EditNoteButton.setOnClickListener(v -> {
            String newTitle = titleEditText.getText().toString().trim();
            String newContent = noteEditText.getText().toString().trim();

            if (noteId != null) {
                // Обновляем только поле title в Notes (не затираем groupId/timestamp)
                Map<String, Object> updates = new HashMap<>();
                updates.put("title", newTitle);
                notesRef.child(noteId).updateChildren(updates);

                // В NoteContents обновляем именно поле "content"
                contentsRef.child(noteId).child("Content").setValue(newContent);
            }

            finish();
        });
    }
}
