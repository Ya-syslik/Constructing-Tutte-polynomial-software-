package org.example;

import java.util.List;

public class TutteCompute {
    public  Polynomial2D compute(Graph graph){
        //получаем список ребер
        List<Edge> edges=graph.getEdgeList();

        //базовый случай (все ребра удалили)
        if (graph.getEdgeList().isEmpty()){
            return Polynomial2D.constant(1);
        }

        //неважно какое ребро брать так что берем первое из оставшихся
        Edge currentEdge=edges.get(0);

        //списки мостов и петель получаем
        List<Edge> selfLoops=graph.selfLoopSearch();
        List<Edge> bridges=graph.bridgeSearch();

        //петля
        if (selfLoops.contains(currentEdge)){
            //удаляем петлю
            Graph withoutSelfLoop = graph.deleteEdge(currentEdge);
            //умножаем ветку рекурсии на y и продолжаем рекурсию внутри данной ветки
            Polynomial2D result = compute(withoutSelfLoop);
            return result.multiplyByY();
        }

        //мост
        if (bridges.contains(currentEdge)){
            //мосты мы стягиваем
            Graph withoutBridge=graph.contractEdge(currentEdge);
            //умножаем ветку рекурсии на x и продолжаем рекурсию внутри данной ветки
            Polynomial2D result = compute(withoutBridge);
            return result.multiplyByX();
        }

        //обычное ребро (не мост,не петля)

        //тут мы разбиваем рекурсию на две ветки со стянутым ребром и с удаленным
        Graph deletedEdgeGraph =graph.deleteEdge(currentEdge);
        Graph contractedEdgeGraph =graph.contractEdge(currentEdge);

        Polynomial2D deletedCase = compute(deletedEdgeGraph);
        Polynomial2D contractedCase = compute(contractedEdgeGraph);

        //все ветки мы в итоге складываем
        return deletedCase.add(contractedCase);
    }
}
