package bearmaps;

import bearmaps.utils.graph.AStarSolver;
import bearmaps.utils.graph.WeightedEdge;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class acts as a helper for the RoutingAPIHandler.
 *
 * @author Josh Hug, Brian Park
 */
public class Router {

    /**
     * Overloaded method for shortestPath that has flexibility to specify a solver
     * and returns a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination location.
     *
     * @param g       The graph to use.
     * @param stlon   The longitude of the start location.
     * @param stlat   The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */
    public static List<Long> shortestPath(AugmentedStreetMapGraph g, double stlon, double stlat,
                                          double destlon, double destlat) {
        long src = g.closest(stlon, stlat);
        long dest = g.closest(destlon, destlat);
        return new AStarSolver<>(g, src, dest, 20).solution();

    }

    /**
     * Create the list of directions corresponding to a route on the graph.
     *
     * @param g     The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(AugmentedStreetMapGraph graph, List<Long> path) {
        double distanceOnPreviousSegment = 0;
        int previousDirectionChange = 0; // Default to "Start".
        int currentDirectionChange;
        List<NavigationDirection> directionsList = new LinkedList<>();
        List<WeightedEdge<Long>> segments = fetchSegments(graph, path);

        if (segments.size() == 1) {
            NavigationDirection singleDirection = createDirection(previousDirectionChange, segments.get(0).getName(), segments.get(0).weight());
            directionsList.add(singleDirection);
            return directionsList;
        }

        for (int i = 1; i < segments.size(); i++) {
            WeightedEdge<Long> previousSegment = segments.get(i - 1);
            WeightedEdge<Long> currentSegment = segments.get(i);

            long previousNode = previousSegment.from();
            long currentNode = previousSegment.to();
            long nextNode = currentSegment.to();

            double[] previousPosition = getPosition(graph, previousNode);
            double[] currentPosition = getPosition(graph, currentNode);
            double[] nextPosition = getPosition(graph, nextNode);

            String previousSegmentName = previousSegment.getName() != null ? previousSegment.getName() : "unknown road";
            String currentSegmentName = currentSegment.getName() != null ? currentSegment.getName() : "unknown road";

            distanceOnPreviousSegment += previousSegment.weight();

            if (!currentSegmentName.equals(previousSegmentName)) {
                double previousBearing = NavigationDirection.bearing(previousPosition[0], currentPosition[0], previousPosition[1], currentPosition[1]);
                double currentBearing = NavigationDirection.bearing(currentPosition[0], nextPosition[0], currentPosition[1], nextPosition[1]);

                currentDirectionChange = NavigationDirection.getDirection(previousBearing, currentBearing);

                NavigationDirection direction = createDirection(previousDirectionChange, previousSegmentName, distanceOnPreviousSegment);
                previousDirectionChange = currentDirectionChange;

                directionsList.add(direction);
                distanceOnPreviousSegment = 0;
            }

            if (i == segments.size() - 1) {
                distanceOnPreviousSegment += currentSegment.weight();
                NavigationDirection finalDirection = createDirection(previousDirectionChange, currentSegmentName, distanceOnPreviousSegment);
                directionsList.add(finalDirection);
            }
        }
        return directionsList;
    }

    private static NavigationDirection createDirection(int direction, String segmentName, double segmentDistance) {
        NavigationDirection directionObj = new NavigationDirection();
        directionObj.direction = direction;
        directionObj.way = segmentName;
        directionObj.distance = segmentDistance;
        return directionObj;
    }

    private static List<WeightedEdge<Long>> fetchSegments(AugmentedStreetMapGraph graph, List<Long> path) {
        List<WeightedEdge<Long>> segments = new LinkedList<>();
        for (int i = 1; i < path.size(); i++) {
            long currentNode = path.get(i - 1);
            long nextNode = path.get(i);
            for (WeightedEdge<Long> edge : graph.neighbors(currentNode)) {
                if (edge.to().equals(nextNode)) {
                    segments.add(edge);
                }
            }
        }
        return segments;
    }

    private static double[] getPosition(AugmentedStreetMapGraph graph, long node) {
        return new double[]{graph.lon(node), graph.lat(node)};
    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for. This is only
     * useful for Part IV of the project.
     */
    public static class NavigationDirection {

        /**
         * Integer constants representing directions.
         */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /**
         * Number of directions supported.
         */
        public static final int NUM_DIRECTIONS = 8;

        /**
         * A mapping of integer values to directions.
         */
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /**
         * Default name for an unknown way.
         */
        public static final String UNKNOWN_ROAD = "unknown road";

        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /**
         * The direction a given NavigationDirection represents.
         */
        int direction;
        /**
         * The name of the way I represent.
         */
        String way;
        /**
         * The distance along this way I represent.
         */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         *
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        /**
         * Checks that a value is between the given ranges.
         */
        private static boolean numInRange(double value, double from, double to) {
            return value >= from && value <= to;
        }

        /**
         * Calculates what direction we are going based on the two bearings, which
         * are the angles from true north. We compare the angles to see whether
         * we are making a left turn or right turn. Then we can just use the absolute value of the
         * difference to give us the degree of turn (straight, sharp, left, or right).
         *
         * @param prevBearing A double in [0, 360.0]
         * @param currBearing A double in [0, 360.0]
         * @return the Navigation Direction type
         */
        private static int getDirection(double prevBearing, double currBearing) {
            double absDiff = Math.abs(currBearing - prevBearing);
            if (numInRange(absDiff, 0.0, 15.0)) {
                return NavigationDirection.STRAIGHT;

            }
            if ((currBearing > prevBearing && absDiff < 180.0)
                    || (currBearing < prevBearing && absDiff > 180.0)) {
                // we're going right
                if (numInRange(absDiff, 15.0, 30.0) || absDiff > 330.0) {
                    // bearmaps.proj2c.example of high abs diff is prev = 355 and curr = 2
                    return NavigationDirection.SLIGHT_RIGHT;
                } else if (numInRange(absDiff, 30.0, 100.0) || absDiff > 260.0) {
                    return NavigationDirection.RIGHT;
                } else {
                    return NavigationDirection.SHARP_RIGHT;
                }
            } else {
                // we're going left
                if (numInRange(absDiff, 15.0, 30.0) || absDiff > 330.0) {
                    return NavigationDirection.SLIGHT_LEFT;
                } else if (numInRange(absDiff, 30.0, 100.0) || absDiff > 260.0) {
                    return NavigationDirection.LEFT;
                } else {
                    return NavigationDirection.SHARP_LEFT;
                }
            }
        }

        /**
         * Returns the initial bearing (angle) between vertices v and w in degrees.
         * The initial bearing is the angle that, if followed in a straight line
         * along a great-circle arc from the starting point, would take you to the
         * end point.
         * Assumes the lon/lat methods are implemented properly.
         * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
         *
         * @param lonV The longitude of the first vertex.
         * @param latV The latitude of the first vertex.
         * @param lonW The longitude of the second vertex.
         * @param latW The latitude of the second vertex.
         * @return The initial bearing between the vertices.
         */
        public static double bearing(double lonV, double lonW, double latV, double latW) {
            double phi1 = Math.toRadians(latV);
            double phi2 = Math.toRadians(latW);
            double lambda1 = Math.toRadians(lonV);
            double lambda2 = Math.toRadians(lonW);

            double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
            double x = Math.cos(phi1) * Math.sin(phi2);
            x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
            return Math.toDegrees(Math.atan2(y, x));
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
