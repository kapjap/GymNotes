package com.example.gymnotes;

import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationViewModel  extends ViewModel {
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usersReference;
    private MutableLiveData<String> error = new MutableLiveData<>();
    private  MutableLiveData<FirebaseUser> user = new MutableLiveData<>();
    public RegistrationViewModel() {
        auth = FirebaseAuth.getInstance();
    }
    public LiveData<String> getError() {
        return error;
    }

    public LiveData<FirebaseUser> getUser() {
        return user;
    }
    public void signUp(
            String email,
            String password,
            String name,
            String lastName
    ){
        auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                user.setValue(authResult.getUser());
                FirebaseUser firebaseUser = authResult.getUser();
                if(firebaseUser==null)
                {
                    return;
                }
                User user1 = new User(
                        firebaseUser.getUid(),
                        name,
                        lastName,
                        email
                );
                usersReference.child(user1.getId()).setValue(user1);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                error.setValue(e.getMessage());
            }
        });
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference =firebaseDatabase.getReference("Users");
    }
}
