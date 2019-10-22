package edu.illinois.cs.cs125.fall2019.mp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
//import android.view.View;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents the main screen of the app, where the user will be able to view invitations and enter games.
 */
public final class MainActivity extends AppCompatActivity {

    /**
     * Called by the Android system when the activity is created.
     * @param savedInstanceState saved state from the previously terminated instance of this activity (unused)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // This "super" call is required for all activities
        super.onCreate(savedInstanceState);


        // Create the UI from a layout resource
        setContentView(R.layout.activity_main);
        Button createGame = findViewById(R.id.createGame);
        //createGame.setOnClickListener(unused -> startActivity(new Intent(this,
               //NewGameActivity.class)));



        // This activity doesn't do anything yet - it immediately launches the game activity
        // Work on it will start in Checkpoint 1

        // Intents are Android's way of specifying what to do/launch
        // Here we create an Intent for launching GameActivity and act on it with startActivity
        // End this activity so that it's removed from the history
        // Otherwise pressing the back button in the game would come back to a blank screen here
        //finish();
        connect();
    }

    // The functions below are stubs that will be filled out in Checkpoint 2

    /**
     * Starts an attempt to connect to the server to fetch/refresh games.
     */
    private void connect() {
        // Make any "loading" UI adjustments you like
        // Use WebApi.startRequest to fetch the games lists
        // In the response callback, call setUpUi with the received data
        WebApi.startRequest(this, WebApi.API_BASE + "/games", response -> {
            // Code in this handler will run when the request completes successfully
            // Do something with the response?
            if (response != null) {
                setUpUi(response);
            }
        }, error -> {
            // Code in this handler will run if the request fails
            // Maybe notify the user of the error?
                Toast.makeText(this, "Oh no!", Toast.LENGTH_LONG).show();
            });
    }

    /**
     * Populates the games lists UI with data retrieved from the server.
     * @param result parsed JSON from the server
     */
    private void setUpUi(final JsonObject result) {
        // Hide any optional "loading" UI you added
        // Clear the games lists
        // Add UI chunks to the lists based on the result data

        LinearLayout invitationGroup = findViewById(R.id.invitationsGroup);
        invitationGroup.setVisibility(View.GONE);
        LinearLayout parent = findViewById(R.id.invitationsList);
        parent.removeAllViews();
        LinearLayout ongoingGroup = findViewById(R.id.ongoingGamesGroup);
        ongoingGroup.setVisibility(View.GONE);
        LinearLayout ongoingList = findViewById(R.id.ongoingGamesList);
        ongoingList.removeAllViews();



        JsonArray games = result.get("games").getAsJsonArray();
        for (JsonElement gameelement: games) {
            JsonArray players = gameelement.getAsJsonObject().get("players").getAsJsonArray();

            for (JsonElement playerelement: players) {
                JsonObject newPlayer = playerelement.getAsJsonObject();


                String gameId = gameelement.getAsJsonObject().get("id").getAsString();
                String owner = gameelement.getAsJsonObject().get("owner").getAsString();
                int state = gameelement.getAsJsonObject().get("state").getAsInt();
                String mode = gameelement.getAsJsonObject().get("mode").getAsString();

                String email = newPlayer.get("email").getAsString();
                int team = newPlayer.get("team").getAsInt();
                String teamId = null;
                if (team == TeamID.OBSERVER) {
                    teamId = "Observer";
                } else if (team == TeamID.TEAM_RED) {
                    teamId = "Red";
                } else if (team == TeamID.TEAM_GREEN) {
                    teamId = "Green";
                } else if (team == TeamID.TEAM_YELLOW) {
                    teamId = "Yellow";
                } else if (team == TeamID.TEAM_BLUE) {
                    teamId = "Blue";
                }
                int currentState = newPlayer.get("state").getAsInt();

                if (newPlayer.get("email").getAsString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                    && state != GameStateID.ENDED) {
                    if (currentState == PlayerStateID.INVITED) {
                        invitationGroup.setVisibility(View.VISIBLE);

                        View invitationsChunk = getLayoutInflater().inflate(R.layout.chunk_invitations, parent, false);
                        TextView emailLabel = invitationsChunk.findViewById(R.id.invitationEmails);
                        emailLabel.setText("Created by " + owner);

                        TextView detailLabel = invitationsChunk.findViewById(R.id.invitationDetails);
                        detailLabel.setText(teamId + ", " + mode + " mode");

                        parent.addView(invitationsChunk);

                        Button acceptButton = invitationsChunk.findViewById(R.id.acceptButton);
                        acceptButton.setOnClickListener((View v) ->
                                WebApi.startRequest(this, WebApi.API_BASE + "/games/" + gameId + "/accept",
                                        Request.Method.POST, null, response -> connect(), error -> {
                                    }));
                        Button declineButton = invitationsChunk.findViewById(R.id.declineButton);
                        declineButton.setOnClickListener((View v) ->
                                WebApi.startRequest(this, WebApi.API_BASE + "/games/" + gameId + "/decline",
                                        Request.Method.POST, null, response -> connect(), error -> {
                                    }));
                    }
                    if (currentState == PlayerStateID.ACCEPTED || currentState == PlayerStateID.PLAYING) {

                        View ongoingGamesChunk = getLayoutInflater().inflate(R.layout.chunk_ongoing_game, ongoingList,
                                false);
                        Button leaveButton = ongoingGamesChunk.findViewById(R.id.Leave);
                        if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(owner)) {
                            leaveButton.setVisibility(View.GONE);
                        }
                        TextView emailLabel = ongoingGamesChunk.findViewById(R.id.ongoingEmails);
                        emailLabel.setText("Created by " + owner);

                        TextView detailLabel = ongoingGamesChunk.findViewById(R.id.ongoingDetails);
                        detailLabel.setText(teamId + ", " + mode + " mode");
                        ongoingList.addView(ongoingGamesChunk);

                        Button enterButton = ongoingGamesChunk.findViewById(R.id.Enter);
                        enterButton.setOnClickListener(v -> enterGame(gameId));


                        leaveButton.setOnClickListener((View v) ->
                                WebApi.startRequest(this, WebApi.API_BASE + "/games/" + gameId + "/leave",
                                        Request.Method.POST, null, response -> connect(), error -> {
                                    }));
                        ongoingGroup.setVisibility(View.VISIBLE);

                    }
                }
            }
        }


        //View ongoingListChunk = getLayoutInflater().inflate(R.layout.chunk_ongoing_game, ongoingList, false);



    }

    /**
     * Enters a game (shows the map).
     * @param gameId the ID of the game to enter
     */
    private void enterGame(final String gameId) {
        // Launch GameActivity with the game ID in an intent extra
        // Do not finish - the user should be able to come back here
        Intent launch = new Intent(this, GameActivity.class);
        launch.putExtra("game", gameId);
        startActivity(launch);
    }

}
