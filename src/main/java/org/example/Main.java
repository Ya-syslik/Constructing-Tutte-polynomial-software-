package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args){
        TutteCompute computer=new TutteCompute();
        List<Edge> testEdgeList=new ArrayList<>();
        Map<Integer,List<Edge>> testMap = new HashMap<>();

        testEdgeList.add(new Edge(1,2));
        testEdgeList.add(new Edge(2,3));
        testEdgeList.add(new Edge(1,3));

        List<Edge> first = new ArrayList<>();
        first.add(testEdgeList.get(0));
        first.add(testEdgeList.get(2));

        List<Edge> second = new ArrayList<>();
        second.add(testEdgeList.get(0));
        second.add(testEdgeList.get(1));

        List<Edge> third = new ArrayList<>();
        third.add(testEdgeList.get(1));
        third.add(testEdgeList.get(2));

        testMap.put(1,first);
        testMap.put(2,second);
        testMap.put(3,third);


        Graph testGraph=new Graph(testEdgeList,testMap);

        System.out.println(computer.compute(testGraph));

        //тут жду вывод свойств в консоль

    }
}
