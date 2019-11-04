package edu.illinois.cs.cs125.fall2019.mp;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This class provides refactored code representing targets on the map.
 */
public class Target {

    /** Stores the current map as a GoogleMap. */
    private GoogleMap targetsMap;
    /** Stores the current position as a LatLng Object. */
    private LatLng targetPosition;
    /** Stores the current TeamId. */
    private int targetTeamID;
    /** Stores the current target marker. */
    private Marker targetMarker;

    /**
     *
     * @param setMap sets current map status.
     * @param setPosition sets current position.
     * @param setTeamId sets current team id.
     */

    public Target(final GoogleMap setMap, final LatLng setPosition, final int setTeamId) {
        targetsMap = setMap;
        targetPosition = setPosition;
        targetTeamID = setTeamId;
        MarkerOptions options = new MarkerOptions().position(setPosition);
        targetMarker = targetsMap.addMarker(options);
        if (targetTeamID == TeamID.OBSERVER) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            targetMarker.setIcon(icon);
        } else if (targetTeamID == TeamID.TEAM_RED) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            targetMarker.setIcon(icon);
        } else if (targetTeamID == TeamID.TEAM_GREEN) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            targetMarker.setIcon(icon);
        } else if (targetTeamID == TeamID.TEAM_YELLOW) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            targetMarker.setIcon(icon);
        } else if (targetTeamID == TeamID.TEAM_BLUE) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            targetMarker.setIcon(icon);
        }

    }

    /**
     *
     * @param newTeam updates targetteamid with new team id.
     */

    public void setTeam(final int newTeam) {
        targetTeamID = newTeam;
        if (targetTeamID == TeamID.OBSERVER) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            targetMarker.setIcon(icon);
        } else if (targetTeamID == TeamID.TEAM_RED) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            targetMarker.setIcon(icon);
        } else if (targetTeamID == TeamID.TEAM_GREEN) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            targetMarker.setIcon(icon);
        } else if (targetTeamID == TeamID.TEAM_YELLOW) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            targetMarker.setIcon(icon);
        } else if (targetTeamID == TeamID.TEAM_BLUE) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            targetMarker.setIcon(icon);
        }
    }

    /**
     *
     * @return returns target position.
     */

    public LatLng getPosition() {
        return targetPosition;
    }

    /**
     *
     * @return returns target team id.
     */

    public int getTeam() {
        return targetTeamID;
    }

}
