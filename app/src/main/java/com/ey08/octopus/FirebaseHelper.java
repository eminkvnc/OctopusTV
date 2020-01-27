package com.ey08.octopus;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ey08.octopus.model.MediaData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class FirebaseHelper {

    private DatabaseReference databaseReference;

    public static FirebaseHelper getInstance(){
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final FirebaseHelper INSTANCE = new FirebaseHelper();
    }

    public FirebaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void addMediaData(MediaData mediaData){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("as", "onDataChange: "+mediaData.getName());
                String[] strings = mediaData.getName().split("\\.");
                String id = UUID.randomUUID().toString();
                if(dataSnapshot.hasChild("MediaData/"+strings[0])){
                    databaseReference.child("MediaData")
                            .child(strings[0])
                            .child("Times")
                            .child(id)
                            .child("startTime")
                            .setValue(mediaData.getStartTime());
                    databaseReference.child("MediaData")
                            .child(strings[0])
                            .child("Times")
                            .child(id)
                            .child("stopTime")
                            .setValue(mediaData.getStopTime());
                }else {
                    databaseReference.child("MediaData").child(strings[0]).setValue(mediaData);
                    databaseReference.child("MediaData")
                            .child(strings[0])
                            .child("Times")
                            .child(id)
                            .child("startTime")
                            .setValue(mediaData.getStartTime());
                    databaseReference.child("MediaData")
                            .child(strings[0])
                            .child("Times")
                            .child(id)
                            .child("stopTime")
                            .setValue(mediaData.getStopTime());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}
