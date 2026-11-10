package org.example;

import java.math.BigInteger;

// Анализ уже построенного многочлена T(x,y).
public class TutteAnalys {
    private final Polynomial2D tutte;
    private final int n; // число вершин
    private final int m; // число ребер
    private final int k; // число компонент связности

    public TutteAnalys(Polynomial2D tutte, int n, int m, int k) {
        if (n < 0 || m < 0 || k < 0 || k > n || (n > 0 && k == 0)) {
            throw new IllegalArgumentException("Неверные размеры графа");
        }
        this.tutte = tutte;
        this.n = n;
        this.m = m;
        this.k = k;
    }

    public Polynomial2D getTuttePolynomial() { return tutte; }

    public BigInteger evaluateTutte(BigInteger x, BigInteger y) {
        return tutte.evaluate(x, y);
    }

    // Упрощенный вызов для Main: пользователь вводит обычные целые числа.
    public BigInteger evaluateTutte(long x, long y) {
        return tutte.evaluate(x, y);
    }

    // Хроматический многочлен: P(q) = (-1)^(n-k) * q^k * T(1-q, 0).
    public Polynomial1D chromaticPolynomial() {
        Polynomial1D oneMinusQ = Polynomial1D.constant(1)
                .add(Polynomial1D.q().scale(-1));
        Polynomial1D sum = Polynomial1D.constant(0);

        // При y=0 все слагаемые с y исчезают.
        for (int degreeX = 0; degreeX <= tutte.getDegreeX(); degreeX++) {
            BigInteger coefficient = tutte.getCoefficient(degreeX, 0);
            sum = sum.add(oneMinusQ.pow(degreeX).scale(coefficient));
        }

        Polynomial1D result = Polynomial1D.q().pow(k).multiply(sum);
        if ((n - k) % 2 != 0) result = result.scale(-1);
        return result;
    }

    // Потоковый многочлен: F(q) = (-1)^(m-n+k) * T(0, 1-q).
    public Polynomial1D flowPolynomial() {
        Polynomial1D oneMinusQ = Polynomial1D.constant(1)
                .add(Polynomial1D.q().scale(-1));
        Polynomial1D sum = Polynomial1D.constant(0);

        // При x=0 все слагаемые с x исчезают.
        for (int degreeY = 0; degreeY <= tutte.getDegreeY(); degreeY++) {
            BigInteger coefficient = tutte.getCoefficient(0, degreeY);
            sum = sum.add(oneMinusQ.pow(degreeY).scale(coefficient));
        }

        if ((m - n + k) % 2 != 0) sum = sum.scale(-1);
        return sum;
    }

    public BigInteger coloringCount(BigInteger q) {
        return chromaticPolynomial().evaluate(q);
    }

    public BigInteger flowCount(BigInteger q) {
        return flowPolynomial().evaluate(q);
    }

    public BigInteger coloringCount(long q) {
        return chromaticPolynomial().evaluate(q);
    }

    public BigInteger flowCount(long q) {
        return flowPolynomial().evaluate(q);
    }

    // Минимальное q, при котором можно правильно раскрасить все вершины.
    // -1 означает, что правильная раскраска невозможна (есть петля).
    public int chromaticNumber() {
        if (n == 0) return 0;
        Polynomial1D p = chromaticPolynomial();
        for (int q = 1; q <= n; q++) {
            if (p.evaluate(q).signum() > 0) return q;
        }
        return -1;
    }

    public boolean hasLoops() { return chromaticPolynomial().isZero(); }
    public boolean hasBridges() { return flowPolynomial().isZero(); }

    // Для связного графа T(1,1) — количество остовных деревьев.
    public BigInteger spanningTreeCount() {
        if (n == 0 || k != 1) return BigInteger.ZERO;
        return tutte.evaluate(1, 1);
    }

    // В каждой компоненте выбираем по одному остовному дереву.
    public BigInteger spanningForestCount() {
        return tutte.evaluate(1, 1);
    }

    public BigInteger acyclicOrientationCount() {
        return tutte.evaluate(2, 0);
    }

    public BigInteger totallyCyclicOrientationCount() {
        return tutte.evaluate(0, 2);
    }

    public int getVertexCount() { return n; }
    public int getEdgeCount() { return m; }
    public int getComponentCount() { return k; }
}