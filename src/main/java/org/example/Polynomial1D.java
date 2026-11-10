package org.example;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

// Многочлен одной переменной q: например, q^3 - 3q^2 + 2q.
public class Polynomial1D {
    // Ключ — степень q, значение — коэффициент при ней.
    private final Map<Integer, BigInteger> terms = new HashMap<>();

    private Polynomial1D() {}

    private void addTerm(int degree, BigInteger coefficient) {
        if (degree < 0) throw new IllegalArgumentException("Отрицательная степень");
        BigInteger old = terms.getOrDefault(degree, BigInteger.ZERO);
        BigInteger sum = old.add(coefficient);
        if (sum.signum() == 0) {
            terms.remove(degree);
        } else {
            terms.put(degree, sum);
        }
    }

    public static Polynomial1D constant(long number) {
        return constant(BigInteger.valueOf(number));
    }

    public static Polynomial1D constant(BigInteger number) {
        Polynomial1D result = new Polynomial1D();
        result.addTerm(0, number);
        return result;
    }

    public static Polynomial1D q() {
        Polynomial1D result = new Polynomial1D();
        result.addTerm(1, BigInteger.ONE);
        return result;
    }

    public Polynomial1D add(Polynomial1D other) {
        Polynomial1D result = new Polynomial1D();
        for (int degree : terms.keySet()) {
            result.addTerm(degree, terms.get(degree));
        }
        for (int degree : other.terms.keySet()) {
            result.addTerm(degree, other.terms.get(degree));
        }
        return result;
    }

    public Polynomial1D multiply(Polynomial1D other) {
        Polynomial1D result = new Polynomial1D();
        for (int degree1 : terms.keySet()) {
            for (int degree2 : other.terms.keySet()) {
                BigInteger value = terms.get(degree1).multiply(other.terms.get(degree2));
                result.addTerm(degree1 + degree2, value);
            }
        }
        return result;
    }

    public Polynomial1D scale(BigInteger number) {
        Polynomial1D result = new Polynomial1D();
        for (int degree : terms.keySet()) {
            result.addTerm(degree, terms.get(degree).multiply(number));
        }
        return result;
    }

    public Polynomial1D scale(long number) {
        return scale(BigInteger.valueOf(number));
    }

    // Возведение многочлена в целую неотрицательную степень.
    public Polynomial1D pow(int exponent) {
        if (exponent < 0) throw new IllegalArgumentException("Отрицательная степень");
        Polynomial1D result = constant(1);
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(this);
        }
        return result;
    }

    public BigInteger evaluate(BigInteger q) {
        BigInteger answer = BigInteger.ZERO;
        for (int degree : terms.keySet()) {
            BigInteger term = terms.get(degree).multiply(q.pow(degree));
            answer = answer.add(term);
        }
        return answer;
    }

    public BigInteger evaluate(long q) {
        return evaluate(BigInteger.valueOf(q));
    }

    public BigInteger getCoefficient(int degree) {
        if (degree < 0) throw new IllegalArgumentException("Отрицательная степень");
        return terms.getOrDefault(degree, BigInteger.ZERO);
    }

    public boolean isZero() { return terms.isEmpty(); }

    public int getDegree() {
        int max = 0;
        for (int degree : terms.keySet()) {
            if (degree > max) max = degree;
        }
        return max;
    }

    @Override
    public String toString() {
        if (isZero()) return "0";
        StringBuilder text = new StringBuilder();
        for (int degree = getDegree(); degree >= 0; degree--) {
            BigInteger coefficient = getCoefficient(degree);
            if (coefficient.signum() == 0) continue;

            if (text.length() > 0) {
                text.append(coefficient.signum() < 0 ? " - " : " + ");
            } else if (coefficient.signum() < 0) {
                text.append("-");
            }

            BigInteger absolute = coefficient.abs();
            if (degree == 0 || !absolute.equals(BigInteger.ONE)) {
                text.append(absolute);
            }
            if (degree > 0) {
                text.append("q");
                if (degree > 1) text.append("^").append(degree);
            }
        }
        return text.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Polynomial1D)) return false;
        return terms.equals(((Polynomial1D) object).terms);
    }

    @Override
    public int hashCode() { return terms.hashCode(); }
}
 