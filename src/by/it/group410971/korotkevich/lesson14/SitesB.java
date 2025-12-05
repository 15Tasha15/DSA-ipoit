package by.it.group410971.korotkevich.lesson14;

import java.util.*;

public class SitesB {

    static class DSU {
        private int[] parent;
        private int[] size;

        public DSU(int n) {
            parent = new int[n];
            size = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }

        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        public void union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);

            if (rootX == rootY) return;

            if (size[rootX] < size[rootY]) {
                parent[rootX] = rootY;
                size[rootY] += size[rootX];
            } else {
                parent[rootY] = rootX;
                size[rootX] += size[rootY];
            }
        }

        public int getSize(int x) {
            return size[find(x)];
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Map<String, Integer> siteToIndex = new HashMap<>();
        List<String[]> pairs = new ArrayList<>();

        while (true) {
            String line = scanner.nextLine().trim();
            if (line.equals("end")) {
                break;
            }

            String[] sites = line.split("\\+");
            if (sites.length != 2) {
                continue;
            }

            String site1 = sites[0].trim();
            String site2 = sites[1].trim();

            siteToIndex.putIfAbsent(site1, siteToIndex.size());
            siteToIndex.putIfAbsent(site2, siteToIndex.size());

            pairs.add(new String[]{site1, site2});
        }

        int n = siteToIndex.size();
        DSU dsu = new DSU(n);

        for (String[] pair : pairs) {
            int index1 = siteToIndex.get(pair[0]);
            int index2 = siteToIndex.get(pair[1]);
            dsu.union(index1, index2);
        }

        Map<Integer, Integer> clusterSizes = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = dsu.find(i);
            clusterSizes.put(root, dsu.getSize(i));
        }

        List<Integer> sizes = new ArrayList<>(clusterSizes.values());
        // Сортировка в порядке убывания
        Collections.sort(sizes, Collections.reverseOrder());

        for (int i = 0; i < sizes.size(); i++) {
            System.out.print(sizes.get(i));
            if (i < sizes.size() - 1) {
                System.out.print(" ");
            }
        }
    }
}