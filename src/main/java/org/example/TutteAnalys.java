package org.example;

import java.math.BigInteger;

// Вычисляет характеристики графа по готовому многочлену Татта T(x,y).
public class TutteAnalys {

    private final Polynomial2D tutte; // Готовый многочлен T(x,y).
    private final int n; // Число вершин исходного графа.
    private final int m; // Число рёбер, включая петли и параллельные рёбра.
    private final int k; // Число компонент связности исходного графа.

    // Конструктор анализатора. Для треугольника: n=3, m=3, k=1.
    public TutteAnalys(Polynomial2D tutte, int n, int m, int k) {
        // Проверяем допустимость количества вершин, рёбер и компонент.
        if (n < 0 || m < 0 || k < 0 || k > n || (n > 0 && k == 0)) {
            throw new IllegalArgumentException("Неверные размеры графа");
        }
        if (tutte == null) {
            throw new IllegalArgumentException("Многочлен Татта не задан");
        }

        this.tutte = tutte;
        this.n = n;
        this.m = m;
        this.k = k;
    }

    // Возвращает исходный многочлен Татта.
    public Polynomial2D getTuttePolynomial() {
        return tutte;
    }

    // Подставляет произвольные большие целые x и y в T(x,y).
    public BigInteger evaluateTutte(BigInteger x, BigInteger y) {
        return tutte.evaluate(x, y);
    }

    // Перегрузка для обычных целых: evaluateTutte(2, 3).
    public BigInteger evaluateTutte(long x, long y) {
        return tutte.evaluate(x, y);
    }

    // P(q) = (-1)^(n-k) * q^k * T(1-q,0) — хроматический многочлен.
    // P(q) показывает число правильных раскрасок в q цветов.
    // Для треугольника: P(q) = q^3 - 3q^2 + 2q.
    public Polynomial1D chromaticPolynomial() {
        // Формируем символьное выражение 1-q вместо конкретного числа.
        Polynomial1D oneMinusQ = Polynomial1D.constant(1)
                .add(Polynomial1D.q().scale(BigInteger.valueOf(-1)));

        // В этой переменной постепенно накапливается T(1-q,0).
        Polynomial1D sum = Polynomial1D.constant(0);

        // При y=0 остаются только слагаемые с нулевой степенью y.
        for (int degreeX = 0; degreeX <= tutte.getDegreeX(); degreeX++) {
            BigInteger coefficient = tutte.getCoefficient(degreeX, 0);

            // c*x^i заменяем на c*(1-q)^i, затем прибавляем к сумме.
            Polynomial1D term = oneMinusQ.pow(degreeX).scale(coefficient);
            sum = sum.add(term);
        }

        // Множитель q^k учитывает количество компонент связности.
        Polynomial1D result = Polynomial1D.q().pow(k).multiply(sum);

        // (-1)^(n-k) меняет знак, если показатель нечётный.
        if ((n - k) % 2 != 0) {
            result = result.scale(BigInteger.valueOf(-1));
        }
        return result;
    }

    // F(q) = (-1)^(m-n+k) * T(0,1-q) — потоковый многочлен.
    // F(q) считает нигде не нулевые потоки над группой порядка q.
    // Для треугольника: F(q) = q-1.
    public Polynomial1D flowPolynomial() {
        // Формируем многочлен 1-q для символьной подстановки вместо y.
        Polynomial1D oneMinusQ = Polynomial1D.constant(1)
                .add(Polynomial1D.q().scale(BigInteger.valueOf(-1)));

        // Здесь накапливается T(0,1-q).
        Polynomial1D sum = Polynomial1D.constant(0);

        // При x=0 остаются только слагаемые с нулевой степенью x.
        for (int degreeY = 0; degreeY <= tutte.getDegreeY(); degreeY++) {
            BigInteger coefficient = tutte.getCoefficient(0, degreeY);

            // c*y^j заменяем на c*(1-q)^j, затем прибавляем к сумме.
            Polynomial1D term = oneMinusQ.pow(degreeY).scale(coefficient);
            sum = sum.add(term);
        }

        // (-1)^(m-n+k) меняет знак, если показатель нечётный.
        if ((m - n + k) % 2 != 0) {
            sum = sum.scale(BigInteger.valueOf(-1));
        }
        return sum;
    }

    // Количество правильных раскрасок при заданном q типа BigInteger.
    public BigInteger coloringCount(BigInteger q) {
        // Сначала строим P(q), затем вычисляем его значение.
        return chromaticPolynomial().evaluate(q);
    }

    // Количество нигде не нулевых потоков при заданном q типа BigInteger.
    public BigInteger flowCount(BigInteger q) {
        // Сначала строим F(q), затем вычисляем его значение.
        return flowPolynomial().evaluate(q);
    }

    // Перегрузка для обычного целого значения q.
    public BigInteger coloringCount(long q) {
        return chromaticPolynomial().evaluate(q);
    }

    // Перегрузка для обычного целого значения q.
    public BigInteger flowCount(long q) {
        return flowPolynomial().evaluate(q);
    }

    // Хроматическое число — минимальное число цветов для правильной раскраски.
    // -1 означает наличие петли, 0 — граф без вершин.
    public int chromaticNumber() {
        if (n == 0) {
            return 0; // Для пустого графа цвета не требуются.
        }

        Polynomial1D p = chromaticPolynomial();

        // Для графа без петель достаточно проверить q от 1 до n.
        for (int q = 1; q <= n; q++) {
            // Первое q с положительным P(q) — минимальное число цветов.
            if (p.evaluate(q).signum() > 0) {
                return q;
            }
        }
        return -1; // С петлёй правильная раскраска невозможна.
    }

    // Наличие петли равносильно тождественной нулевости P(q).
    public boolean hasLoops() {
        return chromaticPolynomial().isZero();
    }

    // Наличие моста равносильно тождественной нулевости F(q).
    public boolean hasBridges() {
        return flowPolynomial().isZero();
    }

    // T(1,1) — число остовных деревьев связного графа.
    // Для несвязного графа единого остовного дерева не существует.
    public BigInteger spanningTreeCount() {
        if (n == 0 || k != 1) {
            return BigInteger.ZERO;
        }
        return maximalSpanningForestCount();
    }

    // T(2,1) — все остовные леса, включая немаксимальные.
    public BigInteger spanningForestCount() {
        return tutte.evaluate(2, 1);
    }

    // T(1,1) — максимальные остовные леса: по дереву в каждой компоненте.
    public BigInteger maximalSpanningForestCount() {
        return tutte.evaluate(1, 1);
    }

    // T(1,2) — остовные подграфы, сохраняющие компоненты связности: k(A)=k(E).
    public BigInteger componentPreservingSpanningSubgraphCount() {
        return tutte.evaluate(1, 2);
    }

    // T(2,0) — число ациклических ориентаций рёбер графа.
    public BigInteger acyclicOrientationCount() {
        return tutte.evaluate(2, 0);
    }

    // T(0,2) — число вполне циклических ориентаций рёбер графа.
    public BigInteger totallyCyclicOrientationCount() {
        return tutte.evaluate(0, 2);
    }

    // T(2,2)=2^m — все остовные подграфы: каждое ребро можно оставить или удалить.
    public BigInteger spanningSubgraphCount() {
        return tutte.evaluate(2, 2);
    }

    // Доступ к размерам исходного графа.
    public int getVertexCount() {
        return n;
    }

    public int getEdgeCount() {
        return m;
    }

    public int getComponentCount() {
        return k;
    }
}
