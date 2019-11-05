package edu.illinois.cs.cs125.fall2019.mp;

import android.content.Intent;
import android.graphics.Point;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
//import android.widget.RadioButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the game creation screen, where the user configures a new game.
 */
public final class NewGameActivity extends AppCompatActivity {

    // This activity doesn't do much at first - it'll be worked on in Checkpoints 1 and 3

    /** The Google Maps view used to set the area for area mode. Null until getMapAsync finishes. */
    private GoogleMap areaMap;
    /** The Google Maps view used to set the area for target mode. Null until getMapAsync finishes. */
    private GoogleMap targetMap;
    /** Stores the targets present on the Google Maps. */
    private List<Marker> targets = new ArrayList<>();
    /** Stores the players. */
    private List<Invitee> invitees;


    /**
     * Called by the Android system when the activity is created.
     * @param savedInstanceState state from the previously terminated instance (unused)
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game); // app/src/main/res/layout/activity_new_game.xml
        setTitle(R.string.create_game); // Change the title in the top bar
        // Now that setContentView has been called, findViewById and findFragmentById work

        // Find the Google Maps component for the area map
        SupportMapFragment areaMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.areaSizeMap);
        // Start the process of getting a Google Maps object
        areaMapFragment.getMapAsync(newMap -> {
            // NONLINEAR CONTROL FLOW: Code in this block is called later, after onCreate ends
            // It's a "callback" - it will be called eventually when the map is ready

            // Set the map variable so it can be used by other functions
            areaMap = newMap;
            // Center it on campustown
            centerMap(areaMap);
        });

        // Find the Google Maps component for the target map
        SupportMapFragment targetMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.targetsMap);
        // Start the process of getting a Google Maps object
        targetMapFragment.getMapAsync(newMap -> {
            // NONLINEAR CONTROL FLOW: Code in this block is called later, after onCreate ends
            // It's a "callback" - it will be called eventually when the map is ready

            // Set the map variable so it can be used by other functions
            targetMap = newMap;
            targetMap.setOnMapLongClickListener(location -> {
                // Create a MarkerOptions object to specify where we want the marker
                MarkerOptions options = new MarkerOptions().position(location);

                // Add it to the map - Google Maps gives us the created Marker
                Marker marker = targetMap.addMarker(options);
                // Keep track of the new marker so changeMarkerColor can adjust it later
                targets.add(marker);
            });
            // Center it on campustown
            centerMap(targetMap);
            targetMap.setOnMapLongClickListener(location -> {
                // Create a MarkerOptions object to specify where we want the marker
                MarkerOptions options = new MarkerOptions().position(location);

                // Add it to the map - Google Maps gives us the created Marker
                Marker marker = targetMap.addMarker(options);
                // Keep track of the new marker so changeMarkerColor can adjust it later
                targets.add(marker);
            });

            targetMap.setOnMarkerClickListener(clickedMarker -> {
                // Code here runs whenever the user taps a marker.
                // clickedMarker is the Marker object the user clicked.
                // 1. Remove the marker from the map with its remove function.
                // 2. Remove it from your targets list.
                clickedMarker.remove();
                targets.remove(clickedMarker);
                return true; // This makes Google Maps not pan the map again
            });

        });
        invitees = new ArrayList<>();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        invitees.add(new Invitee(currentUser.getEmail(), TeamID.OBSERVER));
        updatePlayersUI();

        Button addInvitee = findViewById(R.id.addInvitee);
        addInvitee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                addInvitee();
            }
        });

        /*
         * Setting an ID for a control in the UI designer produces a constant on R.id
         * that can be passed to findViewById to get a reference to that control.
         * Here we get a reference to the Create Game button.
         */
        Button createGame = findViewById(R.id.createGame);
        /*
         * Now that we have a reference to the control, we can use its setOnClickListener
         * method to set the handler to run when the user clicks the button. That function
         * takes an OnClickListener instance. OnClickListener, like many types in Android,
         * has exactly one function which must be filled out, so Java allows instances of it
         * to be written as "lambdas", which are like small functions that can be passed around.
         * The part before the arrow is the argument list (Java infers the types); the part
         * after is the statement to run. Here we don't care about the argument, but it must
         * be there for the signature to match.
         */
        createGame.setOnClickListener(unused -> createGameClicked());
        RadioGroup radio = findViewById(R.id.gameModeGroup);
        radio.setOnCheckedChangeListener((unused, checkedId) -> {
            if (checkedId == R.id.targetModeOption) {
                LinearLayout targetSettings = findViewById(R.id.targetSettings);
                targetSettings.setVisibility(View.VISIBLE);
                LinearLayout areaSettings = findViewById(R.id.areaSettings);
                areaSettings.setVisibility(View.GONE);
            } else if (checkedId == R.id.areaModeOption) {
                LinearLayout areaSettings = findViewById(R.id.areaSettings);
                areaSettings.setVisibility(View.VISIBLE);
                LinearLayout targetSettings = findViewById(R.id.targetSettings);
                targetSettings.setVisibility(View.GONE);

            }
        });
        /*
         * It's also possible to make lambdas for functions that take zero or multiple parameters.
         * In those cases, the parameter list needs to be wrapped in parentheses, like () for a
         * zero-argument lambda or (someArg, anotherArg) for a two-argument lambda. Lambdas that
         * run multiple statements, like the one passed to getMapAsync above, look more like
         * normal functions in that they need their body wrapped in curly braces. Multi-statement
         * lambdas for functions with a non-void return type need return statements, again like
         * normal functions.
         */


