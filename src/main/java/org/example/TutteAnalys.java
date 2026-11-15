package org.example;

import java.math.BigInteger;

public class TutteAnalys {

    private final Polynomial2D tutte; // Готовый многочлен T(x,y).
    private final int n; // Число вершин исходного графа.
    private final int m; // Число рёбер исходного графа (петли учитываются).
    private final int k; // Число компонент связности исходного графа.

    /**
     * Конструктор анализатора.
     * Например, для треугольника: n=3, m=3, k=1.
     */

    public TutteAnalys(Polynomial2D tutte, int n, int m, int k) {
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

    // Позволяет другому классу получить исходное выражение T(x,y).
    public Polynomial2D getTuttePolynomial() { return tutte; }

    // Подстановка заданных пользователем x и y, если понадобится режим калькулятора.
    public BigInteger evaluateTutte(BigInteger x, BigInteger y) {
        return tutte.evaluate(x, y);
    }

    // Вариант с long удобен для примеров: evaluateTutte(2, 3).
    public BigInteger evaluateTutte(long x, long y) {
        return tutte.evaluate(x, y);
    }

    /**
     * ХРОМАТИЧЕСКИЙ МНОГОЧЛЕН P(q):
     * P(q) = (-1)^(n-k) * q^k * T(1-q, 0).
     * P(q) показывает число правильных раскрасок в q цветов.
     * Пример для треугольника: P(q) = q^3 - 3q^2 + 2q.
     */

    public Polynomial1D chromaticPolynomial() {
        // Создаём выражение (1-q), но пока как многочлен, а не число.
        // Символьная подстановка: формируем полином 1-q, не конкретное число.
        Polynomial1D oneMinusQ = Polynomial1D.constant(1)
                .add(Polynomial1D.q().scale(-1));

        // Временная сумма для символьной подстановки T(1-q,0).
        Polynomial1D sum = Polynomial1D.constant(0);

        // Подстановка y=0 обнуляет слагаемые с положительной степенью y.
        for (int degreeX = 0; degreeX <= tutte.getDegreeX(); degreeX++) {

            // Берём только члены с y^0, поскольку при y=0 все остальные исчезают.
            BigInteger coefficient = tutte.getCoefficient(degreeX, 0);

            // Член c*x^i преобразуется в c*(1-q)^i:
            // pow() возводит выражение в степень, scale() учитывает коэффициент.
            Polynomial1D term = oneMinusQ.pow(degreeX).scale(coefficient);
            sum = sum.add(term);
        }

        Polynomial1D result = Polynomial1D.q().pow(k).multiply(sum); // Множитель q^k учитывает количество компонент связности графа.

        // Учитываем знак (-1)^(n-k) по чётности показателя.
        if ((n - k) % 2 != 0) {
            result = result.scale(-1);
        }
        return result;
    }

    /**
     * ПОТОКОВЫЙ МНОГОЧЛЕН F(q):
     * F(q) = (-1)^(m-n+k) * T(0, 1-q).
     * F(q) считает ненулевые потоки над группой порядка q.
     * Пример для треугольника: F(q) = q-1.
     */

    public Polynomial1D flowPolynomial() {

        // Символьная подстановка: формируем полином 1-q, не конкретное число.
        Polynomial1D oneMinusQ = Polynomial1D.constant(1)
                .add(Polynomial1D.q().scale(-1));

        // Временная сумма для символьной подстановки T(0,1-q).
        Polynomial1D sum = Polynomial1D.constant(0);

        // Подстановка x=0 обнуляет слагаемые с положительной степенью x.
        for (int degreeY = 0; degreeY <= tutte.getDegreeY(); degreeY++) {
            // При x=0 остаются лишь члены с x^0; степень y сохраняется.
            BigInteger coefficient = tutte.getCoefficient(0, degreeY);

            // Заменяем y^j выражением (1-q)^j и умножаем на коэффициент.
            Polynomial1D term = oneMinusQ.pow(degreeY).scale(coefficient);
            sum = sum.add(term);
        }

        // Учитываем знак (-1)^(m-n+k) по чётности показателя.
        if ((m - n + k) % 2 != 0) {
            sum = sum.scale(-1);
        }
        return sum;
    }

    // Количество правильных раскрасок при конкретном q.
    public BigInteger coloringCount(BigInteger q) {
        // Последовательный вызов: сначала строим P(q), затем вычисляем значение.
        return chromaticPolynomial().evaluate(q);
    }

    // Количество нигде не нулевых потоков при конкретном q.
    public BigInteger flowCount(BigInteger q) {
        // Аналогично вычисляем F(q) в заданной точке q.
        return flowPolynomial().evaluate(q);
    }

    public BigInteger coloringCount(long q) {
        return chromaticPolynomial().evaluate(q);
    }

    public BigInteger flowCount(long q) {
        return flowPolynomial().evaluate(q);
    }

    /**
     * Хроматическое число — наименьшее количество цветов для раскраски.
     * Проверяем q = 1, 2, ..., n (для графа без петель этого достаточно).
     * Если при любом q раскрасок нет, возвращаем -1: в графе есть петля.
     */

    public int chromaticNumber() {
        if (n == 0) return 0; // Пустому графу не нужны цвета.

        Polynomial1D p = chromaticPolynomial();
        for (int q = 1; q <= n; q++) {
            // Первое q с ненулевым числом раскрасок является минимально допустимым.
            if (p.evaluate(q).signum() > 0) {
                return q; // Первое подходящее q — минимальное.
            }
        }
        return -1; // Для графа с петлёй правильная раскраска невозможна.
    }

    public boolean hasLoops() {
        // Наличие петли эквивалентно тождественной нулевости P_G(q).
        return chromaticPolynomial().isZero();
    }

    public boolean hasBridges() {
        // Наличие моста эквивалентно тождественной нулевости F_G(q).
        return flowPolynomial().isZero();
    }

    /**
     * Количество остоовных деревьев = T(1,1), но только если граф связный.
     * Для несвязного графа одного дерева на все вершины не существует.
     */

    public BigInteger spanningTreeCount() {
        // Несвязный граф не имеет единого остовного дерева для всех вершин.
        if (n == 0 || k != 1) return BigInteger.ZERO;
        // При связном графе T(1,1) — число остовных деревьев;
        // в общем случае — число максимальных остовных лесов.
        return tutte.evaluate(1, 1);
    }

    public BigInteger spanningForestCount() {

        // При связном графе T(1,1) — число остовных деревьев;
        // в общем случае — число максимальных остовных лесов.
        return tutte.evaluate(1, 1);
    }

    // Каждому ребру задаём направление без возникновения направленных циклов.
    // Количество таких ориентаций равно T(2,0).
    public BigInteger acyclicOrientationCount() {
        // Специальная точка T(2,0) определяет количество ациклических ориентаций.
        return tutte.evaluate(2, 0);
    }

    // Количество вполне циклических ориентаций равно T(0,2).
    public BigInteger totallyCyclicOrientationCount() {
        // Специальная точка T(0,2) определяет вполне циклические ориентации.
        return tutte.evaluate(0, 2);
    }

    // Доступ к характеристикам исходного графа.
    public int getVertexCount() { return n; }
    public int getEdgeCount() { return m; }
    public int getComponentCount() { return k; }
}
