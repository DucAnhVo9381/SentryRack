// Andres Zambrano N01495324 ONC
// Duc Anh Vo N01729381 0NC
// Anvesh Rachakonda N01661293 ONC
// Krushna Patel N01713093 ONC

package ca.kava.it.sentryrack.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.kava.it.sentryrack.R;
import ca.kava.it.sentryrack.model.RackRegistration;
import ca.kava.it.sentryrack.model.RackRepository;
import ca.kava.it.sentryrack.model.UserData;

public class RegisterRackController {

    public enum Field { RACK_NAME, FLOOR, ROOM }

    public interface Callback {
        void onFieldError(Field field, int errorResId);
        void onNotLoggedIn();
        void onSuccess(String rackId, String roomId);
        void onError(String message);
    }

    private static final String DEFAULT_LOCATION_NAME = "Default LocationData";
    private static final String DEFAULT_BUILDING_NAME = "Default BuildingData";
    private static final String ROLE_ADMIN = "admin";
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private final RackRepository repository = new RackRepository();
    private final AuthController authController = new AuthController();
    private final Callback callback;

    public RegisterRackController(Callback callback) {
        this.callback = callback;
    }

    public boolean register(String rackName, String floorName, String roomName) {
        String rack = clean(rackName);
        String floor = clean(floorName);
        String room = clean(roomName);

        if (rack.isEmpty()) {
            callback.onFieldError(Field.RACK_NAME, R.string.register_rack_name_error);
            return false;
        }
        if (floor.isEmpty()) {
            callback.onFieldError(Field.FLOOR, R.string.register_rack_floor_error);
            return false;
        }
        if (room.isEmpty()) {
            callback.onFieldError(Field.ROOM, R.string.register_rack_room_error);
            return false;
        }

        UserData userData = authController.getCurrentUser();
        if (userData == null) {
            callback.onNotLoggedIn();
            return false;
        }

        RackRegistration request = new RackRegistration(userData.getUid(), ROLE_ADMIN,
                DEFAULT_LOCATION_NAME, DEFAULT_BUILDING_NAME,
                floor, parseFloorLevel(floor), room, rack);

        repository.registerRack(request, new RackRepository.Callback() {
            @Override
            public void onSuccess(String rackId, String roomId) {
                callback.onSuccess(rackId, roomId);
            }

            @Override
            public void onDuplicateRack() {
                callback.onFieldError(Field.RACK_NAME, R.string.register_rack_duplicate_error);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
        return true;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private Integer parseFloorLevel(String floorName) {
        Matcher matcher = DIGITS.matcher(floorName);
        if (!matcher.find()) return null;
        try {
            return Integer.valueOf(matcher.group());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}