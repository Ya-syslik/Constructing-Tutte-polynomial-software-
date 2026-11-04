package org.example;

//сеттеров,чтобы класс оставался имутабельным (так безопаснее)
public class Edge {
    private int uVertex;
    private int vVertex;
    Edge(int u,int v){
        if (u>=v){
            this.uVertex=v;
            this.vVertex=u;
        }
        else{
            this.uVertex=u;
            this.vVertex=v;
        }
    }

    public int getU() {
        return uVertex;
    }

    public int getV() {
        return vVertex;
    }
}
