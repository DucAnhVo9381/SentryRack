// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;

import ca.kava.it.sentryrack.model.User;
import ca.kava.it.sentryrack.controller.AuthController;

public class DashboardFragment extends Fragment {

    //--- FRAGMENT DEFAULT FIELDS & METHODS ---
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public DashboardFragment() {
        // Required empty public constructor
    }

    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    //--- MAIN LIFECYCLE METHOD ---
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dashboard_fragment, container, false);

        AuthController authController = new AuthController();
        User currentUser = authController.getCurrentUser();
        String displayName = (currentUser != null && currentUser.getDisplayName() != null) ? currentUser.getDisplayName() : getString(R.string.user_name_default);

        TextView dashboardGreeting = view.findViewById(R.id.dashboardGreeting);
        dashboardGreeting.setText(getGreetingMessage(displayName));

        return view;
    }

    // Greeting logic method
    private String getGreetingMessage(String userName) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour < 12) {
            greeting = getString(R.string.good_morning);
        }
        else if (hour < 17) {
            greeting = getString(R.string.good_afternoon);
        }
        else {
            greeting = getString(R.string.good_evening);
        }

        return greeting + ", " + userName;
    }
}