package uk.ac.qub.csc3021.graph;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import uk.ac.qub.csc3021.graph.Edge;


public class SparseMatrixPipelined extends SparseMatrix {
    int[] index;
    int[] sources;
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph

    private String file;
    private LinkedBlockingQueue<int[][]> queue;

    public SparseMatrixPipelined(String file) {
        this.file = file;
        this.queue = new LinkedBlockingQueue<int[][]>();
    }

    int getNext(BufferedReader rd) throws Exception {
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        return Integer.parseInt(line);
    }

    void readFile(BufferedReader rd) throws Exception {
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        if (!line.equalsIgnoreCase("CSC") && !line.equalsIgnoreCase("CSC-CSR"))
            throw new Exception("file format error -- header");

        num_vertices = getNext(rd);
        num_edges = getNext(rd);

        // Allocate data structures
        index = new int[num_vertices + 1];
        sources = new int[num_edges];

        int[] edge = new int[2];
        int[][] block = new int[128][2];
        int blockCounter = 0;

        for (int i = 0; i < num_vertices; ++i) {
            line = rd.readLine();
            if (line == null)
                throw new Exception("premature end of file");
            String[] elm = line.split(" ");
            assert Integer.parseInt(elm[0]) == i : "Error in CSC file";

            for (int j = 1; j < elm.length; ++j) {
                int src = Integer.parseInt(elm[j]);

                // create edges
                edge[0] = src; 
                edge[1] = i; 
                block[blockCounter] = edge;
                if(blockCounter == 127) {
                    queue.put(block);
                    blockCounter = 0;
                } else {
                    blockCounter++;
                }
            }
        }
    }

    // Return number of vertices in the graph
    @Override
    public int getNumVertices() {
        return num_vertices;
    }

    // Return number of edges in the graph
    @Override
    public int getNumEdges() {
        return num_edges;
    }

    @Override
    public void calculateOutDegree(int[] outdeg) {
        throw new UnsupportedOperationException("Unimplemented method 'calculateOutDegree'");
    }

    @Override
    public void ranged_edgemap(Relax relax, int from, int to) {
        throw new UnsupportedOperationException("Unimplemented method 'ranged_edgemap'");
    }

    @Override
    public void edgemap(Relax relax) {
        System.out.println("WE RIDE AT DAWN BITCHES - changed 2");
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
            e.printStackTrace();
            return;
        }
        System.out.println("holy shit balls it worked");
    }
}
