package uk.ac.qub.csc3021.graph;

import java.util.concurrent.atomic.AtomicIntegerArray;

// Calculate the connected components using the disjoint set data structure
// This algorithm only works correctly for undirected graphs
public class DisjointSetCC {
    private static class DSCCRelax implements Relax {
        DSCCRelax(AtomicIntegerArray parent_) {
            parents = new AtomicIntegerArray(parent_.length());
    
            for (int i = 0; i < parent_.length(); i++) {
                parents.set(i, i); // Initialize each element with its index as the parent
            }
        }
    
        public void relax(int src, int dst) {
            union(src, dst);
        }

        public int find(int x)  { // halving
            while (x != parents.get(x)) {
                int next = parents.get(x);
                parents.compareAndSet(x, x, next);
                x = next;
            }
            return x;
        }

        private boolean union(int x, int y) {        
            while (true) {
                int u = find(x);
                int v = find(y);
                if (u < v) {
                    if (parents.compareAndSet(v, v, u)) {
                        return false;
                    }
                } else if (u == v) {
                    return true;
                } else if (parents.compareAndSet(u, u, v)) {
                    return false;
                }
            }
        }


        // Attempt 5
        public int findFull(int x) { // full path compression
            int root = x;
            while (root != parents.get(root)) {
                root = parents.get(root);
            }
            
            while (x != root) {
                int next = parents.get(x);
                parents.set(x, root);
                x = next;
            }
        
            return root;
        }

        public int findNoCompression(int x) { // No compression
            while (x != parents.get(x)) {
                x = parents.get(x);
            }
            return x;
        }

        public int findSplitting(int x)  { // Splitting
            while (x != parents.get(x)) {
                int next = parents.get(x);
                parents.compareAndSet(x, x, parents.get(next));
                x = parents.get(x); 
            }
            return x;
        }

        // Variable declarations
        private AtomicIntegerArray parents;
    }

    public static int[] compute(SparseMatrix matrix) {
        long tm_start = System.nanoTime();

        final int n = matrix.getNumVertices();
        System.out.println("Number of vertices: " + n);
        final AtomicIntegerArray parent = new AtomicIntegerArray(n);
        final boolean verbose = true;

        for (int i = 0; i < n; ++i) {
            // Each vertex is a set on their own
            // ...
        }

        DSCCRelax DSCCrelax = new DSCCRelax(parent);

        double tm_init = (double) (System.nanoTime() - tm_start) * 1e-9;
        System.err.println("Initialisation: " + tm_init + " seconds");
        tm_start = System.nanoTime();

        ParallelContext context = ParallelContextHolder.get();

        // 1. Make pass over graph
        context.edgemap(matrix, DSCCrelax);

        double tm_step = (double) (System.nanoTime() - tm_start) * 1e-9;
        if (verbose)
            System.err.println("processing time=" + tm_step + " seconds");
        tm_start = System.nanoTime();

        // Post-process the labels

        // 1. Count the number of components
        //    and map component IDs to a narrow domain
        int ncc = 0;
        int remap[] = new int[n];
        for (int i = 0; i < n; ++i)
            if (DSCCrelax.find(i) == i)
                remap[i] = ncc++;

        if (verbose)
            System.err.println("Number of components: " + ncc);

        // 2. Calculate the size of each component
        int sizes[] = new int[ncc];
        for (int i = 0; i < n; ++i)
            ++sizes[remap[DSCCrelax.find(i)]];

        if (verbose)
            System.err.println("DisjointSetCC: " + ncc + " components");

        return sizes;
    }
}