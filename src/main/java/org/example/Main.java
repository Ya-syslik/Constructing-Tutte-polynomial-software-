package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args){
        TutteCompute computer=new TutteCompute();

        // K3
        {
            List<Edge> e = new ArrayList<>();
            e.add(new Edge(1,2)); e.add(new Edge(2,3)); e.add(new Edge(1,3));
            Map<Integer,List<Edge>> m = new HashMap<>();
            m.put(1, List.of(e.get(0), e.get(2)));
            m.put(2, List.of(e.get(0), e.get(1)));
            m.put(3, List.of(e.get(1), e.get(2)));
            System.out.println("K3: " + computer.compute(new Graph(e, m)));
        }

        // K2
        {
            List<Edge> e = new ArrayList<>();
            e.add(new Edge(1,2));
            Map<Integer,List<Edge>> m = new HashMap<>();
            m.put(1, List.of(e.get(0)));
            m.put(2, List.of(e.get(0)));
            System.out.println("K2: " + computer.compute(new Graph(e, m)));
        }

        // P3
        {
            List<Edge> e = new ArrayList<>();
            e.add(new Edge(1,2)); e.add(new Edge(2,3));
            Map<Integer,List<Edge>> m = new HashMap<>();
            m.put(1, List.of(e.get(0)));
            m.put(2, List.of(e.get(0), e.get(1)));
            m.put(3, List.of(e.get(1)));
            System.out.println("P3: " + computer.compute(new Graph(e, m)));
        }

        // C4
        {
            List<Edge> e = new ArrayList<>();
            e.add(new Edge(1,2)); e.add(new Edge(2,3)); e.add(new Edge(3,4)); e.add(new Edge(4,1));
            Map<Integer,List<Edge>> m = new HashMap<>();
            m.put(1, List.of(e.get(0), e.get(3)));
            m.put(2, List.of(e.get(0), e.get(1)));
            m.put(3, List.of(e.get(1), e.get(2)));
            m.put(4, List.of(e.get(2), e.get(3)));
            System.out.println("C4: " + computer.compute(new Graph(e, m)));
        }

        // Бабочка
        {
            List<Edge> e = new ArrayList<>();
            e.add(new Edge(1,2)); e.add(new Edge(2,3)); e.add(new Edge(1,3));
            e.add(new Edge(1,4)); e.add(new Edge(4,5)); e.add(new Edge(1,5));
            Map<Integer,List<Edge>> m = new HashMap<>();
            m.put(1, List.of(e.get(0), e.get(2), e.get(3), e.get(5)));
            m.put(2, List.of(e.get(0), e.get(1)));
            m.put(3, List.of(e.get(1), e.get(2)));
            m.put(4, List.of(e.get(3), e.get(4)));
            m.put(5, List.of(e.get(4), e.get(5)));
            System.out.println("Бабочка: " + computer.compute(new Graph(e, m)));
        }

        // Звезда
        {
            List<Edge> e = new ArrayList<>();
            e.add(new Edge(1,2)); e.add(new Edge(1,3)); e.add(new Edge(1,4));
            Map<Integer,List<Edge>> m = new HashMap<>();
            m.put(1, List.of(e.get(0), e.get(1), e.get(2)));
            m.put(2, List.of(e.get(0)));
            m.put(3, List.of(e.get(1)));
            m.put(4, List.of(e.get(2)));
            System.out.println("K1,3: " + computer.compute(new Graph(e, m)));
        }

        // K4
        {
            List<Edge> e = new ArrayList<>();
            e.add(new Edge(1,2)); e.add(new Edge(1,3)); e.add(new Edge(1,4));
            e.add(new Edge(2,3)); e.add(new Edge(2,4)); e.add(new Edge(3,4));
            Map<Integer,List<Edge>> m = new HashMap<>();
            m.put(1, List.of(e.get(0), e.get(1), e.get(2)));
            m.put(2, List.of(e.get(0), e.get(3), e.get(4)));
            m.put(3, List.of(e.get(1), e.get(3), e.get(5)));
            m.put(4, List.of(e.get(2), e.get(4), e.get(5)));
            System.out.println("K4: " + computer.compute(new Graph(e, m)));
        }

        //тут жду вывод свойств в консоль

    }
}
