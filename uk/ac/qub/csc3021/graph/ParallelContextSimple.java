package uk.ac.qub.csc3021.graph;


public class ParallelContextSimple extends ParallelContext {

    private class ThreadSimple extends Thread {
        private final int threadId;
        private final SparseMatrix matrix;
        private final Relax relax;

        public ThreadSimple(int threadId, SparseMatrix matrix, Relax relax) {
            this.threadId = threadId;
            this.matrix = matrix;
            this.relax = relax;
        }

        public void run() {
            int numberOfVertices = matrix.getNumVertices();
            int numberOfThreads = getNumThreads();

            // Calculate the range of vertices for this thread
            int verticesPerThread = numberOfVertices / numberOfThreads;
            int startVertex = threadId * verticesPerThread;
            int endVertex = (threadId == numberOfThreads - 1) ? numberOfVertices : (threadId + 1) * verticesPerThread - 1;

            // Perform edgemap for the specified range of vertices
            matrix.ranged_edgemap(relax, startVertex, endVertex);
        }
    };

    public ParallelContextSimple(int num_threads_) {
        super(num_threads_);
    }

    public void terminate() {
        // Perform any termination actions if needed
    }

    // The edgemap method for Q3 should create threads, which each process
    // one graph partition, then wait for them to complete.
    public void edgemap(SparseMatrix matrix, Relax relax) {
        int numberOfThreads = getNumThreads();
        ThreadSimple[] threads = new ThreadSimple[numberOfThreads];

        // Create and start threads
        for (int i = 0; i < numberOfThreads; i++) {
            ThreadSimple thread = new ThreadSimple(i, matrix, relax);
            threads[i] = thread;
        }

        for (ThreadSimple thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        try {
            for (ThreadSimple thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
