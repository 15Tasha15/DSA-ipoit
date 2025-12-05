package by.it.group410971.korotkevich.lesson13;

import java.util.*;

public class GraphB {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        // Парсинг входной строки
        Map<String, List<String>> graph = new HashMap<>();
        String[] edges = input.split(",\\s*");

        for (String edge : edges) {
            String[] parts = edge.split("\\s*->\\s*");
            String from = parts[0].trim();
            String to = parts[1].trim();

            graph.putIfAbsent(from, new ArrayList<>());
            graph.putIfAbsent(to, new ArrayList<>());
            graph.get(from).add(to);
        }

        // Проверка на наличие циклов
        boolean hasCycle = hasCycle(graph);

        // Вывод результата
        System.out.println(hasCycle ? "yes" : "no");
    }

    private static boolean hasCycle(Map<String, List<String>> graph) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        // Проверяем все вершины графа
        for (String vertex : graph.keySet()) {
            if (!visited.contains(vertex)) {
                if (dfs(vertex, graph, visited, recursionStack)) {
                    return true; // Найден цикл
                }
            }
        }

        return false; // Циклов нет
    }

    private static boolean dfs(String vertex, Map<String, List<String>> graph,
                               Set<String> visited, Set<String> recursionStack) {
        // Если вершина уже в текущем стеке рекурсии - найден цикл
        if (recursionStack.contains(vertex)) {
            return true;
        }

        // Если вершина уже полностью обработана - пропускаем
        if (visited.contains(vertex)) {
            return false;
        }

        // Добавляем вершину в посещенные и в стек рекурсии
        visited.add(vertex);
        recursionStack.add(vertex);

        // Рекурсивно проверяем всех соседей
        for (String neighbor : graph.get(vertex)) {
            if (dfs(neighbor, graph, visited, recursionStack)) {
                return true;
            }
        }

        // Убираем вершину из стека рекурсии (завершаем обработку)
        recursionStack.remove(vertex);
        return false;
    }
}