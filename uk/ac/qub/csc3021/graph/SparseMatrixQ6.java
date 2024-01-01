package uk.ac.qub.csc3021.graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;


public class SparseMatrixQ6 extends SparseMatrix {
    int[] index;
    int[] sources;
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph
    int numThreads;

    private String file;

    public SparseMatrixQ6(String file, int numThreads) {
        this.file = file;
        num_vertices = readLineTwo(file);
        this.numThreads = numThreads;
    }

    public static int readLineTwo(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip the first line
            reader.readLine();

            // Read and return the second line
            return Integer.parseInt(reader.readLine());
        }
        catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    void readRangeBuffered(int start, long end, Relax relax, int threadId) {
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader rd = new BufferedReader(is);
            for(int i = 0; i < start; i++){
                rd.readLine();
            }

            for(int i = start-3; i < end; i++){
                try{
                    String[] elm = rd.readLine().split(" ");
                    for (int j = 1; j < elm.length; ++j) {
                        relax.relax(Integer.parseInt(elm[j]), i);
                    }
                }
                catch(Exception e){
                    break;
                }
            }
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    int getPageLength(){
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader rd = new BufferedReader(is);
            int length = 0;
            while(rd.readLine() != null){
                length++;
            }
            rd.close();
            return length;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void edgemap(Relax relax) {
        int totalLines = getPageLength();
        int linesPerThread = (totalLines + numThreads - 1) / numThreads; // Round up
    
        List<Thread> threads = new ArrayList<>();
    
        for (int i = 0; i < numThreads; i++) {
            final int startLine = i * linesPerThread + 3; // Start from line 3
            final int endLine = Math.min((i + 1) * linesPerThread + 2, totalLines); // Adjust end line accordingly
    
            Thread thread = new Thread(() -> {
                readRangeBuffered(startLine, endLine, relax, numThreads);
            });
    
            thread.start();
            threads.add(thread);
        }
    
        // Wait for all threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
}
