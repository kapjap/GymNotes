package com.example.gymnotes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotesMain extends AppCompatActivity implements Adapter_mainNote.OnNotesChangedListener {

    private RecyclerView recyclerView;
    private Adapter_mainNote adapter;
    private DatabaseReference titlesRef;
    private DatabaseReference contentsRef;
    private String uid;
    private FirebaseAuth auth;

    private TextView textStatsCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    dpToPx(20) + systemBars.left,
                    dpToPx(24) + systemBars.top,
                    dpToPx(20) + systemBars.right,
                    dpToPx(20) + systemBars.bottom
            );
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        uid = user.getUid();
        titlesRef = FirebaseDatabase.getInstance().getReference("Notes").child(uid);
        contentsRef = FirebaseDatabase.getInstance().getReference("NoteContents").child(uid);

        recyclerView = findViewById(R.id.notesRecyclerView);
        textStatsCount = findViewById(R.id.textStatsCount);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter_mainNote(uid, this);
        recyclerView.setAdapter(adapter);

        ImageButton buttonBack = findViewById(R.id.imageButton4);
        buttonBack.setOnClickListener(v -> finish());

        Button buttonAddNoteTop = findViewById(R.id.buttonAddNoteTop);
        buttonAddNoteTop.setOnClickListener(v -> launchNextScreen_Create_Note());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromDatabase();
    }

    private void loadNotesFromDatabase() {
        titlesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot titlesSnapshot) {
                contentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot contentsSnapshot) {
                        List<NoteFull> noteFullList = new ArrayList<>();

                        for (DataSnapshot noteNode : titlesSnapshot.getChildren()) {
                            String noteId = noteNode.getKey();
                            String title = noteNode.child("title").getValue(String.class);

                            String content = contentsSnapshot
                                    .child(noteId)
                                    .child("Content")
                                    .getValue(String.class);

                            if (title == null) {
                                title = "Без названия";
                            }

                            if (content == null) {
                                content = "";
                            }

                            Log.d("NotesMain", "noteId=" + noteId + ", title=" + title + ", content=" + content);
                            noteFullList.add(0, new NoteFull(noteId, title, content));
                        }

                        adapter.setNoteFullList(noteFullList);
                        updateStats(noteFullList.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Failed to read NoteContents", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read Notes", error.toException());
            }
        });
    }

    private void updateStats(int count) {
        if (count == 1) {
            textStatsCount.setText("1 заметка");
        } else if (count >= 2 && count <= 4) {
            textStatsCount.setText(count + " заметки");
        }else if(count == 0 ){
            textStatsCount.setText("Нет заметок");
        }else {
            textStatsCount.setText(count + " заметок");
        }


    }

    @Override
    public void onNotesCountChanged(int newCount) {
        updateStats(newCount);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, NotesMain.class);
    }

    private void launchNextScreen_Create_Note() {
        Intent intent = new Intent(this, Create_Note.class);
        startActivity(intent);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}