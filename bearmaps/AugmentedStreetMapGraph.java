package bearmaps;

import bearmaps.utils.Constants;
import bearmaps.utils.graph.streetmap.Node;
import bearmaps.utils.graph.streetmap.StreetMapGraph;
import bearmaps.utils.ps.KDTree;
import bearmaps.utils.ps.Point;
import bearmaps.utils.trie.MyTrieSet;

import java.util.*;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 * @author Alan Yao, Josh Hug, ________
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {

    /**
     * Scale factor at the natural origin, Berkeley. Prefer to use 1 instead of 0.9996 as in UTM.
     *
     * @source https://gis.stackexchange.com/a/7298
     */
    private static final double K0 = 1.0;
    /**
     * Latitude centered on Berkeley.
     */
    private static final double ROOT_LAT = (Constants.ROOT_ULLAT + Constants.ROOT_LRLAT) / 2;
    /**
     * Longitude centered on Berkeley.
     */
    private static final double ROOT_LON = (Constants.ROOT_ULLON + Constants.ROOT_LRLON) / 2;
    KDTree kdTree;
    List<Point> points;
    HashMap<Point, Node> pointNodePair;
    //MyTrieSet trie;
    MyTrieSet trie;
    HashMap<String, HashMap<Node, String>> cleanNameNodeNamePair;
    HashMap<String, String> cleanNameNamePair;

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        // You might find it helpful to uncomment the line below:
        List<Node> nodes = this.getNodes();
        points = new ArrayList<>();
        pointNodePair = new HashMap<>();
        cleanNameNodeNamePair = new HashMap<>();
        cleanNameNamePair = new HashMap<>();
        trie = new MyTrieSet();

        for (Node node : nodes) {

            double x = projectToX(node.lon(), node.lat());
            double y = projectToY(node.lon(), node.lat());

            Point p = new Point(x, y);
            points.add(p);
            pointNodePair.put(p, node);
        }

        for (Node node : getAllNodes()) {
            //Add names to the trie
            if (node.name() != null) {
                trie.add(cleanString(node.name()));

                HashMap<Node, String> temp = cleanNameNodeNamePair.get(cleanString(node.name()));

                if (temp == null) {
                    temp = new HashMap<>();
                }

                temp.put(node, node.name());

                cleanNameNodeNamePair.put(cleanString(node.name()), temp);
                cleanNameNamePair.put(cleanString(node.name()), node.name());
            }
        }

        kdTree = new KDTree(points);
    }

    /**
     * Return the Euclidean x-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     *
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean x-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToX(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double b = Math.sin(dlon) * Math.cos(phi);
        return (K0 / 2) * Math.log((1 + b) / (1 - b));
    }

    /**
     * Return the Euclidean y-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     *
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean y-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToY(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double con = Math.atan(Math.tan(phi) / Math.cos(dlon));
        return K0 * (con - Math.toRadians(ROOT_LAT));
    }

    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * For Project Part III
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        double x = projectToX(lon, lat);
        double y = projectToY(lon, lat);
        //return 0;

        Point nearestPoint = kdTree.nearest(x, y);
        Node n = pointNodePair.get(nearestPoint);

        while (neighbors(n.id()).size() == 0) {
            nearestPoint = kdTree.nearest(nearestPoint.getX(), nearestPoint.getY());
            n = pointNodePair.get(nearestPoint);
        }

        return n.id();
    }

    /**
     * For Project Part IV (extra credit)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {

        List<String> locations = new LinkedList<>();
        //List<String> keys = trie.keysWithPrefix(cleanString(prefix));

        List<String> strings = new ArrayList<>();
        try {
            strings = trie.keysWithPrefix(cleanString(prefix));
        } catch (NullPointerException e) {

        }

        for (String query : strings) {
            HashMap<Node, String> pair = cleanNameNodeNamePair.get(query);

            for (Node n : pair.keySet()) {
                locations.add(n.name());
            }
        }


        return locations;
    }

    /**
     * For Project Part IV (extra credit)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     *
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {

        List<Map<String, Object>> locationsInfo = new LinkedList<>();

        String location = trie.keysThatMatch(cleanString(locationName));

        if (locationName.equals("")) {
            location = "";
        }

        String name = cleanNameNamePair.get(location);

        HashMap<Node, String> pair = cleanNameNodeNamePair.get(location);

        if (pair != null && !pair.isEmpty()) {
            for (Node n : pair.keySet()) {
                Map<String, Object> info = new HashMap<>();
                info.put("lat", n.lat());
                info.put("lon", n.lon());
                info.put("name", n.name());
                info.put("id", n.id());

                locationsInfo.add(info);

            }
        }
        return locationsInfo;
    }

}
