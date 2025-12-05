package by.it.group410971.korotkevich.lesson12;

import java.util.*;

public class MySplayMap implements NavigableMap<Integer, String> {

    private static class Node {
        Integer key;
        String value;
        Node left, right, parent;

        Node(Integer key, String value, Node parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }
    }

    private Node root;
    private int size;

    public MySplayMap() {
        root = null;
        size = 0;
    }

    @Override
    public String toString() {
        if (size == 0) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        inOrderTraversal(root, sb);
        sb.setLength(sb.length() - 2);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String put(Integer key, String value) {
        if (key == null) throw new NullPointerException();

        Node node = findNode(key);
        if (node != null && key.equals(node.key)) {
            String oldValue = node.value;
            node.value = value;
            splay(node);
            return oldValue;
        }

        Node newNode = new Node(key, value, null);
        if (root == null) {
            root = newNode;
        } else {
            if (key.compareTo(node.key) < 0) {
                node.left = newNode;
                newNode.parent = node;
            } else {
                node.right = newNode;
                newNode.parent = node;
            }
            splay(newNode);
        }
        size++;
        return null;
    }

    @Override
    public String remove(Object key) {
        if (key == null) throw new NullPointerException();

        Node node = findNode((Integer) key);
        if (node == null || !key.equals(node.key)) return null;

        splay(node);
        String removedValue = node.value;

        if (node.left == null) {
            root = node.right;
            if (root != null) root.parent = null;
        } else if (node.right == null) {
            root = node.left;
            if (root != null) root.parent = null;
        } else {
            Node min = minNode(node.right);
            if (min.parent != node) {
                min.parent.left = min.right;
                if (min.right != null) min.right.parent = min.parent;
                min.right = node.right;
                min.right.parent = min;
            }
            min.left = node.left;
            min.left.parent = min;
            root = min;
            root.parent = null;
        }
        size--;
        return removedValue;
    }

    @Override
    public String get(Object key) {
        if (key == null) throw new NullPointerException();
        Node node = findNode((Integer) key);
        if (node != null && key.equals(node.key)) {
            splay(node);
            return node.value;
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) throw new NullPointerException();
        Node node = findNode((Integer) key);
        boolean contains = node != null && key.equals(node.key);
        if (contains) splay(node);
        return contains;
    }

    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof String)) return false;
        return containsValue(root, (String) value);
    }

    @Override
    public int size() { return size; }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean isEmpty() { return size == 0; }

    @Override
    public Integer firstKey() {
        if (root == null) throw new NoSuchElementException();
        Node min = minNode(root);
        splay(min);
        return min.key;
    }

    @Override
    public Integer lastKey() {
        if (root == null) throw new NoSuchElementException();
        Node max = maxNode(root);
        splay(max);
        return max.key;
    }

    @Override
    public Integer lowerKey(Integer key) {
        if (root == null) return null;
        Node node = findNode(key);
        splay(node);

        if (node.key.compareTo(key) < 0) {
            return node.key;
        }

        // Ищем наибольший ключ меньше than key
        if (node.left != null) {
            Node maxLeft = maxNode(node.left);
            splay(maxLeft);
            return maxLeft.key;
        }
        return null;
    }

    @Override
    public Integer floorKey(Integer key) {
        if (root == null) return null;
        Node node = findNode(key);
        splay(node);

        if (node.key.compareTo(key) <= 0) {
            return node.key;
        }

        // Ищем наибольший ключ <= key
        if (node.left != null) {
            Node maxLeft = maxNode(node.left);
            splay(maxLeft);
            return maxLeft.key;
        }
        return null;
    }

    @Override
    public Integer ceilingKey(Integer key) {
        if (root == null) return null;
        Node node = findNode(key);
        splay(node);

        if (node.key.compareTo(key) >= 0) {
            return node.key;
        }

        // Ищем наименьший ключ >= key
        if (node.right != null) {
            Node minRight = minNode(node.right);
            splay(minRight);
            return minRight.key;
        }
        return null;
    }

    @Override
    public Integer higherKey(Integer key) {
        if (root == null) return null;
        Node node = findNode(key);
        splay(node);

        if (node.key.compareTo(key) > 0) {
            return node.key;
        }

        // Ищем наименьший ключ > key
        if (node.right != null) {
            Node minRight = minNode(node.right);
            splay(minRight);
            return minRight.key;
        }
        return null;
    }

    @Override
    public NavigableMap<Integer, String> headMap(Integer toKey) {
        return headMap(toKey, false);
    }

    @Override
    public NavigableMap<Integer, String> tailMap(Integer fromKey) {
        return tailMap(fromKey, true);
    }

    // Splay tree methods
    private void splay(Node x) {
        while (x.parent != null) {
            Node parent = x.parent;
            Node grandparent = parent.parent;

            if (grandparent == null) {
                if (x == parent.left) rotateRight(parent);
                else rotateLeft(parent);
            } else {
                if (x == parent.left && parent == grandparent.left) {
                    rotateRight(grandparent);
                    rotateRight(parent);
                } else if (x == parent.right && parent == grandparent.right) {
                    rotateLeft(grandparent);
                    rotateLeft(parent);
                } else if (x == parent.right && parent == grandparent.left) {
                    rotateLeft(parent);
                    rotateRight(grandparent);
                } else {
                    rotateRight(parent);
                    rotateLeft(grandparent);
                }
            }
        }
        root = x;
    }

    private void rotateLeft(Node x) {
        Node y = x.right;
        if (y != null) {
            x.right = y.left;
            if (y.left != null) y.left.parent = x;
            y.parent = x.parent;
        }
        if (x.parent == null) root = y;
        else if (x == x.parent.left) x.parent.left = y;
        else x.parent.right = y;
        if (y != null) y.left = x;
        x.parent = y;
    }

    private void rotateRight(Node x) {
        Node y = x.left;
        if (y != null) {
            x.left = y.right;
            if (y.right != null) y.right.parent = x;
            y.parent = x.parent;
        }
        if (x.parent == null) root = y;
        else if (x == x.parent.right) x.parent.right = y;
        else x.parent.left = y;
        if (y != null) y.right = x;
        x.parent = y;
    }

    private Node findNode(Integer key) {
        Node current = root;
        Node last = root;
        while (current != null) {
            last = current;
            int cmp = key.compareTo(current.key);
            if (cmp == 0) return current;
            else if (cmp < 0) current = current.left;
            else current = current.right;
        }
        return last;
    }

    private Node minNode(Node node) {
        while (node.left != null) node = node.left;
        return node;
    }

    private Node maxNode(Node node) {
        while (node.right != null) node = node.right;
        return node;
    }

    private boolean containsValue(Node node, String value) {
        if (node == null) return false;
        if (value == null ? node.value == null : value.equals(node.value)) return true;
        return containsValue(node.left, value) || containsValue(node.right, value);
    }

    private void inOrderTraversal(Node node, StringBuilder sb) {
        if (node != null) {
            inOrderTraversal(node.left, sb);
            sb.append(node.key).append("=").append(node.value).append(", ");
            inOrderTraversal(node.right, sb);
        }
    }

    // Остальные методы остаются без изменений
    @Override
    public Entry<Integer, String> lowerEntry(Integer key) {
        Integer lower = lowerKey(key);
        return lower == null ? null : new SimpleEntry(lower, get(lower));
    }

    @Override
    public Entry<Integer, String> floorEntry(Integer key) {
        Integer floor = floorKey(key);
        return floor == null ? null : new SimpleEntry(floor, get(floor));
    }

    @Override
    public Entry<Integer, String> ceilingEntry(Integer key) {
        Integer ceiling = ceilingKey(key);
        return ceiling == null ? null : new SimpleEntry(ceiling, get(ceiling));
    }

    @Override
    public Entry<Integer, String> higherEntry(Integer key) {
        Integer higher = higherKey(key);
        return higher == null ? null : new SimpleEntry(higher, get(higher));
    }

    @Override
    public Entry<Integer, String> firstEntry() {
        return new SimpleEntry(firstKey(), get(firstKey()));
    }

    @Override
    public Entry<Integer, String> lastEntry() {
        return new SimpleEntry(lastKey(), get(lastKey()));
    }

    @Override
    public Entry<Integer, String> pollFirstEntry() {
        Integer first = firstKey();
        String value = remove(first);
        return new SimpleEntry(first, value);
    }

    @Override
    public Entry<Integer, String> pollLastEntry() {
        Integer last = lastKey();
        String value = remove(last);
        return new SimpleEntry(last, value);
    }

    @Override
    public NavigableMap<Integer, String> headMap(Integer toKey, boolean inclusive) {
        MySplayMap result = new MySplayMap();
        buildHeadMap(root, toKey, inclusive, result);
        return result;
    }

    @Override
    public NavigableMap<Integer, String> tailMap(Integer fromKey, boolean inclusive) {
        MySplayMap result = new MySplayMap();
        buildTailMap(root, fromKey, inclusive, result);
        return result;
    }

    private void buildHeadMap(Node node, Integer toKey, boolean inclusive, MySplayMap result) {
        if (node != null) {
            buildHeadMap(node.left, toKey, inclusive, result);
            if ((inclusive && node.key <= toKey) || (!inclusive && node.key < toKey)) {
                result.put(node.key, node.value);
            }
            buildHeadMap(node.right, toKey, inclusive, result);
        }
    }

    private void buildTailMap(Node node, Integer fromKey, boolean inclusive, MySplayMap result) {
        if (node != null) {
            buildTailMap(node.left, fromKey, inclusive, result);
            if ((inclusive && node.key >= fromKey) || (!inclusive && node.key > fromKey)) {
                result.put(node.key, node.value);
            }
            buildTailMap(node.right, fromKey, inclusive, result);
        }
    }

    @Override public Comparator<? super Integer> comparator() { return null; }

    @Override
    public NavigableMap<Integer, String> subMap(Integer fromKey, Integer toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    @Override
    public NavigableMap<Integer, String> subMap(Integer fromKey, boolean fromInclusive, Integer toKey, boolean toInclusive) {
        MySplayMap result = new MySplayMap();
        buildSubMap(root, fromKey, fromInclusive, toKey, toInclusive, result);
        return result;
    }

    private void buildSubMap(Node node, Integer fromKey, boolean fromInclusive, Integer toKey, boolean toInclusive, MySplayMap result) {
        if (node != null) {
            buildSubMap(node.left, fromKey, fromInclusive, toKey, toInclusive, result);
            boolean lowerBound = fromInclusive ? node.key >= fromKey : node.key > fromKey;
            boolean upperBound = toInclusive ? node.key <= toKey : node.key < toKey;
            if (lowerBound && upperBound) {
                result.put(node.key, node.value);
            }
            buildSubMap(node.right, fromKey, fromInclusive, toKey, toInclusive, result);
        }
    }

    @Override public NavigableMap<Integer, String> descendingMap() { throw new UnsupportedOperationException(); }
    @Override public NavigableSet<Integer> navigableKeySet() { throw new UnsupportedOperationException(); }
    @Override public NavigableSet<Integer> descendingKeySet() { throw new UnsupportedOperationException(); }

    @Override
    public void putAll(Map<? extends Integer, ? extends String> m) {
        for (Entry<? extends Integer, ? extends String> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Set<Integer> keySet() {
        Set<Integer> keys = new HashSet<Integer>();
        addKeys(root, keys);
        return keys;
    }

    private void addKeys(Node node, Set<Integer> keys) {
        if (node != null) {
            addKeys(node.left, keys);
            keys.add(node.key);
            addKeys(node.right, keys);
        }
    }

    @Override
    public Collection<String> values() {
        Collection<String> values = new ArrayList<String>();
        addValues(root, values);
        return values;
    }

    private void addValues(Node node, Collection<String> values) {
        if (node != null) {
            addValues(node.left, values);
            values.add(node.value);
            addValues(node.right, values);
        }
    }

    @Override
    public Set<Entry<Integer, String>> entrySet() {
        Set<Entry<Integer, String>> entries = new HashSet<Entry<Integer, String>>();
        addEntries(root, entries);
        return entries;
    }

    private void addEntries(Node node, Set<Entry<Integer, String>> entries) {
        if (node != null) {
            addEntries(node.left, entries);
            entries.add(new SimpleEntry(node.key, node.value));
            addEntries(node.right, entries);
        }
    }

    private static class SimpleEntry implements Entry<Integer, String> {
        private final Integer key;
        private String value;

        SimpleEntry(Integer key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override public Integer getKey() { return key; }
        @Override public String getValue() { return value; }
        @Override public String setValue(String value) {
            String old = this.value;
            this.value = value;
            return old;
        }
    }
}