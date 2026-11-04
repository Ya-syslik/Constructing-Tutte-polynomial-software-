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

    //поиск соседа по ребру для вершины
    private int getOtherVertex(int vertex,Edge edge){
        int otherVertex=edge.getU()==vertex? edge.getV():edge.getU();
        return otherVertex;
    }

    public Graph contractEdge(Edge e){
        Graph contractedGraph=new Graph(this);

        int firstVertex =e.getU();//эта результат стягивания
        int secondVertex =e.getV();//эту удаляем

        contractedGraph.deleteEdge(e);//удаляем ребро между вершинами

        //создаем копию списка, чтобы пройтись по нему без исключений
        // (внутри цикла удаляются ребра, что приводит к крашу)
        List<Edge> vIncidentEdgesCopy =new ArrayList<>();
        for (Edge edgeForCopy: contractedGraph.adjacencyMap.get(secondVertex)){
            Edge edgeCopy = edgeForCopy;
            vIncidentEdgesCopy.add(edgeCopy);
        }

        //создаем новое ребро,
        // так как Edge неизменяем мы вместо перезаписи вершин в уже существующие ребра
        //удаляем старые и создаем новые ребра с корректными вершинами
        for (Edge incidentEdge: vIncidentEdgesCopy){

            int neighbourVertex= getOtherVertex(secondVertex,incidentEdge);//находим соседа удаляемой вершины

            Edge afterContractinonEdge=new Edge(neighbourVertex, firstVertex);//создаем новое ребро для списка структур

            //обновляем список смежных ребер для первой вершины
            contractedGraph.adjacencyMap.get(firstVertex).add(afterContractinonEdge);

            //обновляем список смежных ребер для соседа
            contractedGraph.adjacencyMap.get(neighbourVertex).add(afterContractinonEdge);
            contractedGraph.adjacencyMap.get(neighbourVertex).remove(incidentEdge);

            //обновляем список ребер
            contractedGraph.edgeList.add(afterContractinonEdge);
            contractedGraph.edgeList.remove(incidentEdge);
        }

        //удаляем вторую вершину
        contractedGraph.adjacencyMap.remove(secondVertex);

        return contractedGraph;
    }

    public List<Edge> getEdgeList() {
        return edgeList;
    }

    public Map<Integer, List<Edge>> getAdjacencyMap() {
        return adjacencyMap;
    }
}

