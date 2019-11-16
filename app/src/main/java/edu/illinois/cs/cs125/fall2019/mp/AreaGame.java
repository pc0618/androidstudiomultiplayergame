package edu.illinois.cs.cs125.fall2019.mp;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.WebSocket;



/**
 * Represents an area mode game. Keeps track of cells and the player's most recent capture.
 * <p>
 * All these functions are stubs that you need to implement.
 * Feel free to add any private helper functions that would be useful.
 * See {@link TargetGame} for an example of how multiplayer games are handled.
 */
public final class AreaGame extends Game {

    // You will probably want some instance variables to keep track of the game state
    // (similar to the area mode gameplay logic you previously wrote in GameActivity)


    /**  Stores an AreaDivider created in the constructor .*/
    private AreaDivider divider;

    /** 2D array storing positions of captured cells. */
    private int[][] classCells;

    /** Last updated x index. */
    private int lastX = -1;

    /** Last updated y index. */
    private int lastY = -1;

    /** North latlng. */
    private double areaNorth;

    /** East latlng. */
    private double areaEast;

    /** South latlng. */
    private double areaSouth;

    /** West latlng. */
    private double areaWest;

    /** Cell size parsed from Jsonobject. */
    private double cellSize;

    /** 2D array storing positions of captured cells. */

    /**
     * Creates a game in area mode.
     * <p>
     * Loads the current game state from JSON into instance variables and populates the map
     * to show existing cell captures.
     * @param email the user's email
     * @param map the Google Maps control to render to
     * @param webSocket the websocket to send updates to
     * @param fullState the "full" update from the server
     * @param context the Android UI context
     */
    public AreaGame(final String email, final GoogleMap map, final WebSocket webSocket,
                    final JsonObject fullState, final Context context) {
        super(email, map, webSocket, fullState, context);
        areaNorth = fullState.get("areaNorth").getAsDouble();
        areaEast = fullState.get("areaEast").getAsDouble();
        areaSouth = fullState.get("areaSouth").getAsDouble();
        areaWest = fullState.get("areaWest").getAsDouble();
        cellSize = fullState.get("cellSize").getAsDouble();

        divider = new AreaDivider(areaNorth, areaEast, areaSouth, areaWest, cellSize);
        classCells = new int[divider.getXCells()][divider.getYCells()];
        divider.renderGrid(map);
        JsonArray cells = fullState.getAsJsonArray("cells");
        for (JsonElement element: cells) {
            JsonObject obj = element.getAsJsonObject();
            PolygonOptions options = new PolygonOptions();
            int currX = obj.get("x").getAsInt();
            int currY = obj.get("y").getAsInt();
            int teamNum = obj.get("team").getAsInt();
            classCells[currX][currY] = teamNum;
            LatLngBounds latlng = divider.getCellBounds(currX, currY);
            LatLng southeast = new LatLng(latlng.southwest.latitude, latlng.northeast.longitude);
            LatLng northwest = new LatLng(latlng.northeast.latitude, latlng.southwest.longitude);
            int[] colors = getContext().getResources().getIntArray(R.array.team_colors);
            int color = colors[teamNum];
            PolygonOptions opt = new PolygonOptions();
            opt.add(northwest, latlng.northeast, southeast, latlng.southwest);
            opt.fillColor(color);
            getMap().addPolygon(opt);

        }
        JsonArray players = fullState.get("players").getAsJsonArray();
        for (JsonElement element: players) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.get("email").getAsString().equals(getEmail())) {
                JsonArray paths = obj.get("path").getAsJsonArray();
                if (paths.size() > 0) {
                    JsonObject finalobj = paths.get(paths.size() - 1).getAsJsonObject();
                    lastX = finalobj.get("x").getAsInt();
                    lastY = finalobj.get("y").getAsInt();
                }

            }
        }

    }



    /**
     * Called when the user's location changes.
     * <p>
     * Area mode games detect whether the player is in an uncaptured cell. Capture is possible if
     * the player has no captures yet or if the cell shares a side with the previous cell captured by
     * the player. If capture occurs, a polygon with the team color is added to the cell on the map
     * and a cellCapture update is sent to the server.
     * @param location a location FusedLocationProviderClient is reasonably confident about
     */
    @Override
    public void locationUpdated(final LatLng location) {
        int currentX = divider.getXCoordinate(location);
        int currentY = divider.getYCoordinate(location);
        if (currentX < 0 || currentX >= divider.getXCells() || currentY < 0 || currentY >= divider.getYCells()) {
            return;
        }
        if (lastX != -1 || lastY != -1) {
            if (classCells[currentX][currentY] == 0) {
                if ((Math.abs(currentX - lastX) + Math.abs(currentY - lastY)) == 1) {
                    classCells[currentX][currentY] = getMyTeam();
                    lastX = currentX;
                    lastY = currentY;
                    int[] colors = getContext().getResources().getIntArray(R.array.team_colors);
                    LatLng northwest = new LatLng(divider.getCellBounds(lastX, lastY).northeast.latitude,
                            divider.getCellBounds(lastX, lastY).southwest.longitude);
                    LatLng southeast = new LatLng(divider.getCellBounds(lastX, lastY).southwest.latitude,
                            divider.getCellBounds(lastX, lastY).northeast.longitude);


                    getMap().addPolygon(new PolygonOptions().add(northwest,
                            divider.getCellBounds(lastX, lastY).northeast, southeast,
                            divider.getCellBounds(lastX, lastY).southwest).fillColor(colors[getMyTeam()]));
                    JsonObject obj = new JsonObject();

                    obj.addProperty("type", "cellCapture");
                    obj.addProperty("x", currentX);
                    obj.addProperty("y", currentY);
                    sendMessage(obj.toString());
                }
            }
        }

    }

    /**
     * Processes an update from the server.
     * <p>
     * Since playerCellCapture events are specific to area mode games, this function handles those
     * by placing a polygon of the capturing player's team color on the newly captured cell and
     * recording the cell's new owning team.
     * All other message types are delegated to the superclass.
     * @param message JSON from the server
     * @param type the update type
     * @return whether the message type was recognized
     */
    @Override
    public boolean handleMessage(final JsonObject message, final String type) {
        if (type.equals("playerCellCapture")) {
            int team = message.get("team").getAsInt();
            int x = message.get("x").getAsInt();
            int y = message.get("y").getAsInt();
            classCells[x][y] = team;
            int[] colors = getContext().getResources().getIntArray(R.array.team_colors);
            PolygonOptions opt = new PolygonOptions();
            LatLngBounds latlng = divider.getCellBounds(x, y);

            LatLng southeast = new LatLng(latlng.southwest.latitude, latlng.northeast.longitude);
            LatLng northwest = new LatLng(latlng.northeast.latitude, latlng.southwest.longitude);

            getMap().addPolygon(new PolygonOptions().add(northwest, divider.getCellBounds(x, y).northeast, southeast,
                    divider.getCellBounds(x, y).southwest).fillColor(colors[message.get("team").getAsInt()]));

            //opt.add(northwest, southeast);
            //opt.fillColor(color);
            //getMap().addPolygon(opt);
            return true;
        }
        return false;
    }

    /**
     * Gets a team's score in this area mode game.
     * @param teamId the team ID
     * @return the number of cells owned by the team
     */
    @Override
    public int getTeamScore(final int teamId) {
        int count = 0;
        for (int i = 0; i < divider.getXCells(); i++) {
            for (int j = 0; j < divider.getYCells(); j++) {
                if (classCells[i][j] == teamId) {
                    count++;
                }
            }
        }
        return count;
    }
}
