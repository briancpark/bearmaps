package bearmaps.utils.graph;

import bearmaps.utils.pq.MinHeapPQ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {


    double totalWeight;

    MinHeapPQ<Vertex> fringe;
    HashMap<Vertex, Double> distTo;
    HashMap<Vertex, Vertex> edgeTo;


    List<Vertex> shortestPath;


    double currentTime;
    double stopTime;
    int statesExplored;
    SolverOutcome outcome;


    public AStarSolver(AStarGraph<Vertex> input, Vertex start, Vertex end, double timeout) {
        //setup
        currentTime = System.currentTimeMillis() / 1000.0;
        stopTime = timeout + (System.currentTimeMillis() / 1000.0); //the timeout

        statesExplored = 0;

        fringe = new MinHeapPQ<>();
        distTo = new HashMap<>();
        edgeTo = new HashMap<>();
        shortestPath = new ArrayList<>();


        fringe.insert(start, input.estimatedDistanceToGoal(start, end));
        distTo.put(start, 0.0);


        while (fringe.size() > 0 && !fringe.peek().equals(end)) {
            currentTime = System.currentTimeMillis() / 1000.0;
            if (currentTime > stopTime) {
                outcome = SolverOutcome.TIMEOUT;
                break;
            }

            Vertex v = fringe.poll();
            statesExplored++;

            double p = distTo.get(v) + input.estimatedDistanceToGoal(v, end); //priority value

            for (WeightedEdge<Vertex> e : input.neighbors(v)) {
                //relax e

                Vertex pp = e.from();
                Vertex qq = e.to();
                double ww = e.weight();

                if (!distTo.containsKey(qq)) {
                    distTo.put(qq, Double.MAX_VALUE);
                }

                if (distTo.get(pp) + ww < distTo.get(qq)) {
                    distTo.put(qq, distTo.get(pp) + ww);
                    if (fringe.contains(qq)) {
                        fringe.changePriority(qq, distTo.get(qq) + input.estimatedDistanceToGoal(qq, end));
                    } else {
                        fringe.insert(qq, distTo.get(qq) + input.estimatedDistanceToGoal(qq, end));
                    }
                    edgeTo.put(qq, pp);
                }
            }

        }

        outcome = SolverOutcome.SOLVED;


        /*
        //Dijkstras shortest path collector //lab18


        shortestPath.add(stop);
        while (predecessor.get(stop) != start) {
            stop = predecessor.get(stop);
            shortestPath.add(stop);
        }
        shortestPath.add(start);

        Collections.reverse(shortestPath);
         */

        if (distTo.get(end) == null) {
            outcome = SolverOutcome.UNSOLVABLE;
            totalWeight = 0;
        } else {
            totalWeight = distTo.get(end);

            shortestPath.add(end);

            while (!edgeTo.get(end).equals(start)) {
                end = edgeTo.get(end);
                shortestPath.add(end);
            }
            shortestPath.add(start);

            Collections.reverse(shortestPath);
        }


    }

    public SolverOutcome outcome() {
        return outcome;
    }

    public List<Vertex> solution() {
        return shortestPath;
    }

    public double solutionWeight() {
        if (outcome().equals(SolverOutcome.SOLVED)) {
            return totalWeight;
        }
        return 0;
    }

    public int numStatesExplored() {
        return statesExplored;
    }

    public double explorationTime() {
        return currentTime;
    }
}
