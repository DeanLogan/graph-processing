package uk.ac.qub.csc3021.graph;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

// This class represents the adjacency matrix of a graph as a sparse matrix
// in coordinate format (COO)
public class SparseMatrixCOO extends SparseMatrix {
    // DONE: variable declarations
    int[] source;
    int[] destination;
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph

    public SparseMatrixCOO(String file) {
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader rd = new BufferedReader(is);
            readFile(rd);
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e);
            return;
        } catch (UnsupportedEncodingException e) {
            System.err.println("Unsupported encoding exception: " + e);
            return;
        } catch (Exception e) {
            System.err.println("Exception: " + e);
            return;
        }
    }

    int getNext(BufferedReader rd) throws Exception {
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        return Integer.parseInt(line);
    }

    void getNextPair(BufferedReader rd, int pair[]) throws Exception {
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        StringTokenizer st = new StringTokenizer(line);
        pair[0] = Integer.parseInt(st.nextToken());
        pair[1] = Integer.parseInt(st.nextToken());
    }

    void readFile(BufferedReader rd) throws Exception {
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        if (!line.equalsIgnoreCase("COO"))
            throw new Exception("file format error -- header");

        num_vertices = getNext(rd);
        num_edges = getNext(rd);

        // DONE: Allocate memory for the COO representation
        source = new int[num_edges];
        destination = new int[num_edges];

        int edge[] = new int[2];
        for (int i = 0; i < num_edges; ++i) {
            getNextPair(rd, edge);
            // DONE: Insert edge with source edge[0] and destination edge[1]
            source[i] = edge[0];
            destination[i] = edge[1];
        }
    }

    // Return number of vertices in the graph
    public int getNumVertices() {
        return num_vertices;
    }

    // Return number of edges in the graph
    public int getNumEdges() {
        return num_edges;
    }

// Auxiliary function for PageRank calculation
    public void calculateOutDegree(int outdeg[]) {
        // Initialize out-degrees to zero
        for (int i = 0; i < num_vertices; i++) {
            outdeg[i] = 0;
        }

        // Count out-degrees based on the COO representation
        for (int i = 0; i < num_edges; i++) {
            int srcVertex = source[i];
            outdeg[srcVertex]++;
        }
    }

    // Perform one sweep over all edges in the graph, calling the functional
    // interface Relax once for each edge.
    public void edgemap(Relax relax) {
        for (int i = 0; i < num_edges; i++) {
            int srcVertex = source[i];
            int dstVertex = destination[i];

            // Call the relax operation for the edge (srcVertex, dstVertex)
            relax.relax(srcVertex, dstVertex);
        }
    }

    public void ranged_edgemap(Relax relax, int from, int to) {
        int numEdges = to - from;
        int numThreads = ParallelContextHolder.get().getNumThreads();
        
        // Calculate the number of edges to process per thread
        int edgesPerThread = numEdges / numThreads;
        
        // Create an array to hold the threads
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            // Calculate the range of edges for this thread
            final int start = from + threadId * edgesPerThread;
            final int end = (threadId == numThreads - 1) ? to : start + edgesPerThread;
            
            threads[i] = new Thread(() -> {
                // Perform PageRank computation on the edges in the range [start, end)
                for (int j = start; j < end; j++) {
                    int src = source[j];
                    int dst = destination[j];
                    relax.relax(src, dst);
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
