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
// in compressed sparse rows format (CSR), where a row index corresponds to
// a source vertex and a column index corresponds to a destination
public class SparseMatrixCSR extends SparseMatrix {
    // DONE: variable declarations
    int[] rowPointers;
    int[] columnIndices;
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph

    public SparseMatrixCSR(String file) {
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
        if (!line.equalsIgnoreCase("CSR") && !line.equalsIgnoreCase("CSC-CSR"))
            throw new Exception("file format error -- header");

        num_vertices = getNext(rd);
        num_edges = getNext(rd);

        // DONE: Allocate memory for the CSR representation
        rowPointers = new int[num_vertices+1]; // adds an extra 1 to the array to store the last row pointer
        columnIndices = new int[num_edges];

        for (int i = 0; i < num_vertices; ++i) {
            line = rd.readLine();
            if (line == null)
                throw new Exception("premature end of file");
            String elm[] = line.split(" ");
            assert Integer.parseInt(elm[0]) == i : "Error in CSR file";
            for (int j = 1; j < elm.length; ++j) {
                int dst = Integer.parseInt(elm[j]);
                // DONE:
                //    Record an edge from source i to destination dst
                columnIndices[rowPointers[i] + j - 1] = dst; // Store the destination in columnIndices
            }
            rowPointers[i + 1] = rowPointers[i] + elm.length - 1; // Set the next row pointer
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
        for (int i=0; i < num_vertices; i++) {
            outdeg[i] = rowPointers[i + 1] - rowPointers[i]; // Set the out degree to the difference between the next row pointer and the current row pointer as this difference is the same as the number of edges for the current vertex
        }
    }

    // Apply relax once to every edge in the graph
    public void edgemap(Relax relax) {
        // DONE:
        //    Iterate over all edges in the sparse matrix and calculate
        //    the contribution to the new PageRank value of a destination
        //    vertex made by the corresponding source vertex
        for(int i=0; i<num_vertices; i++){
            for(int j=0; j<(rowPointers[i+1]-rowPointers[i]); j++){
                relax.relax(i, columnIndices[j+rowPointers[i]]);
            }
        }
    }

    public void ranged_edgemap(Relax relax, int from, int to) {
        // Only implement for parallel/concurrent processing
        // if you find it useful. Not relevant for the first assignment.
    }
}

