package com.example.manalighare.mapColoring;

import java.util.LinkedList;

public class Graph {
    private int V; // No. of vertices
    private LinkedList<Integer> adj[]; //Adjacency List

    public Graph(int v) {
        this.V = v;
        this.adj = new LinkedList[v];
        for (int i=0; i<v; i++)
            adj[i] = new LinkedList();
    }


    //Function to add an edge into the graph
    void addEdge(int v,int w)
    {
        adj[v].add(w);
        adj[w].add(v); //Graph is undirected
    }

    public int getV() {
        return V;
    }

    public void setV(int v) {
        V = v;
    }

    public LinkedList<Integer>[] getAdj() {
        return adj;
    }

    public void setAdj(LinkedList<Integer>[] adj) {
        this.adj = adj;
    }
}
