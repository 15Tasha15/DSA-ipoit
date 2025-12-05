package by.it.group410971.korotkevich.lesson13;

import java.util.*;

public class GraphA {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        // Парсинг входной строки
        Map<String, List<String>> graph = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        String[] edges = input.split(",\\s*");

        for (String edge : edges) {
            String[] parts = edge.split("\\s*->\\s*");
            String from = parts[0].trim();
            String to = parts[1].trim();

            graph.putIfAbsent(from, new ArrayList<>());
            graph.putIfAbsent(to, new ArrayList<>());
            graph.get(from).add(to);

            inDegree.putIfAbsent(from, 0);
            inDegree.put(to, inDegree.getOrDefault(to, 0) + 1);
        }

        // Топологическая сортировка алгоритмом Кана
        List<String> result = kahnsTopologicalSort(graph, inDegree);

        // Вывод результата с пробелами
        for (int i = 0; i < result.size(); i++) {
            System.out.print(result.get(i));
            if (i < result.size() - 1) {
                System.out.print(" ");
            }
        }
    }

    private static List<String> kahnsTopologicalSort(Map<String, List<String>> graph,
                                                     Map<String, Integer> inDegree) {
        List<String> result = new ArrayList<>();

        // Используем TreeSet для автоматической сортировки
        TreeSet<String> queue = new TreeSet<>((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (NumberFormatException e) {
                return a.compareTo(b);
            }
        });

        // Добавляем вершины с нулевой входящей степенью
        for (String vertex : graph.keySet()) {
            if (inDegree.getOrDefault(vertex, 0) == 0) {
                queue.add(vertex);
            }
        }

        while (!queue.isEmpty()) {
            // Берем первую вершину из отсортированного множества
            String vertex = queue.pollFirst();
            result.add(vertex);

            // Обрабатываем всех соседей
            for (String neighbor : graph.get(vertex)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // Проверка на циклы
        if (result.size() != graph.size()) {
            throw new RuntimeException("Graph has cycles");
        }

        return result;
    }
}