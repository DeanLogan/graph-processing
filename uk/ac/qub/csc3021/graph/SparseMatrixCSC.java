package uk.ac.qub.csc3021.graph;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

// This class represents the adjacency matrix of a graph as a sparse matrix
// in compressed sparse columns format (CSC). The incoming edges for each
// vertex are listed.
public class SparseMatrixCSC extends SparseMatrix {
    // DONE: variable declarations
    int[] index;
    int[] sources;
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph

    public SparseMatrixCSC(String file) {
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
            System.err.println("Exception: " + e);
            return;
        }
    }

    int getNext(BufferedReader rd) throws Exception {
        String line = rd.readLine();
        if (line == null)
            throw new Exception("premature end of file");
        return Integer.parseInt(line);
    }

    void readFile(BufferedReader rd) throws Exception {
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

    // Return number of vertices in the graph
    public int getNumVertices() {
        return num_vertices;
    }

    // Return number of edges in the graph
    public int getNumEdges() {
        return num_edges;
    }

    // Auxiliary function for PageRank calculation
    public void calculateOutDegree(int outdeg[]) {
        // DONE:
        //    Calculate the out-degree for every vertex, i.e., the
        //    number of edges where a vertex appears as a source vertex.
        
        // assumes outdeg[] is already initialised to 0
        for (int i = 0; i < num_edges; ++i) {
            outdeg[sources[i]]++;
        }
    }

    // Apply relax once to every edge in the graph
    public void edgemap(Relax relax) {
        // DONE:
        //    Iterate over all edges in the sparse matrix and calculate
        //    the contribution to the new PageRank value of a destination
        //    vertex made by the corresponding source vertex
        int count = 0;
        for(int i=0; i<num_vertices; i++){
            for(int j=0; j<(index[i+1]-index[i]); j++){
                count++;
                //System.out.println(sources[j+index[i]]+" "+i);
                relax.relax(sources[j+index[i]], i);
            }
        }
        System.out.println(count);
    }

    public void ranged_edgemap(Relax relax, int from, int to) {
        // Only implement for parallel/concurrent processing
        // if you find it useful. Not relevant for the first assignment.
        // DONE:
        // Iterate over partition indicated by from...to and calculate
        // the contribution to the new PageRank value of a destination
        // vertex made by the corresponding source vertex
        for (int i = from; i < to; i++) {
            for(int j=index[i]; j<(index[i+1]); j++){
                relax.relax(sources[j], i);
            }
        }
    }
}
