# Assignment 3

This repo contains several branches, I have went back and added 8 branches, one for each of the attempts in Q6 and another for the code for Q2 - Q4 (uneffected by any changes made within Q6). 

This main branch contains all attempts for Q6 within a single file if that is easier for you to read.

# Q2 - Q4

This contains the answers for q2, q3 and q4 for this assignment.

To run q2 ensure that matrix = new SparseMatrixCSC( inputFileCSC ); is the matrix created within the ICHOOSE if statement in Driver.java on line 76 and that ParallelContextHolder.set( new ParallelContextSimple(num_threads) ); is within the other ICHOOSE if statement in Driver.java on line 101.

To run q3 ensure that matrix = new SparseMatrixCSC( inputFileCSC ); is the matrix created within the ICHOOSE if statement in Driver.java on line 76 and that ParallelContextHolder.set( new ParallelContextSimple(num_threads) ); is within the other ICHOOSE if statement in Driver.java on line 101.

To run q4 ensure that matrix = new SparseMatrixPipelined( inputFile, num_threads ); is the matrix created within the ICHOOSE if statement in Driver.java on line 76 and that ParallelContextHolder.set( new ParallelContextSingleThread() ); is within the other ICHOOSE if statement in Driver.java on line 101.
As per the instructions within the assignment I have added the threading code for the producer and consumer within the edgemap function in SparseMatrixPipelined so to assure that this threading code is used the ParallelContext needs to be set to the single thread one as described within the instructions for question 4.

# Q6

For any of the Q6 attempts ensure that ParallelContextHolder is set to PArallelContextSingleThread() as the thread creation and management is handled within SparseMatrixQ6, to do this make sure this line is inlculded in Driver before line 107 ParallelContextHolder.set( new ParallelContextSingleThread() );

# Command Line Arguments

- javac Driver.java
- java -ea Driver (pr|cc|ds) 1 ".\output.txt" (CSR|CSC|COO|ICHOOSE) ".\smallGraphs\rMatGraph_J_5_100.csr"