package uk.ac.qub.csc3021.graph;

import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
    private BlockingQueue<int[]> buffer;
    private Relax relax;

    public Consumer(BlockingQueue<int[]> buffer, Relax relax) {
        this.buffer = buffer;
        this.relax = relax;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Get edges from the shared buffer
                int[] edge = buffer.take();

                // Check for a special signal indicating the end of data
                if (edge[0] == -1 && edge[1] == -1) {
                    break;
                }

                // Process the edge using relax.relax(src, dst)
                int src = edge[0];
                int dst = edge[1];
                relax.relax(src, dst);

                // Optionally, you can print or perform other actions after processing an edge
                System.out.println("Consumer processed edge: " + src + " -> " + dst);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}