package uk.ac.qub.csc3021.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Producer implements Runnable {
    private final String filePath;
    private final BlockingQueue<int[]> buffer;
    private final int blockSize;

    public Producer(String filePath, BlockingQueue<int[]> buffer, int blockSize) {
        this.filePath = filePath;
        this.buffer = buffer;
        this.blockSize = blockSize;
    }

    @Override
    public void run() {
        try (BufferedReader rd = new BufferedReader(new FileReader(filePath))) {
            readFile(rd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFile(BufferedReader rd) throws Exception {
        // Skip the header
        rd.readLine();

        while (true) {
            int[] block = readBlock(rd);
            if (block == null || block.length == 0) {
                // End of file or no more edges
                break;
            }

            buffer.put(block); // Add the block to the shared buffer
        }

        // Add a signal to indicate the end of data
        buffer.put(new int[]{-1, -1});
    }

    private int[] readBlock(BufferedReader rd) throws Exception {
        int[] block = new int[blockSize * 2]; // Each edge has a source and destination
        int edgeCount = 0;
    
        for (int i = 0; i < blockSize; ++i) {
            String line = rd.readLine();
    
            if (line == null) {
                // End of file
                break;
            }
    
            String[] elm = line.split(" ");
            
            if (elm.length < 1) {
                // Skip lines without any elements
                continue;
            }
    
            int src = Integer.parseInt(elm[0]);
            block[edgeCount++] = src;
    
            for (int j = 1; j < elm.length; ++j) {
                if (edgeCount < block.length) {
                    int dst = Integer.parseInt(elm[j]);
                    block[edgeCount++] = dst;
                } else {
                    // The block array is full, break the loop
                    break;
                }
            }
        }
    
        if (edgeCount == 0) {
            return null; // No more edges
        }
    
        // Trim the array to the actual number of edges
        return java.util.Arrays.copyOf(block, edgeCount);
    }
}
