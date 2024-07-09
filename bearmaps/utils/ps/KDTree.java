package bearmaps.utils.ps;

import java.util.List;

public class KDTree implements PointSet {

    private KDTreeNode root;

    public KDTree(List<Point> points) {
        for (Point p : points) {
            root = insert(root, p, true);
        }
    }

    private KDTreeNode insert(KDTreeNode node, Point point, boolean layer) {
        KDTreeNode insertion = new KDTreeNode(point, null, null, false);

        if (node == null) {
            return new KDTreeNode(point, null, null, layer);
        } else if (insertion.compareTo(node) < 0) {
            node.left = insert(node.left, point, !layer);
        } else if (insertion.compareTo(node) == 0) {
            if (node.left == null && node.right != null) {
                node.left = new KDTreeNode(point, null, null, !layer);
            } else if (node.left != null && node.right == null) {
                node.right = new KDTreeNode(point, null, null, !layer);
            } else {
                return null;
            }
        } else {
            node.right = insert(node.right, point, !layer);
        }
        return node;
    }

    @Override
    public Point nearest(double x, double y) {
        return nearestHelper(root, new Point(x, y), null, Double.MAX_VALUE);
    }

    public Point nearestHelper(KDTreeNode node, Point point, Point closest, double minDistance) {
        if (node == null) {
            return closest;
        }

        double distance = Point.distance(point, node.point);
        KDTreeNode pointNode = new KDTreeNode(point, null, null, false);

        if (node == null) {
            return closest;
        }

        if (distance < minDistance) {
            minDistance = distance;
            closest = node.point;
        }

        if (pointNode.compareTo(node) < 0) {
            closest = nearestHelper(node.left, point, closest, minDistance);
        } else {
            closest = nearestHelper(node.right, point, closest, minDistance);
        }

        distance = Point.distance(closest, point);


        //New recursion for next best if there exists (Explore the bad side)

        double distanceToEdge;

        //Edge point denotes if there can be an even closer distance
        Point edge;
        if (node.layer) {
            edge = new Point(node.point.getX(), pointNode.point.getY());
        } else {
            edge = new Point(pointNode.point.getX(), node.point.getY());
        }
        distanceToEdge = Point.distance(edge, pointNode.point);

        //If distance to edge is shorter than our minDistance, then let's explore the "bad" side
        if (distanceToEdge < distance) {
            if (pointNode.compareTo(node) < 0) {
                closest = nearestHelper(node.right, point, closest, distance);
            } else {
                closest = nearestHelper(node.left, point, closest, distance);
            }
        }

        return closest;
    }

    /**
     * if layer is true, it is x layer, false, then it is y layer
     *
     * @param
     */
    private class KDTreeNode implements Comparable<KDTreeNode> {

        private Point point;
        private KDTreeNode left;
        private KDTreeNode right;
        private boolean layer;

        public KDTreeNode(Point point, KDTreeNode left, KDTreeNode right, boolean layer) {
            this.point = point;
            this.left = left;
            this.right = right;
            this.layer = layer;
        }

        @Override
        public int compareTo(KDTreeNode node) {
            if (node.layer) {
                return Double.compare(this.point.getX(), node.point.getX());
            } else {
                return Double.compare(this.point.getY(), node.point.getY());
            }
        }
    }
}
