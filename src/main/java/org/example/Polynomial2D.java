package org.example;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Многочлен T(x, y). Например: 2x^2 + 3xy + y.
public class Polynomial2D {

    // Одно слагаемое задают две степени: степень x и степень y.
    public static class Monomial {
        private final int degreeX;
        private final int degreeY;

        public Monomial(int degreeX, int degreeY) {
            if (degreeX < 0 || degreeY < 0) {
                throw new IllegalArgumentException("Степени должны быть неотрицательными");
            }
            this.degreeX = degreeX;
            this.degreeY = degreeY;
        }

        public int getDegreeX() { return degreeX; }
        public int getDegreeY() { return degreeY; }

        // Нужны, чтобы Map считала одинаковые степени одним ключом.
        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Monomial)) return false;
            Monomial other = (Monomial) object;
            return degreeX == other.degreeX && degreeY == other.degreeY;
        }

        @Override
        public int hashCode() {
            return 31 * degreeX + degreeY;
        }
    }

    // Ключ: (степень x, степень y). Значение: коэффициент.
    private final Map<Monomial, BigInteger> terms = new HashMap<>();

    private Polynomial2D() {}

    // Добавление одного слагаемого (используется только внутри класса).
    private void addTerm(int degreeX, int degreeY, BigInteger coefficient) {
        Monomial key = new Monomial(degreeX, degreeY);
        BigInteger previous = terms.getOrDefault(key, BigInteger.ZERO);
        BigInteger sum = previous.add(coefficient);
        if (sum.signum() == 0) {
            terms.remove(key); // Слагаемые с нулевыми коэффициентами не храним.
        } else {
            terms.put(key, sum);
        }
    }

    public static Polynomial2D constant(long number) {
        return monomial(0, 0, BigInteger.valueOf(number));
    }

    public static Polynomial2D constant(BigInteger number) {
        return monomial(0, 0, number);
    }

    public static Polynomial2D x() {
        return monomial(1, 0, BigInteger.ONE);
    }

    public static Polynomial2D y() {
        return monomial(0, 1, BigInteger.ONE);
    }

    public static Polynomial2D monomial(int degreeX, int degreeY, BigInteger coefficient) {
        Polynomial2D result = new Polynomial2D();
        result.addTerm(degreeX, degreeY, coefficient);
        return result;
    }

    // Сложение: одинаковые степени объединяются.
    public Polynomial2D add(Polynomial2D other) {
        Polynomial2D result = new Polynomial2D();
        for (Map.Entry<Monomial, BigInteger> entry : terms.entrySet()) {
            Monomial m = entry.getKey();
            result.addTerm(m.degreeX, m.degreeY, entry.getValue());
        }
        for (Map.Entry<Monomial, BigInteger> entry : other.terms.entrySet()) {
            Monomial m = entry.getKey();
            result.addTerm(m.degreeX, m.degreeY, entry.getValue());
        }
        return result;
    }

    // Умножение: коэффициенты перемножаются, степени складываются.
    public Polynomial2D multiply(Polynomial2D other) {
        Polynomial2D result = new Polynomial2D();
        for (Map.Entry<Monomial, BigInteger> first : terms.entrySet()) {
            for (Map.Entry<Monomial, BigInteger> second : other.terms.entrySet()) {
                int xDegree = first.getKey().degreeX + second.getKey().degreeX;
                int yDegree = first.getKey().degreeY + second.getKey().degreeY;
                BigInteger value = first.getValue().multiply(second.getValue());
                result.addTerm(xDegree, yDegree, value);
            }
        }
        return result;
    }

    public Polynomial2D multiplyByX() { return multiply(x()); }
    public Polynomial2D multiplyByY() { return multiply(y()); }

    // Подстановка чисел в T(x, y).
    public BigInteger evaluate(BigInteger x, BigInteger y) {
        BigInteger answer = BigInteger.ZERO;
        for (Map.Entry<Monomial, BigInteger> entry : terms.entrySet()) {
            Monomial m = entry.getKey();
            BigInteger term = entry.getValue()
                    .multiply(x.pow(m.degreeX))
                    .multiply(y.pow(m.degreeY));
            answer = answer.add(term);
        }
        return answer;
    }

    public BigInteger evaluate(long x, long y) {
        return evaluate(BigInteger.valueOf(x), BigInteger.valueOf(y));
    }

    public BigInteger getCoefficient(int degreeX, int degreeY) {
        return terms.getOrDefault(new Monomial(degreeX, degreeY), BigInteger.ZERO);
    }

    // Возвращаем только для чтения, чтобы другой класс случайно не изменил выражение.
    public Map<Monomial, BigInteger> getTerms() {
        return Collections.unmodifiableMap(terms);
    }

    public int getTermCount() { return terms.size(); }
    public boolean isZero() { return terms.isEmpty(); }

    public int getDegreeX() {
        int max = 0;
        for (Monomial m : terms.keySet()) {
            if (m.degreeX > max) max = m.degreeX;
        }
        return max;
    }

    public int getDegreeY() {
        int max = 0;
        for (Monomial m : terms.keySet()) {
            if (m.degreeY > max) max = m.degreeY;
        }
        return max;
    }

    @Override
    public String toString() {
        if (isZero()) return "0";
        StringBuilder text = new StringBuilder();
        // Просматриваем степени по убыванию для красивого порядка слагаемых.
        for (int i = getDegreeX(); i >= 0; i--) {
            for (int j = getDegreeY(); j >= 0; j--) {
                BigInteger coefficient = getCoefficient(i, j);
                if (coefficient.signum() == 0) continue;

                if (text.length() > 0) {
                    text.append(coefficient.signum() < 0 ? " - " : " + ");
                } else if (coefficient.signum() < 0) {
                    text.append("-");
                }

                BigInteger absolute = coefficient.abs();
                if ((i == 0 && j == 0) || !absolute.equals(BigInteger.ONE)) {
                    text.append(absolute);
                }
                if (i > 0) {
                    text.append("x");
                    if (i > 1) text.append("^").append(i);
                }
                if (j > 0) {
                    text.append("y");
                    if (j > 1) text.append("^").append(j);
                }
            }
        }
        return text.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Polynomial2D)) return false;
        return terms.equals(((Polynomial2D) object).terms);
    }

    @Override
    public int hashCode() { return terms.hashCode(); }
}
