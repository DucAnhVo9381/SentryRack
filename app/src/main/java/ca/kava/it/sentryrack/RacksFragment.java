// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC
package ca.kava.it.sentryrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/** Racks screen - opened from the bottom navigation and the drawer
 * @author Krushna
 * @date 2026-09-28
 */
/** Racks screen design - Deliverable 2
 * Lists every monitored rack as a card with its image, name, location,
 * status (Online / Warning / Offline) and 4 sensor readings
 * (temperature, power, coolant, door). Filter chips are design only.
 * Design principle: this class only loads its layout (single responsibility);
 * all styling lives in themes_dashboard.xml, colors.xml and dimens.xml.
 * The sample racks will be replaced with racks from the cloud database later.
 * @author Krushna
 * @date 2026-10-03
 */
public class RacksFragment extends Fragment {

    // Loads the layout for the Racks screen (layout/fragment_racks.xml)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_racks, container, false);
    }
}