package com.example.gymnotes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class Adapter_mainNote extends RecyclerView.Adapter<Adapter_mainNote.NotesViewHolder> {

    public interface OnNotesChangedListener {
        void onNotesCountChanged(int newCount);
    }

    private List<NoteFull> noteFullList = new ArrayList<>();
    private final DatabaseReference titlesRef;
    private final DatabaseReference contentsRef;
    private final String uid;
    private final OnNotesChangedListener listener;

    public Adapter_mainNote(String uid, OnNotesChangedListener listener) {
        this.uid = uid;
        this.listener = listener;
        titlesRef = FirebaseDatabase.getInstance().getReference("Notes").child(uid);
        contentsRef = FirebaseDatabase.getInstance().getReference("NoteContents").child(uid);
    }

    public void setNoteFullList(List<NoteFull> noteFullList) {
        this.noteFullList = noteFullList;
        notifyDataSetChanged();

        if (listener != null) {
            listener.onNotesCountChanged(this.noteFullList.size());
        }
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        NoteFull noteFull = noteFullList.get(position);

        holder.noteTitle.setText(noteFull.getTitle() != null ? noteFull.getTitle() : "Без названия");
        holder.contentTitle.setText(noteFull.getContent() != null ? noteFull.getContent() : "");

        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            NoteFull currentNote = noteFullList.get(currentPosition);
            String noteId = currentNote.getId();

            if (noteId != null) {
                titlesRef.child(noteId).removeValue();
                contentsRef.child(noteId).removeValue();
            }

            noteFullList.remove(currentPosition);
            notifyItemRemoved(currentPosition);
            notifyItemRangeChanged(currentPosition, noteFullList.size());

            if (listener != null) {
                listener.onNotesCountChanged(noteFullList.size());
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, Edit_Note.class);
            intent.putExtra("uid", uid);
            intent.putExtra("noteId", noteFull.getId());
            intent.putExtra("noteTitle", noteFull.getTitle());
            intent.putExtra("noteContent", noteFull.getContent());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return noteFullList != null ? noteFullList.size() : 0;
    }

    static class NotesViewHolder extends RecyclerView.ViewHolder {
        private final TextView noteTitle;
        private final TextView contentTitle;
        private final ImageButton btnDelete;
        private final ImageButton btnEdit;

        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            contentTitle = itemView.findViewById(R.id.noteContent);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}