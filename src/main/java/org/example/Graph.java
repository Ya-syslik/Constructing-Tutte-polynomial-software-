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

    private enum VertexColor{
        WHITE,
        GREY,
        BLACK
    }

    private enum EdgeDirection{
        UV,
        VU,
        UNDIRECTED
    }

    private void orientingDFS(int currentVertex,Map<Integer,VertexColor> vertexColorMap, Map<Edge,EdgeDirection> edgeDirectionMap,
            Map<Integer,List<Edge>> adjacencyMap,List<Integer> vertexOrder){

        vertexOrder.add(currentVertex);//добавляем в список для второго обхода
        vertexColorMap.put(currentVertex,VertexColor.GREY);//обрабатываемая вершина стала серой


        for (Edge e:adjacencyMap.get(currentVertex)){

            int neighbourVertex=getOtherVertex(currentVertex,e);
            VertexColor neighbourVertexColor=vertexColorMap.get(neighbourVertex);

            if (neighbourVertexColor==VertexColor.WHITE){
                if (neighbourVertex==e.getU()){
                    edgeDirectionMap.put(e,EdgeDirection.UV);
                }
                else{
                    edgeDirectionMap.put(e,EdgeDirection.VU);
                }
                orientingDFS(neighbourVertex,vertexColorMap, edgeDirectionMap,adjacencyMap,vertexOrder);
            }
            // если мы если сосед серый то он замыкает цикл такое ребро отсается неориентированным и уже обработано
            else if (neighbourVertexColor==VertexColor.GREY||neighbourVertexColor==VertexColor.BLACK){
                continue;
            }
        }

        //на этот момент стек рекурсии для этой вершины завершен,
        // то есть все ветки дерева рекурсии после текущей вершины уже черные
        vertexColorMap.put(currentVertex,VertexColor.BLACK);
    }

    private void findConnectedComp(int vertex,Map<Integer,Integer> vertexConnectedComponent,int connectedComponentCnt,
                                   Map<Edge,EdgeDirection> edgeDirectionMap){


        List<Edge> ways = adjacencyMap.get(vertex);
        for (Edge way:ways){

            int neighbourByWay=getOtherVertex(vertex,way);
            //если сосед необработан (ему не присвоена компонента связности)
            //решил что лучше так, чем опять использовать WHITE-GREY-BLACK
            if (!vertexConnectedComponent.containsKey(neighbourByWay)) {
                //проверка проходимости
                if (edgeDirectionMap.get(way) == EdgeDirection.UNDIRECTED) {
                    vertexConnectedComponent.put(neighbourByWay, connectedComponentCnt);
                    findConnectedComp(neighbourByWay,vertexConnectedComponent,connectedComponentCnt, edgeDirectionMap);
                }
                else if ((edgeDirectionMap.get(way) == EdgeDirection.UV) && (way.getU() == vertex)) {
                    vertexConnectedComponent.put(neighbourByWay, connectedComponentCnt);
                    findConnectedComp(neighbourByWay,vertexConnectedComponent,connectedComponentCnt, edgeDirectionMap);
                }
                else if ((edgeDirectionMap.get(way) == EdgeDirection.VU) && (way.getV() == vertex)) {
                    vertexConnectedComponent.put(neighbourByWay, connectedComponentCnt);
                    findConnectedComp(neighbourByWay,vertexConnectedComponent,connectedComponentCnt, edgeDirectionMap);
                }
            }
        }
    }

    public List<Edge> bridgeSearch(){//поиск мостов с алгоритмом DFS
        List <Edge> bridgeList=new ArrayList<>();

        List<Integer> vertexOrder=new ArrayList<>();

        //цвет-вершина
        Map<Integer,VertexColor> vertexColorMap=new HashMap<>();
        for (int vertex:adjacencyMap.keySet()){
            vertexColorMap.put(vertex,VertexColor.WHITE);
        }

        //ребро ориентация
        Map<Edge,EdgeDirection> edgeDirectionMap =new HashMap<>();
        for (Edge e:edgeList){
            edgeDirectionMap.put(e,EdgeDirection.UNDIRECTED);
        }

        //первый проход DFS
        for (int vertex:adjacencyMap.keySet()){
            if(vertexColorMap.get(vertex)==VertexColor.WHITE){
                orientingDFS(vertex,vertexColorMap, edgeDirectionMap,adjacencyMap,vertexOrder);
            }
        }
        //ребра ориентрированны, список обхода готов

        //вершина-номер (цвет) компоненты связности
        Map<Integer,Integer> vertexConnectedComponent =new HashMap<>();
        int connectedComponentCnt=0;//номер цвета первой компоненты (можно поменять на 1)

        for (int vertex:vertexOrder){
            //если вершина до сих пор не принадлежит ни одной из компонент связности
            // (которые находятся во время второго прохода)
            if (!vertexConnectedComponent.containsKey(vertex)) {
                //создаем новую компоненту связности
                vertexConnectedComponent.put(vertex,connectedComponentCnt);
                findConnectedComp(vertex,vertexConnectedComponent,connectedComponentCnt, edgeDirectionMap);
                connectedComponentCnt++;
            }
        }

        //у нас есть список компонент связности разных цветов,
        //теперь мы просто ищем ребра,у которых вершины в разных компонентах
        for (Edge e:edgeList){
            //цвета вершин
            int uComp=vertexConnectedComponent.get(e.getU());
            int vComp=vertexConnectedComponent.get(e.getV());

            if (uComp!=vComp){
                bridgeList.add(e);
            }
        }
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

