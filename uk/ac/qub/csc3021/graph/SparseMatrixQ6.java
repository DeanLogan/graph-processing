package uk.ac.qub.csc3021.graph;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SparseMatrixQ6 extends SparseMatrix {
    int[] index;
    int[] sources;
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph
    int numThreads;
    int fileSize;
    int chunkSize;
    int position;
    FileChannel fc;

    public SparseMatrixQ6(String file, int numThreads) {
        this.numThreads = numThreads;
        try{
            fc = FileChannel.open(Paths.get(file), StandardOpenOption.READ);
            fileSize = (int) fc.size();
            chunkSize = fileSize / numThreads;
            FileChannel fc = FileChannel.open(Paths.get(file), StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(100);
            fc.read(buffer);
            String fileContent = new String(buffer.array(), StandardCharsets.UTF_8);
            String[] lines = fileContent.split("\n");
            num_vertices = Integer.parseInt(lines[1]);
            num_edges = Integer.parseInt(lines[2]);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    void readLinesInRangePipelined(int threadNum, Relax relax, int threadPool) {
        ExecutorService executor = Executors.newFixedThreadPool(threadPool);
        int start = threadNum * chunkSize + 1;
        int end = (threadNum == numThreads - 1) ? fileSize : (threadNum + 1) * chunkSize;
        MappedByteBuffer buffer;
        try{
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, start, end - start);
        } catch(Exception e){ return; }
        
        int lineStart = 0;
        for (int i = 0; i < buffer.limit(); i++) {
            if (buffer.get(i) == '\n') {
                final int lineEnd = i;
                final int lineStartFinal = lineStart;
                executor.submit(() -> processLine(buffer, lineStartFinal, lineEnd, relax));
                lineStart = i + 1;
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void processLine(ByteBuffer buffer, int start, int end, Relax relax) {
        int spaceIndex = -1;
        for (int i = start; i < end; i++) {
            if (buffer.get(i) == ' ') {
                spaceIndex = i;
                break;
            }
        }
    
        if (spaceIndex != -1) {
            int firstNumber;
            try {
                firstNumber = parseIntegerFromBuffer(buffer, start, spaceIndex);
            } catch (Exception e) {
                return;
            }
    
            int numberStart = spaceIndex + 1;
            for (int i = spaceIndex + 1; i < end; i++) {
                if (buffer.get(i) == ' ') {
                    relax.relax(firstNumber, parseIntegerFromBuffer(buffer, numberStart, i));
                    numberStart = i + 1;
                }
            }
            relax.relax(firstNumber, parseIntegerFromBuffer(buffer, numberStart, end)); // Call relax for the last number in the line
        }
    }
    
    int parseIntegerFromBuffer(ByteBuffer buffer, int start, int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            result = result * 10 + (buffer.get(i) - '0');
        }
        return result;
    }
    
    public void edgemap(Relax relax){
        Thread[] threads;
        if(numThreads > 3 && num_edges > 1000000){
            int pairsOfThree = (int) Math.floor(numThreads / 3);
            int remainder = numThreads % 3;
            threads = new Thread[pairsOfThree + remainder];
            if(remainder != 0){
                Thread thread = new Thread(() -> readLinesInRange(0, relax));
                thread.start();
                threads[0] = thread;
            }
            for (int i = remainder; i < pairsOfThree+remainder; i++) {
                final int threadNum = i;
                Thread thread = new Thread(() -> readLinesInRangePipelined(threadNum, relax, 2));
                thread.start();
                threads[i] = thread;
            } 
        }
        else{
            threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                final int threadNum = i;
                Thread thread = new Thread(() -> readLinesInRange(threadNum, relax));
                thread.start();
                threads[i] = thread;
            } 
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
    
    void readLinesInRange(int threadNum, Relax relax) {
        int start = threadNum * chunkSize + 1;
        int end = (threadNum == numThreads - 1) ? fileSize : (threadNum + 1) * chunkSize;
        MappedByteBuffer buffer;
        try{
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, start, end - start);
        } catch(Exception e){ return; }
        
        int lineStart = 0;
        for (int i = 0; i < buffer.limit(); i++) {
            if (buffer.get(i) == '\n') {
                processLine(buffer, lineStart, i, relax);
                lineStart = i + 1;
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
