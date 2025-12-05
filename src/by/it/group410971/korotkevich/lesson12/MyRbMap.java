package by.it.group410971.korotkevich.lesson12;

import java.util.*;

public class MyRbMap implements SortedMap<Integer, String> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private static class Node {
        Integer key;
        String value;
        Node left, right;
        boolean color;

        Node(Integer key, String value, boolean color) {
            this.key = key;
            this.value = value;
            this.color = color;
        }
    }

    private Node root;
    private int size;

    public MyRbMap() {
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

        String[] oldValue = new String[1];
        root = put(root, key, value, oldValue);
        root.color = BLACK;
        if (oldValue[0] == null) size++;
        return oldValue[0];
    }

    @Override
    public String remove(Object key) {
        if (key == null) throw new NullPointerException();
        if (!containsKey(key)) return null;

        // Временное сохранение значения перед удалением
        String valueToRemove = get(key);

        if (!isRed(root.left) && !isRed(root.right))
            root.color = RED;

        root = remove(root, (Integer) key);
        if (root != null) root.color = BLACK;
        size--;

        return valueToRemove;
    }

    @Override
    public String get(Object key) {
        if (key == null) throw new NullPointerException();
        Node node = get(root, (Integer) key);
        return node == null ? null : node.value;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) throw new NullPointerException();
        return get(root, (Integer) key) != null;
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
        return min(root).key;
    }

    @Override
    public Integer lastKey() {
        if (root == null) throw new NoSuchElementException();
        return max(root).key;
    }

    @Override
    public SortedMap<Integer, String> headMap(Integer toKey) {
        MyRbMap result = new MyRbMap();
        buildHeadMap(root, toKey, result);
        return result;
    }

    @Override
    public SortedMap<Integer, String> tailMap(Integer fromKey) {
        MyRbMap result = new MyRbMap();
        buildTailMap(root, fromKey, result);
        return result;
    }

    // RB Tree methods
    private boolean isRed(Node node) {
        return node != null && node.color == RED;
    }

    private Node rotateLeft(Node h) {
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    private Node rotateRight(Node h) {
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    private void flipColors(Node h) {
        h.color = !h.color;
        h.left.color = !h.left.color;
        h.right.color = !h.right.color;
    }

    private Node put(Node h, Integer key, String value, String[] oldValue) {
        if (h == null) return new Node(key, value, RED);

        int cmp = key.compareTo(h.key);
        if (cmp < 0) h.left = put(h.left, key, value, oldValue);
        else if (cmp > 0) h.right = put(h.right, key, value, oldValue);
        else {
            oldValue[0] = h.value;
            h.value = value;
            return h;
        }

        if (isRed(h.right) && !isRed(h.left)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);

        return h;
    }

    // Упрощенный remove без возврата значения через массив
    private Node remove(Node h, Integer key) {
        if (key.compareTo(h.key) < 0) {
            if (!isRed(h.left) && !isRed(h.left.left))
                h = moveRedLeft(h);
            h.left = remove(h.left, key);
        } else {
            if (isRed(h.left)) h = rotateRight(h);
            if (key.compareTo(h.key) == 0 && h.right == null) {
                return null;
            }
            if (!isRed(h.right) && !isRed(h.right.left))
                h = moveRedRight(h);
            if (key.compareTo(h.key) == 0) {
                Node min = min(h.right);
                h.key = min.key;
                h.value = min.value;
                h.right = deleteMin(h.right);
            } else {
                h.right = remove(h.right, key);
            }
        }
        return balance(h);
    }

    private Node moveRedLeft(Node h) {
        flipColors(h);
        if (isRed(h.right.left)) {
            h.right = rotateRight(h.right);
            h = rotateLeft(h);
            flipColors(h);
        }
        return h;
    }

    private Node moveRedRight(Node h) {
        flipColors(h);
        if (isRed(h.left.left)) {
            h = rotateRight(h);
            flipColors(h);
        }
        return h;
    }

    private Node balance(Node h) {
        if (isRed(h.right)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);
        return h;
    }

    private Node deleteMin(Node h) {
        if (h.left == null) return null;
        if (!isRed(h.left) && !isRed(h.left.left))
            h = moveRedLeft(h);
        h.left = deleteMin(h.left);
        return balance(h);
    }

    private Node min(Node x) {
        while (x.left != null) x = x.left;
        return x;
    }

    private Node max(Node x) {
        while (x.right != null) x = x.right;
        return x;
    }

    private Node get(Node x, Integer key) {
        while (x != null) {
            int cmp = key.compareTo(x.key);
            if (cmp < 0) x = x.left;
            else if (cmp > 0) x = x.right;
            else return x;
        }
        return null;
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

    private void buildHeadMap(Node node, Integer toKey, MyRbMap result) {
        if (node != null) {
            buildHeadMap(node.left, toKey, result);
            if (node.key < toKey) {
                result.put(node.key, node.value);
            }
            buildHeadMap(node.right, toKey, result);
        }
    }

    private void buildTailMap(Node node, Integer fromKey, MyRbMap result) {
        if (node != null) {
            buildTailMap(node.left, fromKey, result);
            if (node.key >= fromKey) {
                result.put(node.key, node.value);
            }
            buildTailMap(node.right, fromKey, result);
        }
    }

    // Остальные методы остаются без изменений
    @Override public Comparator<? super Integer> comparator() { return null; }

    @Override
    public SortedMap<Integer, String> subMap(Integer fromKey, Integer toKey) {
        MyRbMap result = new MyRbMap();
        buildSubMap(root, fromKey, toKey, result);
        return result;
    }

    private void buildSubMap(Node node, Integer fromKey, Integer toKey, MyRbMap result) {
        if (node != null) {
            buildSubMap(node.left, fromKey, toKey, result);
            if (node.key >= fromKey && node.key < toKey) {
                result.put(node.key, node.value);
            }
            buildSubMap(node.right, fromKey, toKey, result);
        }
    }

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