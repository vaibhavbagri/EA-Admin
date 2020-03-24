package com.liminal.ea_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RewardsActivity extends AppCompatActivity {

    private List<RewardDetails> rewardDetailsList = new ArrayList<>();
    private RewardsAdapter rewardsAdapter;
    private DatabaseReference databaseReference;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        rewardsAdapter = new RewardsAdapter(rewardDetailsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(rewardsAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                RewardDetails rewardDetails = rewardDetailsList.get(position);
                Toast.makeText(getApplicationContext(), rewardDetails.getTitle() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        SharedPreferences sharedPreferences = getSharedPreferences("User_Details", Context.MODE_PRIVATE);
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("adminTable")
                .child(sharedPreferences.getString("id",""))
                .child("Rewards");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Read data from firebase
                rewardDetailsList.clear();
                for (DataSnapshot reward : dataSnapshot.getChildren()) {
                    String rid = reward.getKey();
                    String title = Objects.requireNonNull(reward.child("Title").getValue()).toString();
                    String description = Objects.requireNonNull(reward.child("Description").getValue()).toString();
                    long cost = (long) reward.child("Cost").getValue();
                    long quantity = (long) reward.child("Quantity").getValue();
                    RewardDetails rewardDetails = new RewardDetails(rid, title, description, cost, quantity);
                    rewardDetailsList.add(rewardDetails);
                    count++;
                    assert rid != null;
                    Log.d("Rewards Activity", rid);
                }
                rewardsAdapter.notifyDataSetChanged();
//                databaseReference.removeEventListener(this);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Read failed
                Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
            }
        };

        databaseReference.addValueEventListener(eventListener);

        FloatingActionButton fab = findViewById(R.id.newRewardsButton);
        fab.setOnClickListener(dialog -> createPopUpDialog(this));
    }

    private void createPopUpDialog(Context context) {
        final Dialog inputTextDialog = new Dialog(context);
        inputTextDialog.setContentView(R.layout.dialog_reward_input);

        Button cancelButton = inputTextDialog.findViewById(R.id.cancelButton);
        Button uploadButton = inputTextDialog.findViewById(R.id.uploadButton);

        EditText title = inputTextDialog.findViewById(R.id.title);
        EditText description = inputTextDialog.findViewById(R.id.description);
        EditText cost = inputTextDialog.findViewById(R.id.cost);
        EditText quantity = inputTextDialog.findViewById(R.id.quantity);

        cancelButton.setOnClickListener(view -> inputTextDialog.dismiss());
        uploadButton.setOnClickListener(v -> {
            uploadText(String.valueOf(title.getText()),String.valueOf(description.getText()),String.valueOf(cost.getText()),String.valueOf(quantity.getText()));
            inputTextDialog.dismiss();
        });

        inputTextDialog.show();
    }

    private void uploadText(String title, String description, String cost, String quantity) {
        int code = 100 + count;
        String rid = "XYZ" + code;
        Map<Object, Object> map = new HashMap<>();
        map.put("Title",title);
        map.put("Description",description);
        map.put("Cost",Long.parseLong(cost));
        map.put("Quantity",Long.parseLong(quantity));
        databaseReference.child(rid).setValue(map);
    }
}
