package uk.ac.qub.csc3021.graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
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

    int getNext(BufferedReader rd) throws Exception {
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        return Integer.parseInt(line);
    }

    void readFile(BufferedReader rd, Relax relax) throws Exception {
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        if (!line.equalsIgnoreCase("CSC") && !line.equalsIgnoreCase("CSC-CSR"))
            throw new Exception("file format error -- header");

        num_vertices = getNext(rd);
        num_edges = getNext(rd);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads); // 10 is just an example, adjust as needed
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < num_vertices; ++i) {
            line = rd.readLine();
            if (line == null)
                throw new Exception("premature end of file");
            String[] elm = line.split(" ");
            assert Integer.parseInt(elm[0]) == i : "Error in CSC file";

            final int index = i;
            Callable<Void> task = () -> {
                for (int j = 1; j < elm.length; ++j) {
                    relax.relax(Integer.parseInt(elm[j]), index);
                }
                return null;
            };

            futures.add(executor.submit(task));
        }

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
    }

    private static int getFileSize(RandomAccessFile rd) throws IOException {
        return (int) rd.length();
    }

    void readLinesInRange(int start, long end, Relax relax, int threadId) {
        try {
            RandomAccessFile rd = new RandomAccessFile(file, "r");
            rd.seek(start - 1);
            if(rd.readByte() != '\n'){
                rd.readLine();
            }
            long position = start;
            String line = rd.readLine();
            System.out.println("thread: "+threadId+" is starting while loop");
            while (position < end && line != null) {
                String[] elm = line.split(" ");
                for (int j = 1; j < elm.length; ++j) {
                    relax.relax(Integer.parseInt(elm[j]), Integer.parseInt(elm[0]));
                }
                position = rd.getFilePointer();
                line = rd.readLine();
            }
            System.out.println("thread: "+threadId+" is ended while loop");
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public void edgemap1(Relax relax){
        // try{
        //     RandomAccessFile rd = new RandomAccessFile(file, "r");
        //     int fileSize = getFileSize(rd);
        //     int chunkSize = fileSize / numThreads;
        //     rd.readLine();
        //     rd.readLine();
        //     rd.readLine();
        //     int position = (int) rd.getFilePointer();
        //     int start = 1 * chunkSize;
        //     readLinesInRange(rd, position,   (0 == numThreads - 1) ? fileSize : (0 + 1) * chunkSize, relax);
        //     System.out.println("file");
        //     readLinesInRange(rd, start,   (1 == numThreads - 1) ? fileSize : (1 + 1) * chunkSize, relax);
        // }
        // catch(Exception e){
        //     e.printStackTrace();
        // }

        try {
            RandomAccessFile rd = new RandomAccessFile(file, "r");
            
            int fileSize = getFileSize(rd);
            int chunkSize = fileSize / numThreads;

            Thread[] threads = new Thread[numThreads];
            rd.readLine();
            rd.readLine();
            rd.readLine();
            int position = (int) rd.getFilePointer();
            rd.close();
            
            Thread thread = new Thread(() -> readLinesInRange(position, (0 == numThreads - 1) ? fileSize : (0 + 1) * chunkSize, relax,0));
            thread.start();
            threads[0] = thread;

            for (int i = 1; i < numThreads; i++) {
                int start = i * chunkSize + 1;
                long end = (i == numThreads - 1) ? fileSize : (i + 1) * chunkSize;
                thread = new Thread(() -> readLinesInRange(start, end, relax,(int)Thread.currentThread().getId()));
                thread.start();
                threads[i] = thread;
            }
            // Wait for all threads to finish
            for (Thread threadL : threads) {
                try {
                    threadL.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void edgemap(Relax relax) {
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader rd = new BufferedReader(is);
            readFile(rd, relax);
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