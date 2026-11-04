package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    private List<Edge> edgeList =new ArrayList<>();//список ребер графа
    private Map <Integer,List<Edge>> adjacencyMap =new HashMap<>();//мапа (ключ-верршина,значение-инцидентные ребра)

    public Graph(){}//пока не понятно какие данные поступают на вход вероятно файл
    // ,думаю написать отдлеьный конструтор если надо будет данные с файла, а по умолчанию  оставить таким

    public Graph (Graph original){//конструктор копирования
        //Graph GCopy=new Graph();
        for (Edge e:original.edgeList){
            this.edgeList.add(e);
        }
        for (int key:original.adjacencyMap.keySet()){
            List<Edge> incidentEdgeListCopy=new ArrayList<>();
            for (Edge incidentEdge:original.adjacencyMap.get(key)){
                incidentEdgeListCopy.add(incidentEdge);
            }
            this.adjacencyMap.put(key,incidentEdgeListCopy);
        }
    }

    public List<Edge> selfLoopSearch(){//поиск петель
        List<Edge> selfLoopList =new ArrayList<>();
        for (Edge e:edgeList){
            if(e.getU()==e.getV()){
                selfLoopList.add(e);
            }
        }
        return selfLoopList;
    }

    public List<Edge> bridgeSearch(){//поиск мостов с алгоритмом DFS
        List <Edge> bridgeList=new ArrayList<>();
        //алгоритм//
        return bridgeList;
    }

    public Graph deleteEdge(Edge e){
        Graph graphWithoutE=new Graph(this);
        graphWithoutE.edgeList.remove(e);
        graphWithoutE.adjacencyMap.get(e.getU()).remove(e);
        graphWithoutE.adjacencyMap.get(e.getV()).remove(e);
        return graphWithoutE;
    }

    public Graph сontractEdge(Edge e){
        Graph contractedGraph=new Graph(this);
        //алгоритм//
        return contractedGraph;
    }

    public List<Edge> getEdgeList() {
        return edgeList;
    }

    public Map<Integer, List<Edge>> getAdjacencyMap() {
        return adjacencyMap;
    }
}

