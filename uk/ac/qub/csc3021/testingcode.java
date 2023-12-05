package uk.ac.qub.csc3021.graph;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import uk.ac.qub.csc3021.graph.Edge;


class LBQueue<E> {
    LinkedBlockingQueue<E> queue;

    LBQueue( int size ) {
		queue = new LinkedBlockingQueue<E>( size );
    }
    LBQueue() {
		queue = new LinkedBlockingQueue<E>(); // unbounded capacity
    }

    public void put( E i ) {
		boolean done = false;
		while( !done ) {
			try { queue.put( i ); done = true; }
			catch( InterruptedException e) { }
		}
    }

    public E get() {
		E item = null;
		while( item == null ) {
			try { item = queue.take(); }
			catch( InterruptedException e) { }
		}
		return item;
    }
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

public class SparseMatrixPipelined extends SparseMatrix {
    // ... (rest of your existing code)

    // Additional variables for threading
    private final int numThreads;  // Number of consumer threads
    private final LBQueue<Edge[]> edgeQueue;
    private Edge[] edgesList;
    int[] index;
    int[] sources;
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph

    public SparseMatrixPipelined(String file, int numThreads) {
        this.numThreads = numThreads;
        this.edgeQueue = new LBQueue<Edge[]>();
        this.edgesList = new Edge[1]; 

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
            System.err.println(Exception.class + ": " + e);
            return;
        }
    }

    void readFile(BufferedReader rd) throws Exception {
        System.out.println("CORRECT ONE");
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        if (!line.equalsIgnoreCase("CSC") && !line.equalsIgnoreCase("CSC-CSR"))
            throw new Exception("file format error -- header");

        num_vertices = getNext(rd);
        num_edges = getNext(rd);

        // DONE: allocate data structures
        index = new int[num_vertices+1];
        sources = new int[num_edges];

        for (int i = 0; i < num_vertices; ++i) {
            line = rd.readLine();
            if (line == null)
                throw new Exception("premature end of file");
            String elm[] = line.split(" ");
            assert Integer.parseInt(elm[0]) == i : "Error in CSC file";
            for (int j = 1; j < elm.length; ++j) {
                int src = Integer.parseInt(elm[j]);
                // DONE:
                // Record an edge from source src to destination i
                sources[index[i] + j - 1] = src; // Store the source in sources
            }
            index[i + 1] = index[i] + elm.length - 1; // Set the next index
        }
    }

    int getNext(BufferedReader rd) throws Exception {
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        return Integer.parseInt(line);
    }

    // Function to produce edges and add to the queue
    private void produceEdges(Edge[][] blocks) throws Exception {
        System.out.println(edgeQueue.isEmpty());
        // Read edges from the file and add to the queue
        for(Edge[] block : blocks ) {
            edgeQueue.put(block);
        }
        System.out.println(edgeQueue.isEmpty());
        // for(int i=0; i<edgesList.length; i++){
        //     System.out.println(i);
        // }
    }

    // Function to consume edges from the queue and process them
    private void consumeEdges(Relax relax) throws Exception {
        // Process edges from the queue
        while (true) {
            // System.out.println("Consumer thread " + Thread.currentThread().getId() + " is running, num is "+edgeQueue.getNum());
            Edge[] edge = edgeQueue.get(); // Blocking call until an edge is available
            for(int i=0; i<edge.length; i++){
                relax.relax(edge[i].getSource(), edge[i].getDestination());
            }
            if(edgeQueue.isEmpty()){
                break; // Break if queue is empty (all edges have been processed
            } 
        }
    }

    private Edge[] createEdgeArray(int length){
        Edge[] edgeArray = new Edge[length];
        int edgeIndex = 0;
        for(int i=0; i<num_vertices; i++){
            for(int j=0; j<(index[i+1]-index[i]); j++){
                edgeArray[edgeIndex] = new Edge(sources[j+index[i]], i);
                edgeIndex++;
            }
        }
        return edgeArray;
    }

    private static Edge[][] createBlocks(Edge[] originalArray, int blockLength) {
        int numSubArrays = (originalArray.length + blockLength - 1) / blockLength;
        Edge[][] blocks = new Edge[numSubArrays][];
        // Populate subarrays
        for (int i = 0; i < numSubArrays; i++) {
            // Calculate the start and end indices for each subarray
            int start = i * blockLength;
            int end = Math.min((i + 1) * blockLength, originalArray.length);
            // Create a subarray
            blocks[i] = new Edge[end - start];
            // Copy elements from the original array to the subarray
            System.arraycopy(originalArray, start, blocks[i], 0, end - start);
        }

        return blocks;
    }

    public void edgemap(Relax relax) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        Edge[] edgeArray = createEdgeArray(getNumEdges());
        
        Edge[][] blocks = createBlocks(edgeArray, 10000000);
        // Start producer thread
        executor.execute(() -> {
            try {
                produceEdges(blocks); // Function to read edges and add to the queue
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Start consumer threads
        // for (int i = 0; i < numThreads; i++) {
        //     executor.execute(() -> {
        //         try {
        //             consumeEdges(relax); // Function to process edges from the queue
        //         } catch (Exception e) {
        //             e.printStackTrace();
        //         }
        //     });
        // }

        // Shutdown the executor after all threads are done
        executor.shutdown();

        try {
            // Wait for all threads to finish
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculateOutDegree'");
    }

    @Override
    public void ranged_edgemap(Relax relax, int from, int to) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ranged_edgemap'");
    }
}
