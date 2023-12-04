package uk.ac.qub.csc3021.graph;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SparseMatrixPipelined extends SparseMatrixCSC {
    public SparseMatrixPipelined(String file) {
        super(file);
    }

    @Override
    public void edgemap(Relax relax) {
        int BUFFER_SIZE = Integer.MAX_VALUE;
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(BUFFER_SIZE);

        // Producer thread
        Thread producer = new Thread(() -> {
            for(int i=0; i<num_vertices; i++){
                for(int j=0; j<(index[i+1]-index[i]); j++){
                    try {
                        queue.put(sources[j+index[i]]);
                        queue.put(i);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });

        // Consumer threads
        Thread[] consumers = new Thread[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new Thread(() -> {
                while (true) {
                    try {
                        int src = queue.take();
                        int dst = queue.take();
                        relax.relax(src, dst);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // Start threads
        producer.start();
        for (Thread consumer : consumers) {
            consumer.start();
        }

        // Wait for threads to finish
        try {
            producer.join();
            for (Thread consumer : consumers) {
                consumer.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}