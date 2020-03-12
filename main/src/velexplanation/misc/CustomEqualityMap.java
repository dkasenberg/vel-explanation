package velexplanation.misc;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.*;
import java.util.function.BiPredicate;

public class CustomEqualityMap<K, V> implements Map<K, V> {

//    Equality /hashing isn't defined for this class right now.
//    This is mostly for convenience.

    protected List<K> keys;
    protected List<V> values;
    protected BiPredicate<K, K> eqTest;

    public CustomEqualityMap(BiPredicate<K, K> eqTest) {
        this.eqTest = eqTest;
        this.keys = new ArrayList<>();
        this.values = new ArrayList<>();
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    protected int indexOf(K key) {
        for (int i = 0; i < keys.size(); i++) {
            if (eqTest.test(key, keys.get(i))) return i;
        }
        return -1;
    }

    @Override
    public boolean containsKey(Object key) {
        return indexOf((K) key) != -1;
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains((V) value);
    }

    @Override
    public V get(Object key) {
        return values.get(indexOf((K) key));
    }

    @Override
    public V put(K key, V value) {
        int index = indexOf(key);
        if (index == -1) {
            keys.add(key);
            values.add(value);
            return null;
        }
        V prevValue = values.get(index);
        values.set(index, value);
        return prevValue;
    }

    @Override
    public V remove(Object key) {
        int index = indexOf((K) key);
        if (index == -1) return null;
        V value = values.get(index);
        keys.remove(index);
        values.remove(index);
        return value;
    }

    @Override
    public void putAll(Map m) {
        for (Object k : m.keySet()) {
            this.put((K) k, (V) m.get(k));
        }

    }

    @Override
    public void clear() {
        this.keys.clear();
        this.values.clear();
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>(keys);
    }

    @Override
    public Collection<V> values() {
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> retVal = new HashSet<>();
        for (int i = 0; i < keys.size(); i++) {
            retVal.add(new MutablePair<>(keys.get(i), values.get(i)));
        }
        return retVal;
    }
}
