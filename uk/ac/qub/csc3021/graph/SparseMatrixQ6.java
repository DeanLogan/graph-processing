package uk.ac.qub.csc3021.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;



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

    private static int getFileSize(RandomAccessFile rd) throws IOException {
        return (int) rd.length();
    }

    void readLinesInRange(int start, int end, Relax relax, int threadId) {
        try (FileChannel fc = FileChannel.open(Paths.get(file), StandardOpenOption.READ)) {
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, start, end - start);
            //System.out.println("thread: "+threadId+" is starting");
            int lineStart = 0;
            for (int i = 0; i < buffer.limit(); i++) {
                if (buffer.get(i) == '\n') {
                    processLine(buffer, lineStart, i, relax, threadId);
                    lineStart = i + 1;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void processLine(MappedByteBuffer buffer, int start, int end, Relax relax, int threadId) {
        int spaceIndex = -1;
        for (int i = start; i < end; i++) {
            if (buffer.get(i) == ' ') {
                spaceIndex = i;
                break;
            }
        }
    
        if (spaceIndex != -1) {
            byte[] firstNumberBytes = new byte[spaceIndex - start];
            buffer.position(start);
            buffer.get(firstNumberBytes, 0, spaceIndex - start);
            int firstNumber;
            try{
                firstNumber = Integer.parseInt(new String(firstNumberBytes));
            } catch(Exception e){return;}
            //System.out.println("thread id "+threadId+"["+new String(firstNumberBytes)+"]");
            buffer.position(spaceIndex);
            byte[] remainingBytes = new byte[end - spaceIndex ];
            buffer.get(remainingBytes, 0, end - spaceIndex );
            String remaining = new String(remainingBytes);
            //System.out.println("remaining: ="+remaining+"]");
            String[] remainingNumbers = remaining.split(" ");
            for (int i = 1; i < remainingNumbers.length; i++) {
                int num = Integer.parseInt(remainingNumbers[i]);
                relax.relax(firstNumber, num);
            }
        }
    }

    public void edgemap(Relax relax){
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
                int end = (i == numThreads - 1) ? fileSize : (i + 1) * chunkSize;
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
