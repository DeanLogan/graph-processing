# Assignment 3

This repo contains the code for my final assignment for my university module CSC3021 Concurrent Programming. The written part of the assignment (including the questions) are within the "report" folder. 

This assignment focuses on implementing concurrent solutions for the connected components problem on undirected graphs, with an emphasis on leveraging multiple processor cores for accelerated computation. The tasks include analyzing the sequential implementation, parallelizing the edgemap method, implementing Jayanti and Tarjan's disjoint set algorithm, creating a pipelined operation for disjoint sets, and optimizing performance in a competition setting.

Q6 also has a competition element to it, to measure the execution time of the solution created for Q6 a DOMjudge server was created, allowing code to be submitted and a score given based on the performance of the solution to Q6. 

There are several branches within this repo, one for each of the attempts for Q6 and another for the code for Q2, Q3 & Q4 (uneffected by any changes made within Q6). 

# Code Description

The code models graph analytics applications as a repeated graph traversal. Each traversal operates like a higher-order map method: all edges of the graph are visited and a user-supplied method is called for each edge. This method performs a function specific to the graph analytics. For instance, in PageRank it consists of a floating-point addition, while the label propagation algorithm performs a ‘minimum’ operation on integers.

The code may operate using one of three graph data structures (CSR, CSC or COO).

Graph data sets are available from [http://www.eeecs.qub.ac.uk/~H.Vandierendonck/CSC3021/graphs/](http://www.eeecs.qub.ac.uk/~H.Vandierendonck/CSC3021/graphs/). The file format of these, after decompression, is:

CSR and CSC:
* one line containing the number of vertices and number of edges, separated by a space.
* for each vertex, a line containing: the vertex ID of the corresponding vertex followed by the IDs of all its neighbours. IDs are separated by a space.
In the CSR format, neighbours correspond to out-edges, whereas in the CSC format these correspond to in-edges.

COO:
* one line containing the number of vertices and number of edges, separated by a space.
* for each edge, one line containing the IDs of source and destination vertex separated by a space.

## PageRank

PageRank is Google’s algorithm to rank the search results that match the queried keywords. The algorithm models the internet as a directed graph where web pages are represented as vertices and links between pages are edges. The PageRank algorithm calculates the likelihood, or rank, that a page will be visited by people surfing the web. 
The rank of a page depends on the rank of the pages that link to it, where pages that are frequently pointed to tend to gain a higher rank. Also, pages pointed to by highly ranked pages tend to receive a higher rank. The algorithm is itself quite simple but in practice it captures well the appreciation of the importance of pages by humans.

## DisjointSet
Jayanti and Tarjan's randomized concurrent disjoint-set algorithm is a randomized parallel algorithm designed for disjoint-set data structures, which maintain a partition of a set into disjoint subsets. The algorithm is based on the classical Union-Find data structure, which supports two main operations: Union, which merges two disjoint sets, and Find, which determines the set to which a given element belongs.

# Command Line Arguments

The programs can be called using a driver program called Driver.java. This driver requires command-line arguments to specify what to do (the problem: PageRank or Connected Components; the graph data structure; number of threads to use).

## Explaination of Arguments

- pr = Running the edgemap method on the PageRank Algorithm.
- ds = Running the edgemap method on the DisjointSet Algorithm.
- CSR = Running the choosen algorithm using a Compressed Sparse Row.
- CSC = Running the choosen algorithm using a Compressed Sparse Column.
- COO = Running the choosen algorithm using a Coordinate list.
- ICHOOSE = Used for Q6 to create a new method

## Comands to Use

1. javac Driver.java
2. java -ea Driver (pr|ds) (type in an int for the number of threads that you wish to use) ".\output.txt" (CSR|CSC|COO|ICHOOSE) (replace with path to a local copy of one of the data sets available [here](http://www.eeecs.qub.ac.uk/~H.Vandierendonck/CSC3021/graphs/))



# Q2 - Q4

This contains the answers for q2, q3 and q4 for this assignment.

To run q2 ensure that matrix = new SparseMatrixCSC( inputFileCSC ); is the matrix created within the ICHOOSE if statement in Driver.java on line 76 and that ParallelContextHolder.set( new ParallelContextSimple(num_threads) ); is within the other ICHOOSE if statement in Driver.java on line 101.

To run q3 ensure that matrix = new SparseMatrixCSC( inputFileCSC ); is the matrix created within the ICHOOSE if statement in Driver.java on line 76 and that ParallelContextHolder.set( new ParallelContextSimple(num_threads) ); is within the other ICHOOSE if statement in Driver.java on line 101.

To run q4 ensure that matrix = new SparseMatrixPipelined( inputFile, num_threads ); is the matrix created within the ICHOOSE if statement in Driver.java on line 76 and that ParallelContextHolder.set( new ParallelContextSingleThread() ); is within the other ICHOOSE if statement in Driver.java on line 101.
As per the instructions within the assignment I have added the threading code for the producer and consumer within the edgemap function in SparseMatrixPipelined so to assure that this threading code is used the ParallelContext needs to be set to the single thread one as described within the instructions for question 4.

# Q6

For any of the Q6 attempts ensure that ParallelContextHolder is set to ParallelContextSingleThread() as the thread creation and management is handled within SparseMatrixQ6.

