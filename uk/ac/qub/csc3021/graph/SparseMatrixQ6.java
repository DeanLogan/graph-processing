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

    // attempt 1

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

    public void edgemap1(Relax relax) {
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

    // attempt 2

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

    public void edgemap2(Relax relax){
        try {
            RandomAccessFile rd = new RandomAccessFile(file, "r");
        
            int fileSize = getFileSize(rd);
            int chunkSize = fileSize / numThreads;
        
            rd.readLine();
            rd.readLine();
            rd.readLine();
            int position = (int) rd.getFilePointer();
            rd.close();
        
            List<Thread> threads = new ArrayList<>();
        
            Thread firstThread = new Thread(() -> readLinesInRange(position, (0 == numThreads - 1) ? fileSize : (0 + 1) * chunkSize, relax, 0));
            firstThread.start();
            threads.add(firstThread);
        
            for (int i = 1; i < numThreads; i++) {
                int start = i * chunkSize + 1;
                long end = (i == numThreads - 1) ? fileSize : (i + 1) * chunkSize;
                final int threadId = i;
                Thread thread = new Thread(() -> readLinesInRange(start, end, relax, threadId));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // attempt 3
    
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

        void readFileLines(BufferedReader rd, Relax relax) {
        try{
            String line = rd.readLine();
            while (line != null) {
                String[] elm = line.split(" ");
                for (int j = 1; j < elm.length; ++j) {
                    relax.relax(Integer.parseInt(elm[j]), Integer.parseInt(elm[0]));
                }
                line = rd.readLine();
            } 
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void edgemap3(Relax relax) {
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

    public void edgemap(Relax relax) {
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
            BufferedReader rd = new BufferedReader(is);
            rd.readLine();
            rd.readLine();
            rd.readLine();
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                Thread thread = new Thread(() -> {
                    readFileLines(rd, relax);
                });
        
                thread.start();
                threads[i] = thread;
            }
            
            // Wait for all threads to finish
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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

    // attempt 4


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
