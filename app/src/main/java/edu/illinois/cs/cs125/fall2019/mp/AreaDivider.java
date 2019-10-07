package edu.illinois.cs.cs125.fall2019.mp;
import androidx.annotation.VisibleForTesting;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 *Divides a rectangular area into identically sized, roughly square cells.
 *Each cell is given an X and Y coordinate. X increases from the west boundary toward the east boundary;
 *Y increases from south to north. So (0, 0) is the cell in the southwest corner.
 *(0, 1) is the cell just north of the southwestern corner cell.
 *Instances of this class are created with a desired cell size.
 *However, it is unlikely that the area dimensions will be an exact multiple of that length,
 *so placing fully sized cells would leave a small "sliver" on the east or north side.
 *Length should be redistributed so that each cell is exactly the same size.
 *If the area is 70 meters long in one dimension and the cell size is 20 meters,
 *there will be four cells in that dimension (there's room for three full cells plus a 10m sliver),
 *each of which is 70 / 4 = 17.5 meters long. Redistribution happens independently for the two dimensions,
 *so a 70x40 area would be divided into 17.5x20.0 cells with a 20m cell size.

*/

public class AreaDivider {
    /**
     * North Boundary.
     */
    private double north;
    /**
     * South Boundary.
     */
    private double south;
    /**
     * East Boundary.
     */
    private double east;
    /**
     * West Boundary.
     */
    private double west;
    /**
     * Cell Size.
     */
    private double cellSize;

    /**
     * Creates an Area Divider for an area.
     * @param setNorth North Latitude
     * @param setSouth South Latitude
     * @param setEast East Longitude
     * @param setWest West Longitude
     * @param setCellSize specified length of the cell
     */
    public AreaDivider(final double setNorth, final double setEast,
                       final double setSouth, final double setWest,
                       final double setCellSize) {
        this.north = setNorth;
        this.south = setSouth;
        this.east = setEast;
        this.west = setWest;
        this.cellSize = setCellSize;
    }

    /**
     * Gets the number of cells between the west and east boundaries.
     * See the class description for more details on area division.
     * @return xCells.
     */

    public int getXCells() {
        double distance = LatLngUtils.distance(south, west, south, east);
        double xCell = distance / cellSize;
        return (int) Math.ceil(xCell);
    }

    /**
     * Gets the number of cells between the south and north boundaries.
     * See the class description for more details on area division.
     * @return yCells.
     */

    public int getYCells() {
        double distance = LatLngUtils.distance(south, west, north, west);
        double yCell = distance / cellSize;
        return (int) Math.ceil(yCell);
    }

    /**
     * Gets the X coordinate of the cell containing the specified location.
     * The point is not necessarily within the area. If it is not, the return value is up to you.
     * However, you will want some way to tell from GameActivity that the location is not in a
     * valid cell.
     * Allowing the returned coordinate to be negative or >= getXCells() is fine, as is always
     * returning a sentinel value like -1 for an out-of-bounds location.
     *
     * Parameters:
     * @param location The location as a LatLng object
     * @return xCoordinate of the cell with the correct LatLng.
     */

    public int getXCoordinate(final com.google.android.gms.maps.model.LatLng location) {
        double xDist = (east - west) / getXCells();
        double xLength = location.longitude - west;
        double xCoordinate = xLength / xDist;
        if (xCoordinate < 0) {
            return -1;
        }
        return (int) xCoordinate;

    }

    /**
     * Gets the Y coordinate of the cell containing the specified location.
     * The point is not necessarily within the area. If it is not, the return value is up to you.
     * However, you will want some way to tell from GameActivity that the location is not in a
     * valid cell.
     * Allowing the returned coordinate to be negative or >= getYCells() is fine, as is always
     * returning a sentinel value like -1 for an out-of-bounds location.
     * @param location The location as a LatLng object
     * @return yCoordinate of the cell with the correct LatLng.
     */

    public int getYCoordinate(final com.google.android.gms.maps.model.LatLng location) {
        double yDist = (north - south) / getYCells();
        double yLength = location.latitude - south;
        double yCoordinate = yLength / yDist;
        if (yCoordinate < 0) {
            return -1;
        }
        return (int) yCoordinate;

    }

    /**
     * Gets the boundary of the specified cell as a Google Maps LatLngBounds Object.
     * @param x xCoordinate
     * @param y yCoordinate
     * @return the boundaries of the cell.
     */

    public LatLngBounds getCellBounds(final int x, final int y) {
        double xDist = (east - west) / getXCells();
        double yDist = (north - south) / getYCells();
        LatLng southWest = new LatLng((south + yDist * y), (west + xDist * x));
        LatLng northEast = new LatLng((south + yDist * (y + 1)), (west + xDist * (x + 1)));
        return new LatLngBounds(southWest, northEast);
    }

    /**
     * Adds a colored line to the map.
     * @param startLat Latitude of first coordinate.
     * @param startLong Longitude of first coordinate.
     * @param endLat Latitude of the second coordinate.
     * @param endLong Longitude of the second coordinate.
     * @param map the Google Map to draw on.
     */
    @VisibleForTesting

    public void addLine(final double startLat, final double startLong,
                         final double endLat, final double endLong,
                         final com.google.android.gms.maps.GoogleMap map) {
        LatLng start = new LatLng(startLat, startLong);
        LatLng end = new LatLng(endLat, endLong);

        final int linePx = 12;
        PolylineOptions fill = new PolylineOptions().add(start, end).width(linePx).zIndex(1);
        map.addPolyline(fill);
    }

    /**
     * Draws the grid to a map using solid black polylines.
     * There should be one line on each of the four boundaries of the overall area and as many
     * internal lines as necessary to divide the rows and columns of the grid.
     * Each line should span the whole width or height of the area rather than the side of just one
     * cell.
     * For example, an area divided into a 2x3 grid would be drawn with 7 lines total:
     * 4 for the outer boundaries, 1 vertical line to divide the west half from the east half
     * (2 columns), and 2 horizontal lines to divide the area into 3 rows.
     * See the provided addLine function from GameActivity for how to add a line to the map.
     * Since these lines should be black, you do not need the extra line to make the line appear
     * to have a border.
     * @param map Google Map used for rendering
     */

    public void renderGrid(final com.google.android.gms.maps.GoogleMap map) {
        double xDist = (east - west) / getXCells();
        double yDist = (north - south) / getYCells();
        for (int i = 0; i <= getXCells(); i++) {
            addLine(north, (xDist * i + west), south, (xDist * i + west), map);
        }
        for (int i = 0; i <= getYCells(); i++) {
            addLine((yDist * i + south), west, (yDist * i + south), east, map);
        }
    }

}
