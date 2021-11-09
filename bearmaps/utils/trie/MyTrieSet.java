package bearmaps.utils.trie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MyTrieSet {

    Node root;

    public MyTrieSet() {
        root = new Node('0', false);
    }

    /** Clears all items out of Trie */
    public void clear() {
        root = new Node('0', false);
    }

    /** Returns true if the Trie contains KEY, false otherwise */
    public boolean contains(String key) {
        if (key == null || key.length() < 1) {
            return false;
        }
        Node curr = root;
        for (int i = 0, n = key.length(); i < n; i++) {
            char c = key.charAt(i);
            if (!curr.map.containsKey(c)) {
                return false;
            }
            curr = curr.map.get(c);
        }
        return curr.isKey;
    }


    public void add(String key) {
        if (key == null || key.length() < 1) {
            return;
        }
        Node curr = root;
        for (int i = 0, n = key.length(); i < n; i++) {
            char c = key.charAt(i);
            if (!curr.map.containsKey(c)) {
                curr.map.put(c, new Node(c, false));
            }
            curr = curr.map.get(c);
        }
        curr.isKey = true;
    }

    /** Returns a list of all words that start with PREFIX */
    public List<String> keysWithPrefix(String prefix) {
        List<String> l = new ArrayList<>();

        Node curr = root;

        for (int i = 0, n = prefix.length(); i < n; i++) {
            char c = prefix.charAt(i);
            curr = curr.map.get(c);
        }
        prefixHelper(curr, prefix, l);
        return l;
    }

    public String keysThatMatch(String key) {
        Node curr = root;

        for (int i = 0, n = key.length(); i < n; i++) {
            char c = key.charAt(i);
            curr = curr.map.get(c);
        }

        if (curr.isKey) {
            return key;
        } else {
            return null;
        }
    }

    public void prefixHelper(Node n, String prefix, List<String> l) {
        if (n == null) {
            return;
        }
        if (n.isKey) {
            l.add(prefix);
        }
        Set<Character> chars = n.map.keySet();
        for (char c : chars) {
            prefixHelper(n.map.get(c), prefix + c, l);
            }
        }

    public String longestPrefixOf(String key) {
        throw new UnsupportedOperationException();
    }

    private class Node {

        boolean isKey;
        char c;
        HashMap<Character, Node> map = new HashMap<>();

        private Node(char c, boolean isKey) {
            this.c = c;
            this.isKey = isKey;
        }
    }


    public static void main(String[] args) {
        MyTrieSet t = new MyTrieSet();
        t.add("hello");
        t.add("hi");
        t.add("help");
        t.add("zebra");
    }
}
