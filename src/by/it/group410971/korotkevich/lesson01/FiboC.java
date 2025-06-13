package by.it.group410971.korotkevich.lesson01;

/*
 * Даны целые числа 1<=n<=1E18 и 2<=m<=1E5,
 * необходимо найти остаток от деления n-го числа Фибоначчи на m
 * время расчета должно быть не более 2 секунд
 */

public class FiboC {

    private long startTime = System.currentTimeMillis();

    public static void main(String[] args) {
        FiboC fibo = new FiboC();
        int n = 55555;
        int m = 1000;
        System.out.printf("fasterC(%d)=%d \n\t time=%d \n\n", n, fibo.fasterC(n, m), fibo.time());
    }

    private long time() {
        return System.currentTimeMillis() - startTime;
    }

    long fasterC(long n, int m) {
        int pisanoPeriod = pisano(m); // Период Пизано
        n = n % pisanoPeriod; // cокращаем n по модулю периода
        if (n == 0) {
            return 0;
        }
        long[] fib = new long[(int)n + 1]; //число Фибоначчи по модулю m
        fib[0] = 0;
        fib[1] = 1;
        for (int i = 2; i <= n; i++) {
            fib[i] = (fib[i - 1] + fib[i - 2]) % m;
        }

        return fib[(int)n];
    }

    // Находит период Пизано для m
    private int pisano(int m) {
        int prev = 0;
        int curr = 1;
        for (int i = 0; i < m * m; i++) {
            int next = (prev + curr) % m;
            if (next == 1 && curr == 0) {
                return i + 1;
            }
            prev = curr;
            curr = next;
        }
        return 0; // Если период не найден
    }
}

