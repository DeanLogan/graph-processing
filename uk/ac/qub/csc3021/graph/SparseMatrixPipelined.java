package uk.ac.qub.csc3021.graph;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.io.IOException;


public class SparseMatrixPipelined extends SparseMatrix {
    int[] index;
    int[] sources;
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph
    private volatile boolean continueRunning = true;

    private String file;
    private LinkedBlockingQueue<int[][]> queue;

    public SparseMatrixPipelined(String file) {
        this.file = file;
        this.queue = new LinkedBlockingQueue<int[][]>();
        num_vertices = readLineTwo(file);
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

    int getNext(BufferedReader rd) throws Exception {
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        return Integer.parseInt(line);
    }

    void readFile(BufferedReader rd, int blockSize) throws Exception {
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

        int[][] block = new int[blockSize][2];
        int blockCounter = 0;

        for (int i = 0; i < num_vertices; ++i) {
            line = rd.readLine();
            if (line == null)
                throw new Exception("premature end of file");
            String[] elm = line.split(" ");
            assert Integer.parseInt(elm[0]) == i : "Error in CSC file";

            for (int j = 1; j < elm.length; ++j) {
                int src = Integer.parseInt(elm[j]);

                // add edge to block
                block[blockCounter][0] = src;
                block[blockCounter][1] = i;
                if(blockCounter == blockSize-1) {
                    queue.put(block);
                    block = new int[blockSize][2]; // create a new block only when the current one is full
                    blockCounter = 0;
                } else {
                    blockCounter++;
                }
            }
        }

        // Add the last block if it's not empty
        if(blockCounter > 0) {
            queue.put(Arrays.copyOf(block, blockCounter)); // copy only the filled part of the block
        }

        while(!queue.isEmpty()){}
        continueRunning = false;
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

    public void startConsumer(Relax relax) {
        Thread consumerThread = new Thread(() -> {
            int[][] block;
            while(continueRunning) {
                try {
                    if(queue.isEmpty() == true) {
                        continue;
                    }
                    block = queue.take();
                    for(int i = 0; i < block.length; i++) {
                        if(block[i][0] != block[i][1]) {
                            relax.relax(block[i][0], block[i][1]);
                        }                    
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        consumerThread.start();
    }

    public void consumer(Relax relax){
        int[][] block;
        while(queue.isEmpty() == false) {
            try {
                block = queue.take();
                for(int i = 0; i < block.length; i++) {
                    if(block[i][0] == block[i][1]) {
                        //System.out.println("lol");
                    } else{
                        // System.out.println(block[i][0] + " "+ block[i][1]);
                        relax.relax(block[i][0], block[i][1]);
                    }
                    
                }
                // break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void edgemap(Relax relax) {
        try {
            startConsumer(relax);
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader rd = new BufferedReader(is);
            readFile(rd, 128);
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
    }

    // @Override
    // public void edgemap(Relax relax) {
    //     try {
    //         InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
    //         BufferedReader rd = new BufferedReader(is);
    //         readFile(rd, 128);
    //     } catch (FileNotFoundException e) {
    //         System.err.println("File not found: " + e);
    //         return;
    //     } catch (UnsupportedEncodingException e) {
    //         System.err.println("Unsupported encoding exception: " + e);
    //         return;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return;
    //     }
    //     consumer(relax);
    // }
}