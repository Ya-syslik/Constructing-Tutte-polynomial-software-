package org.example;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Polynomial1D {

    // Один ключ Integer = степень q; значение BigInteger = коэффициент a_i.
    private final Map<Integer, BigInteger> terms = new HashMap<>();

    private Polynomial1D() {}

    /**
     * Добавляет одно слагаемое coefficient * q^degree.
     * Если степень уже есть, складывает коэффициенты.
     * Если сумма равна нулю, удаляет слагаемое.
     */

    private void addTerm(int degree, BigInteger coefficient) {
        if (degree < 0) throw new IllegalArgumentException("Отрицательная степень");

        BigInteger old = terms.getOrDefault(degree, BigInteger.ZERO); // Получаем коэффициент при q^degree, либо 0 для отсутствующей степени.
        BigInteger sum = old.add(coefficient); // Складываем коэффициенты подобных членов.
        if (sum.signum() == 0) {
            terms.remove(degree);
        } else {
            // put() обновляет значение по ключу; сам не складывает числа.
            terms.put(degree, sum);
        }
    }

    // Создание постоянного многочлена: constant(5) -> 5.
    public static Polynomial1D constant(long number) {
        return constant(BigInteger.valueOf(number));
    }

    // То же, если число уже записано как BigInteger.
    public static Polynomial1D constant(BigInteger number) {
        Polynomial1D result = new Polynomial1D(); // Создаём независимый объект результата: исходный многочлен не меняем.
        result.addTerm(0, number); // Константа = число * q^0.
        return result;
    }

    // Создание переменной q: коэффициент 1, степень 1.
    public static Polynomial1D q() {
        Polynomial1D result = new Polynomial1D(); // Создаём независимый объект результата: исходный многочлен не меняем.
        result.addTerm(1, BigInteger.ONE);
        return result;
    }
    //Сложение двух многочленов
    public Polynomial1D add(Polynomial1D other) {
        Polynomial1D result = new Polynomial1D(); // Создаём независимый объект результата: исходный многочлен не меняем.

        // Все члены первого многочлена.
        // keySet() возвращает множество существующих степеней.
        // Цикл обходит любое число членов разреженного многочлена.

        for (int degree : terms.keySet()) {
            result.addTerm(degree, terms.get(degree));
        }

        // Все члены второго многочлена.
        for (int degree : other.terms.keySet()) {
            result.addTerm(degree, other.terms.get(degree));
        }

        return result;
    }
    public Polynomial1D multiply(Polynomial1D other) {
        Polynomial1D result = new Polynomial1D();

        // При умножении перебираем все пары членов обоих множителей.
        for (int degree1 : terms.keySet()) {
            for (int degree2 : other.terms.keySet()) {
                BigInteger value = terms.get(degree1).multiply(other.terms.get(degree2));
                result.addTerm(degree1 + degree2, value); // Степени q складываем, результат прибавляем к нужному члену.
            }
        }
        return result;
    }

    // Умножение на скаляр изменяет коэффициенты, но не степени q.
    public Polynomial1D scale(BigInteger number) {
        Polynomial1D result = new Polynomial1D();

        // keySet() возвращает множество существующих степеней.
        // Цикл обходит любое число членов разреженного многочлена.

        for (int degree : terms.keySet()) {
            result.addTerm(degree, terms.get(degree).multiply(number));
        }
        return result;
    }

    // Перегрузка метода для обычного целого числа.
    public Polynomial1D scale(long number) {
        return scale(BigInteger.valueOf(number));
    }

    //Возведение всего многочлена в степень.
    public Polynomial1D pow(int exponent) {
        if (exponent < 0) throw new IllegalArgumentException("Отрицательная степень");
        Polynomial1D result = constant(1); // Начинаем с 1, т.к. A^0 = 1.
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(this); // this — ссылка на объект-основание; каждый проход добавляет множитель.
        }
        return result;
    }

    // Вычисление значения при указанном q, например P(3).
    public BigInteger evaluate(BigInteger q) {
        BigInteger answer = BigInteger.ZERO;

        // keySet() возвращает множество существующих степеней.
        // Цикл обходит любое число членов разреженного многочлена.

        for (int degree : terms.keySet()) {
            // Оцениваем один член a_i*q^i; pow() принимает степень типа int.
            BigInteger term = terms.get(degree).multiply(q.pow(degree));
            answer = answer.add(term);
        }
        return answer;
    }

    // Перегрузка evaluate(): можно вызвать метод с обычным long.
    public BigInteger evaluate(long q) {
        return evaluate(BigInteger.valueOf(q)); // Перегрузка метода с long приводит аргумент к BigInteger.
    }

    // Возвращает коэффициент при q^degree (или ноль, если его нет).
    public BigInteger getCoefficient(int degree) {
        if (degree < 0) throw new IllegalArgumentException("Отрицательная степень");
        return terms.getOrDefault(degree, BigInteger.ZERO);
    }

    // Истина, если после сокращений слагаемых не осталось.
    // Семантический предикат isZero() использует инвариант «нет нулевых членов».
    public boolean isZero() {
        return terms.isEmpty(); }

    // Наибольшая степень переменной q.
    public int getDegree() {
        int max = 0;
        for (int degree : terms.keySet()) {
            if (degree > max) max = degree;
        }
        return max;
    }

    //Форматирование: из Map получается строка, например "q^3 - 3q^2 + 2q".
    //Пропускает коэффициенты 0 и единицы перед переменной.

    @Override
    public String toString() {
        if (isZero()) return "0";

        // Составляем математическую запись через изменяемый StringBuilder.
        StringBuilder text = new StringBuilder();
        for (int degree = getDegree(); degree >= 0; degree--) {
            BigInteger coefficient = getCoefficient(degree);
            if (coefficient.signum() == 0) continue; // Слагаемого нет.

            // Ставим нужный знак перед слагаемым.
            if (text.length() > 0) {
                text.append(coefficient.signum() < 0 ? " - " : " + ");
            } else if (coefficient.signum() < 0) {
                text.append("-");
            }

            BigInteger absolute = coefficient.abs();
            // Не выводим 1 перед q, но выводим свободный член и модуль коэффициента > 1.
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

    // Равенство многочленов проверяем по всем коэффициентам и степеням.
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Polynomial1D)) return false;
        return terms.equals(((Polynomial1D) object).terms); // Объекты сравниваются по набору степеней и коэффициентов.
    }

    // hashCode() должен быть согласован с equals().
    @Override
    // Соблюдаем контракт Object: a.equals(b) => одинаковые hashCode().
    public int hashCode() { return terms.hashCode(); }
}
