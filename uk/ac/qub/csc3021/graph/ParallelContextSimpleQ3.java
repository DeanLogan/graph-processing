package uk.ac.qub.csc3021.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ParallelContextSimpleQ3 extends ParallelContext {
    private final int threadId;
    private final SparseMatrix matrix;
    private final Relax relax;

    private class ThreadSimple extends Thread {
        private final int threadId;
        private final SparseMatrix matrix;
        private final Relax relax;

        public ThreadSimple(int threadId, SparseMatrix matrix, Relax relax) {
            this.threadId = threadId;
            this.matrix = matrix;
            this.relax = relax;
        }

        public void run() {
            int numberOfVertices = matrix.getNumVertices();
            int numberOfThreads = getNumThreads();

            // Calculate the range of vertices for this thread
            int verticesPerThread = numberOfVertices / numberOfThreads;
            int startVertex = threadId * verticesPerThread;
            int endVertex = (threadId == numberOfThreads - 1) ? numberOfVertices : (threadId + 1) * verticesPerThread;

            // Perform edgemap for the specified range of vertices
            matrix.ranged_edgemap(relax, startVertex, endVertex);
        }
    };
    public ParallelContextSimpleQ3(int num_threads_) {
        super(num_threads_);
    }

    public void terminate() {
        // Perform any termination actions if needed
    }

    public void edgemap(SparseMatrix matrix, Relax relax) {
        int numberOfThreads = getNumThreads();
        ThreadSimple[] threads = new ThreadSimple[numberOfThreads];
        final int blockSize = 128; // Adjust this value based on performance

        // Producer thread reads edges from file
        Thread producerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader("your_edge_file.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Parse the edge information and put it into the queue
                    Edge edge = parseEdge(line);
                    matrix.putEdgeIntoQueue(edge);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Signal the end of input to consumers
            matrix.signalEndOfInput();
        });

        producerThread.start();

        // Consumer threads call edgemap on edges
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new ThreadSimple(i, matrix, relax, blockSize);
            threads[i].start();
        }

        // Wait for the producer and consumer threads to finish
        try {
            producerThread.join();
            for (int i = 0; i < numberOfThreads; i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Helper method to parse an edge from a string
    private Edge parseEdge(String line) {
        // Implement this based on your edge representation in the file
        // For example, assuming the file contains two integers separated by a space
        String[] parts = line.split(" ");
        int src = Integer.parseInt(parts[0]);
        int dst = Integer.parseInt(parts[1]);
        return new Edge(src, dst);
    }
}
