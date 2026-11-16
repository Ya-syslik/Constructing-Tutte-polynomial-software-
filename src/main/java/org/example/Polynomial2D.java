package org.example;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Polynomial2D {

    /**
     * Monomial (моном) — один вид слагаемого БЕЗ коэффициента.
    * Класс вложен в Polynomial2D, поскольку отдельно от него почти не нужен.
    * Вложенный класс реализует неизменяемый ключ хеш-таблицы.
    * Учитываются только показатели степеней; коэффициент хранится в значении Map.
    */

    public static class Monomial {
        private final int degreeX;
        private final int degreeY;

        // Конструктор получает две степени одного слагаемого.
        public Monomial(int degreeX, int degreeY) {
            if (degreeX < 0 || degreeY < 0) {
                throw new IllegalArgumentException("Степени должны быть неотрицательными");
            }
            this.degreeX = degreeX;
            this.degreeY = degreeY;
        }

        public int getDegreeX() { return degreeX; }
        public int getDegreeY() { return degreeY; }

        /**
         * Если два объекта Monomial имеют степени (2,1) и (2,1),
         * то для HashMap это один и тот же вид слагаемого x^2*y.
         * Поэтому переопределяем equals() и hashCode() вместе.
         */

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Monomial)) return false;

            // Downcasting: приводим проверенный Object к Monomial; новый объект
            // при таком приведении НЕ создаётся — копируется ссылка.

            Monomial other = (Monomial) object;
            return degreeX == other.degreeX && degreeY == other.degreeY;
        }

        @Override
        public int hashCode() {
            // У равных мономов должен быть одинаковый хеш-код.
            // Коллизии допустимы: после хеширования HashMap проверяет equals().
            return 31 * degreeX + degreeY;
        }
    }

    /**
     * Главная структура данных:
     *   ключ Monomial(2,1) -> коэффициент 3  означает 3*x^2*y;
     *   ключ Monomial(0,0) -> коэффициент 5  означает свободный член 5.
     * HashMap быстро находит коэффициент по паре степеней.
     * Нулевые коэффициенты не храним.
     */

    // Поле объявлено через интерфейс Map, реализация — HashMap.
    private final Map<Monomial, BigInteger> terms = new HashMap<>();

    private Polynomial2D() {}

    private void addTerm(int degreeX, int degreeY, BigInteger coefficient) {
        Monomial key = new Monomial(degreeX, degreeY);

        BigInteger previous = terms.getOrDefault(key, BigInteger.ZERO); // Если слагаемое ещё не встречалось, используем нулевой коэффициент.
        BigInteger sum = previous.add(coefficient);

        //Удаление результата сложения если получился 0
        if (sum.signum() == 0) {
            terms.remove(key);
        } else {
            terms.put(key, sum);
        }
    }

    // Создаёт многочлен-константу.
    public static Polynomial2D constant(long number) {
        return monomial(0, 0, BigInteger.valueOf(number));
    }

    // Та же операция, когда число уже имеет тип BigInteger.
    public static Polynomial2D constant(BigInteger number) {
        return monomial(0, 0, number);
    }

    // x имеет коэффициент 1 и степени x^1*y^0.
    public static Polynomial2D x() {
        return monomial(1, 0, BigInteger.ONE);
    }

    // y имеет коэффициент 1 и степени x^0*y^1.
    public static Polynomial2D y() {
        return monomial(0, 1, BigInteger.ONE);
    }

    // Статический фабричный метод: возвращает Polynomial2D из одного члена.
    public static Polynomial2D monomial(int degreeX, int degreeY, BigInteger coefficient) {
        Polynomial2D result = new Polynomial2D();
        result.addTerm(degreeX, degreeY, coefficient);
        return result;
    }

    //Сложение двух многочленов
    public Polynomial2D add(Polynomial2D other) {
        Polynomial2D result = new Polynomial2D();

        // Переносим в результат все слагаемые первого многочлена.
        // Map.Entry — одна пара «ключ-значение»; entrySet() позволяет
        // пройти по ВСЕМ ненулевым слагаемым, а не только по двум членам.

        for (Map.Entry<Monomial, BigInteger> entry : terms.entrySet()) {
            Monomial m = entry.getKey();    // Пара степеней.
            BigInteger c = entry.getValue(); // Коэффициент.
            result.addTerm(m.degreeX, m.degreeY, c);
        }

        // Добавляем слагаемые второго многочлена.
        // addTerm() объединит слагаемые с одинаковыми степенями.

        for (Map.Entry<Monomial, BigInteger> entry : other.terms.entrySet()) {
            Monomial m = entry.getKey();
            BigInteger c = entry.getValue();
            result.addTerm(m.degreeX, m.degreeY, c);
        }

        return result;
    }

    //Умножение по правилу раскрытия скобок.
    public Polynomial2D multiply(Polynomial2D other) {
        Polynomial2D result = new Polynomial2D();

        // Вложенные циклы образуют декартово произведение наборов слагаемых.
        // При a и b членах выполняется a*b попарных умножений.

        for (Map.Entry<Monomial, BigInteger> first : terms.entrySet()) {
            for (Map.Entry<Monomial, BigInteger> second : other.terms.entrySet()) {
                // x^a * x^b = x^(a+b); аналогично для y.
                int xDegree = first.getKey().degreeX + second.getKey().degreeX;
                int yDegree = first.getKey().degreeY + second.getKey().degreeY;

                // Числовые коэффициенты перемножаются.
                BigInteger value = first.getValue().multiply(second.getValue());

                // Совпадающие мономы складываются автоматически.
                result.addTerm(xDegree, yDegree, value);
            }
        }
        return result;
    }

    // Обёртка для мостов T_G(x,y)=x*T_{G/e}(x,y).
    public Polynomial2D multiplyByX() { return multiply(x()); }
    // Обёртка для петель T_G(x,y)=y*T_{G-e}(x,y).
    public Polynomial2D multiplyByY() { return multiply(y()); }

    //Подставляет конкретные значения x и y.
    public BigInteger evaluate(BigInteger x, BigInteger y) {
        BigInteger answer = BigInteger.ZERO;

        // Обходим все ненулевые члены; каждый Map.Entry содержит пару
        // «моном — коэффициент», независимо от общего числа слагаемых.

        for (Map.Entry<Monomial, BigInteger> entry : terms.entrySet()) {
            Monomial m = entry.getKey();
            BigInteger term = entry.getValue() // Вычисляем одно слагаемое c*x^i*y^j.
                    .multiply(x.pow(m.degreeX))
                    .multiply(y.pow(m.degreeY));
            answer = answer.add(term); // Прибавляем его к общей сумме.
        }
        return answer;
    }

    // Удобная перегрузка для обычных целых чисел.
    public BigInteger evaluate(long x, long y) {
        return evaluate(BigInteger.valueOf(x), BigInteger.valueOf(y));
    }

    //Получает коэффициент при x^degreeX * y^degreeY (или 0).
    public BigInteger getCoefficient(int degreeX, int degreeY) {
        return terms.getOrDefault(new Monomial(degreeX, degreeY), BigInteger.ZERO);
    }

    // Возвращает словарь для чтения: анализатор сможет изучать слагаемые
    public Map<Monomial, BigInteger> getTerms() {
        return Collections.unmodifiableMap(terms);
    }

    // Количество слагаемых с ненулевым коэффициентом.
    public int getTermCount() { return terms.size(); }

    // Нулевой многочлен хранится как пустая карта.
    public boolean isZero() { return terms.isEmpty(); }

    // Наибольшая степень переменной x среди всех слагаемых.
    public int getDegreeX() {
        int max = 0;
        for (Monomial m : terms.keySet()) {
            if (m.degreeX > max) max = m.degreeX;
        }
        return max;
    }

    // То же самое для переменной y.
    public int getDegreeY() {
        int max = 0;
        for (Monomial m : terms.keySet()) {
            if (m.degreeY > max) max = m.degreeY;
        }
        return max;
    }

    /**
     * Превращает объект в строку вида "x^2 + x + y".
     * Java сама вызывает toString(), когда мы пишем:
     * System.out.println("T(x,y) = " + polynomial);
     */

    @Override
    public String toString() {
        if (isZero()) return "0";
        StringBuilder text = new StringBuilder();

        // Перебираем степени от больших к маленьким,
        // чтобы старшие степени в выводе шли первыми.

        for (int i = getDegreeX(); i >= 0; i--) {
            for (int j = getDegreeY(); j >= 0; j--) {
                BigInteger coefficient = getCoefficient(i, j);
                if (coefficient.signum() == 0) continue;

                // Учитываем знак коэффициента: плюс или минус.
                if (text.length() > 0) {
                    text.append(coefficient.signum() < 0 ? " - " : " + ");
                } else if (coefficient.signum() < 0) {
                    text.append("-");
                }

                // abs() убирает минус: его мы уже вывели отдельно.
                BigInteger absolute = coefficient.abs();

                if ((i == 0 && j == 0) || !absolute.equals(BigInteger.ONE)) {
                    text.append(absolute);
                }

                // Добавляем x и y вместе с их степенями, если они ненулевые.
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

    // Два многочлена считаются равными, если их словари слагаемых равны.
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Polynomial2D)) return false;
        return terms.equals(((Polynomial2D) object).terms); // После проверки типа сравниваем содержание Map, а не адреса объектов.
    }

    // Переопределяется вместе с equals(): требование Java для HashMap/HashSet.
    @Override
    // Равные по equals() многочлены имеют равный хеш-код.
    public int hashCode() { return terms.hashCode(); }
}
