package com.example.gymnotes;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Create_Note extends AppCompatActivity {
    private EditText editTextTitle;
    private EditText editTextContent;
    private Button buttonSaveNote;
    private EditText group;
    private String formattedDate;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference notesReference;
    private DatabaseReference noteContentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView dateTextView = findViewById(R.id.noteDate);
        initView();

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        formattedDate = dateFormat.format(currentDate);
        dateTextView.setText(formattedDate);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();

        notesReference = FirebaseDatabase.getInstance()
                .getReference("Notes")
                .child(uid);

        noteContentRef = FirebaseDatabase.getInstance()
                .getReference("NoteContents")
                .child(uid);

        buttonSaveNote.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String noteTitle = editTextTitle.getText().toString().trim();
        String groupNote = group.getText().toString().trim();

        if (noteTitle.isEmpty()) {
            editTextTitle.setError("Title is required");
            return;
        }
        if (!groupNote.equalsIgnoreCase("Strength") && !groupNote.equalsIgnoreCase("Cardio")) {
            group.setError("Invalid group name. Use 'Strength' or 'Cardio'");
            return;
        }

        Note note = new Note(noteTitle, groupNote, formattedDate);

        //уникал ключ
        String noteId = notesReference.push().getKey();
        if (noteId == null) {
            Toast.makeText(this, "Error generating note ID", Toast.LENGTH_SHORT).show();
            return;
        }

        //  заголовок заметки
        notesReference.child(noteId).setValue(note)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // содержимое заметки под тем же UID и noteId
        String contentNote = editTextContent.getText().toString().trim();
        NoteContent noteContent = new NoteContent(contentNote);
        noteContentRef.child(noteId).setValue(noteContent);
    }

    private void initView() {
        editTextTitle = findViewById(R.id.titleEditText);
        editTextContent = findViewById(R.id.noteEditText);
        buttonSaveNote = findViewById(R.id.saveNoteButton);
        group = findViewById(R.id.editTextGroup);
    }
}
