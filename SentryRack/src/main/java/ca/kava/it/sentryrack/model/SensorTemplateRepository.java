package ca.kava.it.sentryrack.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class SensorTemplateRepository {

    public interface Callback {
        void onLoaded(DataSnapshot template);
        void onSaved();
        void onError(String message);
    }

    private final DatabaseReference templateRef =
            FirebaseDatabase.getInstance().getReference().child("sensorTemplate");
    private final Callback callback;

    public SensorTemplateRepository(Callback callback) {
        this.callback = callback;
    }

    public void load() {
        templateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onLoaded(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void save(Map<String, Object> template) {
        templateRef.setValue(template, (error, reference) -> {
            if (error != null) {
                callback.onError(error.getMessage());
            } else {
                callback.onSaved();
            }
        });
    }
}