            // checkedId is the R.id constant of the currently checked RadioButton
            // Your code here: make only the selected mode's settings group visible





    }

    /**
     * Updates Player UI with information about new Invitees.
     */
    private void updatePlayersUI() {
        LinearLayout playersList = findViewById(R.id.playersList);
        playersList.removeAllViews();
        if (invitees != null) {
            for (Invitee i: invitees) {
                View playersChunk = getLayoutInflater().inflate(R.layout.chunk_invitee, playersList, false);
                TextView inviteeEmail = playersChunk.findViewById(R.id.inviteeEmail);
                inviteeEmail.setText(i.getEmail());

                Spinner inviteeTeam = playersChunk.findViewById(R.id.inviteeTeam);
                Button removeButton = playersChunk.findViewById(R.id.removeInvitee);

                inviteeTeam.setSelection(i.getTeamId());

                inviteeTeam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(final AdapterView<?> parent, final View view, final int position,
                                               final long id) {
                        i.setTeamId(position);
                        updatePlayersUI();
                    }

                    @Override
                    public void onNothingSelected(final AdapterView<?> parent) {

                    }
                });

                if (i.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                    removeButton.setVisibility(View.GONE);
                }
                removeButton.setOnClickListener(unused -> {
                    invitees.remove(i);
                    updatePlayersUI();
                });

                playersList.addView(playersChunk);
            }
        }
    }

    /**
     * Adds Invitees.
     */

    public void addInvitee() {
        EditText newInviteeEmail = findViewById(R.id.newInviteeEmail);
        String newInviteeEmailString = newInviteeEmail.getText().toString();

        if ((newInviteeEmailString.isEmpty()) == false) {
            Invitee newRole = new Invitee(newInviteeEmailString, TeamID.OBSERVER);
            invitees.add(newRole);
            newInviteeEmail.setText("");
            updatePlayersUI();
        }
    }

    /**
     * Sets up the area sizing map with initial settings: centering on campustown.
     * <p>
     * You don't need to alter or understand this function, but you will want to use it when
     * you add another map control in Checkpoint 3.
     * @param map the map to center
     */
    private void centerMap(final GoogleMap map) {
        // Bounds of campustown and some surroundings
        final double swLatitude = 40.098331;
        final double swLongitude = -88.246065;
        final double neLatitude = 40.116601;
        final double neLongitude = -88.213077;

        // Get the window dimensions (for the width)
        Point windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);

        // Convert 300dp (height of map control) to pixels
        final int mapHeightDp = 300;
        float heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mapHeightDp,
                getResources().getDisplayMetrics());

        // Submit the camera update
        final int paddingPx = 10;
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                new LatLng(swLatitude, swLongitude),
                new LatLng(neLatitude, neLongitude)), windowSize.x, (int) heightPx, paddingPx));
    }

    /**
     * Code to run when the Create Game button is clicked.
     */
    private void createGameClicked() {
        // Set up an Intent that will launch GameActivity
        Intent intent = new Intent(this, GameActivity.class);
        RadioGroup radio = findViewById(R.id.gameModeGroup);
        EditText proximityThreshold = findViewById(R.id.proximityThreshold);
        EditText cellSize = findViewById(R.id.cellSize);
        /*if (radio.getCheckedRadioButtonId() != -1 && Integer.parseInt(cellSize.getText().toString())
                != 0 && Integer.parseInt(proximityThreshold.getText().toString()) != 0) {
            //startActivity(intent);
        }*/
        String gameMode = intent.getStringExtra("mode");
        if (radio.getChildAt(0).getId() == R.id.targetModeOption) {
            //EditText proximityThreshold = findViewById(R.id.proximityThreshold);
            String text = proximityThreshold.getText().toString();
            int threshold = Integer.parseInt(text);
            intent.putExtra("proximityThreshold", threshold);
        } else if (radio.getChildAt(1).getId() == R.id.areaModeOption) {
            //EditText cellSize = findViewById(R.id.cellSize);
            String text = cellSize.getText().toString();
            int size = Integer.parseInt(text);
            intent.putExtra("cellSize", size);
            LatLngBounds bounds = areaMap.getProjection().getVisibleRegion().latLngBounds;
            intent.putExtra("areaNorth", bounds.northeast.longitude);
            intent.putExtra("areaEast", bounds.northeast.latitude);
            intent.putExtra("areaSouth", bounds.southwest.longitude);
            intent.putExtra("areaWest", bounds.southwest.latitude);
        }
        //finish();


        // Complete this function so that it populates the Intent with the user's settings (using putExtra)
        // If the user has set all necessary settings, launch the GameActivity and finish this activity
    }

}
