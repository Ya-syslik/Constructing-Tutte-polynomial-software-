package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    private List<Edge> edgeList = new ArrayList<>();//список ребер графа
    private Map<Integer, List<Edge>> adjacencyMap = new HashMap<>();//мапа (ключ-верршина,значение-инцидентные ребра)

    public Graph() {
    }//пока не понятно какие данные поступают на вход вероятно файл
    // ,думаю написать отдлеьный конструтор если надо будет данные с файла, а по умолчанию  оставить таким

    public Graph(List edgeList, Map adjacencyMap) {//констуктор для тестов
        this.edgeList = edgeList;
        this.adjacencyMap = adjacencyMap;
    }

    public Graph(Graph original) {//конструктор копирования
        for (Edge e : original.edgeList) {
            this.edgeList.add(e);
        }
        for (int key : original.adjacencyMap.keySet()) {
            List<Edge> incidentEdgeListCopy = new ArrayList<>();
            for (Edge incidentEdge : original.adjacencyMap.get(key)) {
                incidentEdgeListCopy.add(incidentEdge);
            }
            this.adjacencyMap.put(key, incidentEdgeListCopy);
        }
    }

    public List<Edge> getEdgeList() {
        return edgeList;
    }

    public Map<Integer, List<Edge>> getAdjacencyMap() {
        return adjacencyMap;
    }


    private void addEdgeInternal(Edge addingEdge){
        edgeList.add(addingEdge);

        int u=addingEdge.getU();
        int v=addingEdge.getV();
        if(u!=v){
            adjacencyMap.get(u).add(addingEdge);
            adjacencyMap.get(v).add(addingEdge);
        }
        else if (u==v){
            adjacencyMap.get(u).add(addingEdge);
        }
    }

    private void removeEdgeInternal(Edge removingEdge){
        edgeList.remove(removingEdge);

        int u=removingEdge.getU();
        int v=removingEdge.getV();
        adjacencyMap.get(u).remove(removingEdge);
        if (u!=v) {
            adjacencyMap.get(v).remove(removingEdge);
        }
    }

    //поиск соседа по ребру для вершины
    private int getOtherVertex(int vertex,Edge edge){
        int otherVertex=edge.getU()==vertex? edge.getV():edge.getU();
        return otherVertex;
    }

    public Graph deleteEdge(Edge e){
        Graph withoutEdgeGraph= new Graph(this);
        withoutEdgeGraph.removeEdgeInternal(e);
        return withoutEdgeGraph;
    }

    public Graph contractEdge(Edge e){

        int survivingVertex=e.getU();
        int deletingVertex=e.getV();

        //проверка на передачу в метод петли (нельзя стгивать петлю,возвращаем исходный граф)
        if (survivingVertex==deletingVertex){
            return this;
        }

        Graph contractedGraph=deleteEdge(e);

        //копия для цикла,так как внутри него из структуры удаляются ребра
        List<Edge> allEdgesFromDeletingVertex = new ArrayList<>(contractedGraph.getAdjacencyMap().get(deletingVertex));

        for (Edge eFromDelV :allEdgesFromDeletingVertex) {
            int neighbour=getOtherVertex(deletingVertex,eFromDelV);
            //если у удаляемой вершины была петля,то она полностью переходит на survivingVertex
            int targetVertex=(neighbour==deletingVertex)?survivingVertex:neighbour;

            Edge afterContactionEdge=new Edge(survivingVertex,targetVertex);
            contractedGraph.removeEdgeInternal(eFromDelV);
            contractedGraph.addEdgeInternal(afterContactionEdge);
        }

        contractedGraph.getAdjacencyMap().remove(deletingVertex);

        return contractedGraph;
    }


    public List<Edge> selfLoopSearch() {//поиск петель
        List<Edge> selfLoopList = new ArrayList<>();
        for (Edge e : edgeList) {
            if (e.getU() == e.getV()) {
                selfLoopList.add(e);
            }
        }
        return selfLoopList;
    }


    //структуры для алгоритма поиска мостов
    private enum VertexColor {
        WHITE,
        GREY,
        BLACK
    }

    private enum EdgeDirection {
        UV,
        VU,
        UNDIRECTED
    }

    //цвет-вершина
    private Map<Integer, VertexColor> vertexColorMap = new HashMap<>();
    private void vertexColorMapStartInit() {
        for (int vertex : adjacencyMap.keySet()) {
            vertexColorMap.put(vertex, VertexColor.WHITE);
        }
    }

    //вершина-номер (цвет) компоненты связности
    private Map<Integer, Integer> vertexConnectedComponent = new HashMap<>();
    private int connectedComponentCnt = 0;//номер цвета первой компоненты (можно поменять на 1)

    //ребро ориентация
    private Map<Edge, EdgeDirection> edgeDirectionMap = new HashMap<>();
    private void edgeDirectionMapStartInit() {
        for (Edge e : edgeList) {
            edgeDirectionMap.put(e, EdgeDirection.UNDIRECTED);
        }
    }

    //порядок обхода вершин
    List<Integer> vertexOrder=new ArrayList<>();

    public List<Edge> bridgeSearch(){//поиск мостов с алгоритмом DFS
        List <Edge> bridgeList=new ArrayList<>();

        vertexColorMapStartInit();
        edgeDirectionMapStartInit();

        //первый проход DFS
        for (int vertex:adjacencyMap.keySet()){
            if(vertexColorMap.get(vertex)==VertexColor.WHITE){
                orientingDFS(vertex);
            }
        }

        //ребра ориентрированны, список обхода готов

        for (int vertex:vertexOrder){
            //если вершина до сих пор не принадлежит ни одной из компонент связности
            // (которые находятся во время второго прохода)
            if (!vertexConnectedComponent.containsKey(vertex)) {
                //создаем новую компоненту связности
                vertexConnectedComponent.put(vertex,connectedComponentCnt);
                findConnectedComp(vertex,connectedComponentCnt);
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

    //первый проход DFS ориентирует ребра графа
    private void orientingDFS(int currentVertex) {

        vertexOrder.add(currentVertex);//добавляем в список для второго обхода
        vertexColorMap.put(currentVertex, VertexColor.GREY);//обрабатываемая вершина стала серой

        for (Edge e : adjacencyMap.get(currentVertex)) {

            int neighbourVertex = getOtherVertex(currentVertex, e);
            VertexColor neighbourVertexColor = vertexColorMap.get(neighbourVertex);

            if (neighbourVertexColor == VertexColor.WHITE) {
                if (neighbourVertex == e.getU()) {
                    edgeDirectionMap.put(e, EdgeDirection.UV);
                } else {
                    edgeDirectionMap.put(e, EdgeDirection.VU);
                }
                orientingDFS(neighbourVertex);
            }
            //  если сосед серый то он замыкает цикл такое ребро отсается неориентированным и уже обработано
            else if (neighbourVertexColor == VertexColor.GREY || neighbourVertexColor == VertexColor.BLACK) {
                continue;
            }
        }

        //на этот момент стек рекурсии для этой вершины завершен,
        // то есть все ветки дерева рекурсии после текущей вершины уже черные
        vertexColorMap.put(currentVertex, VertexColor.BLACK);
    }

    //проверка проходимости
    private boolean canPass(Edge way, int fromVertex, EdgeDirection dir) {
        if (dir == EdgeDirection.UNDIRECTED) {
            return true;
        }
        if (dir == EdgeDirection.UV && fromVertex == way.getU()) {
            return true;
        }
        return (dir == EdgeDirection.VU && fromVertex == way.getV());
    }

    //второй проход DFS
    //нахождение ровно одной компоненты связности от переданной в функцию вершины
    private void findConnectedComp(int vertex,  int connectedComponentCnt) {

        for (Edge way : adjacencyMap.get(vertex)) {
            int neighbourByWay = getOtherVertex(vertex, way);
            //если сосед необработан (ему не присвоена компонента связности)
            if (!vertexConnectedComponent.containsKey(neighbourByWay) && canPass(way, vertex, edgeDirectionMap.get(way))) {
                vertexConnectedComponent.put(neighbourByWay, connectedComponentCnt);
                findConnectedComp(neighbourByWay,  connectedComponentCnt);
            }
        }
    }

}

