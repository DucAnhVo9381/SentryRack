// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC

package ca.kava.it.sentryrack;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

/** Help center screen (opened from the 3-dot overflow menu).
 * - Search bar filters the questions while you type
 * - Tap a question card to open / close its answer (smooth animation + arrow turns)
 * @author Anvesh
 * @date 2026-10-08
 */
public class HelpCenterFragment extends Fragment {

    // Questions and answers (same order). Add a new pair here to add a new FAQ.
    private static final int[] QUESTIONS = {
            R.string.help_q1, R.string.help_q2, R.string.help_q3,
            R.string.help_q4, R.string.help_q5
    };
    private static final int[] ANSWERS = {
            R.string.help_a1, R.string.help_a2, R.string.help_a3,
            R.string.help_a4, R.string.help_a5
    };

    // Arrow angles: 90 = pointing down (closed), 270 = pointing up (open)
    private static final float ARROW_CLOSED = 90f;
    private static final float ARROW_OPEN = 270f;
    private static final long ARROW_ANIM_MS = 200;

    private LinearLayout faqContainer;
    private TextView noResults;

    public HelpCenterFragment() {
        // Required empty public constructor
    }

    /** Loads the Help center layout
     * @author Anvesh
     * @date 2026-10-08
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help_center, container, false);
    }

    /** Finds the views, builds the FAQ cards and turns on the search
     * @author Anvesh
     * @date 2026-10-08
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        faqContainer = view.findViewById(R.id.helpFaqContainer);
        noResults = view.findViewById(R.id.helpNoResults);

        addFaqCards();
        setupSearch(view);
    }

    /** Creates one card (ar_item_faq.xml) for each question and adds it to the screen
     * @author Anvesh
     * @date 2026-10-08
     */
    private void addFaqCards() {
        LayoutInflater inflater = getLayoutInflater();

        for (int i = 0; i < QUESTIONS.length; i++) {
            View card = inflater.inflate(R.layout.overflow_item_faq, faqContainer, false);

            TextView question = card.findViewById(R.id.faqQuestion);
            TextView answer = card.findViewById(R.id.faqAnswer);
            question.setText(QUESTIONS[i]);
            answer.setText(ANSWERS[i]);

            card.findViewById(R.id.faqHeader).setOnClickListener(v -> toggleAnswer(card));
            faqContainer.addView(card);
        }
    }

    /** Opens the answer if it is closed, closes it if it is open.
     * The card grows/shrinks smoothly and the arrow turns.
     * @author Anvesh
     * @date 2026-10-08
     */
    private void toggleAnswer(View card) {
        TextView answer = card.findViewById(R.id.faqAnswer);
        ImageView arrow = card.findViewById(R.id.faqArrow);
        boolean isOpen = answer.getVisibility() == View.VISIBLE;

        // Animates the card size change and moves the cards below it smoothly
        TransitionManager.beginDelayedTransition(faqContainer, new AutoTransition());

        answer.setVisibility(isOpen ? View.GONE : View.VISIBLE);
        arrow.animate()
                .rotation(isOpen ? ARROW_CLOSED : ARROW_OPEN)
                .setDuration(ARROW_ANIM_MS)
                .start();
    }

    /** Filters the cards every time the user types in the search bar
     * @author Anvesh
     * @date 2026-10-08
     */
    private void setupSearch(View view) {
        EditText searchInput = view.findViewById(R.id.helpSearchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                filterFaq(s.toString());
            }
        });
    }

    /** Shows only the cards whose question OR answer contains the search text.
     * Shows "No matching questions" when nothing matches.
     * @author Anvesh
     * @date 2026-10-08
     */
    private void filterFaq(String query) {
        String search = query.trim().toLowerCase(Locale.getDefault());
        int shownCount = 0;

        for (int i = 0; i < faqContainer.getChildCount(); i++) {
            View card = faqContainer.getChildAt(i);
            TextView question = card.findViewById(R.id.faqQuestion);
            TextView answer = card.findViewById(R.id.faqAnswer);

            String cardText = (question.getText() + " " + answer.getText())
                    .toLowerCase(Locale.getDefault());
            boolean matches = search.isEmpty() || cardText.contains(search);

            card.setVisibility(matches ? View.VISIBLE : View.GONE);
            if (matches) {
                shownCount++;
            }
        }
        noResults.setVisibility(shownCount == 0 ? View.VISIBLE : View.GONE);
    }
}