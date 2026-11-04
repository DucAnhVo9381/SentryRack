package ca.kava.it.sentryrack;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anvesh Rachakonda
 * @date 2026-11-04
 * Single place that talks to Firebase Realtime Database for support data:
 * feedback, app ratings and Help Center FAQs.
 * Design pattern: Singleton (one shared instance for the whole app).
 */
public final class SupportRepository {

    public static final String NODE_FEEDBACK = "feedback";
    public static final String NODE_RATINGS = "ratings";
    public static final String NODE_FAQ = "help_faq";

    private static final String FIELD_QUESTION = "question";
    private static final String FIELD_ANSWER = "answer";

    /** Tells the caller whether a write reached the cloud. */
    public interface ResultCallback {
        void onResult(boolean success);
    }

    /** Returns FAQ lists from the cloud (empty lists = nothing stored yet). */
    public interface FaqCallback {
        void onFaqsLoaded(List<String> questions, List<String> answers);
    }

    private static volatile SupportRepository instance;
    private final DatabaseReference root;

    private SupportRepository(Context context) {
        // URL comes from strings.xml because google-services.json has no firebase_url
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                context.getString(R.string.firebase_db_url));
        root = db.getReference();
    }

    /** Returns the one shared instance (thread safe). */
    public static SupportRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (SupportRepository.class) {
                if (instance == null) {
                    instance = new SupportRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /** Saves one feedback entry under feedback/{pushId}. */
    public void sendFeedback(String type, String message, String email,
                             String appVersion, @Nullable ResultCallback callback) {
        Map<String, Object> data = deviceInfo(email, appVersion);
        data.put("type", type);
        data.put("message", message);
        write(root.child(NODE_FEEDBACK).push(), data, callback);
    }

    /** Saves one rating entry under ratings/{pushId}. */
    public void sendRating(int stars, String email, String appVersion,
                           @Nullable ResultCallback callback) {
        Map<String, Object> data = deviceInfo(email, appVersion);
        data.put("stars", stars);
        write(root.child(NODE_RATINGS).push(), data, callback);
    }

    /** Reads help_faq/{lang} once. */
    public void loadFaqs(String lang, @NonNull FaqCallback callback) {
        root.child(NODE_FAQ).child(lang).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> questions = new ArrayList<>();
                List<String> answers = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren()) {
                    String q = item.child(FIELD_QUESTION).getValue(String.class);
                    String a = item.child(FIELD_ANSWER).getValue(String.class);
                    if (q != null && a != null) {
                        questions.add(q);
                        answers.add(a);
                    }
                }
                callback.onFaqsLoaded(questions, answers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFaqsLoaded(new ArrayList<>(), new ArrayList<>());
            }
        });
    }

    /** Uploads the local FAQ list to help_faq/{lang} (used when the cloud is empty). */
    public void seedFaqs(String lang, List<String> questions, List<String> answers) {
        List<Map<String, Object>> list = new ArrayList<>();
        int count = Math.min(questions.size(), answers.size());
        for (int i = 0; i < count; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put(FIELD_QUESTION, questions.get(i));
            item.put(FIELD_ANSWER, answers.get(i));
            list.add(item);
        }
        root.child(NODE_FAQ).child(lang).setValue(list);
    }

    // ---- private helpers (DRY) ----

    private Map<String, Object> deviceInfo(String email, String appVersion) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email == null ? "" : email);
        data.put("deviceModel", Build.MANUFACTURER + " " + Build.MODEL);
        data.put("androidVersion", Build.VERSION.RELEASE);
        data.put("appVersion", appVersion);
        data.put("createdAt", ServerValue.TIMESTAMP);
        return data;
    }

    private void write(DatabaseReference ref, Map<String, Object> data,
                       @Nullable ResultCallback callback) {
        ref.setValue(data).addOnCompleteListener(task -> {
            if (callback != null) callback.onResult(task.isSuccessful());
        });
    }
}