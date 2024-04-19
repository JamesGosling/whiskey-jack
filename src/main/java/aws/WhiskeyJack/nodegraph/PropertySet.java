/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.WhiskeyJack.nodegraph;

import java.util.*;
import java.util.function.*;

public class PropertySet<T extends PropertyBase> implements Set<T> {
    private T[] set = (T[])empty;
    private boolean inOrder;
    private static final PropertyBase[] empty = new PropertyBase[0];
    @Override
    public boolean add(T e) {
        if(e == null || slot(e.getName()) >= 0) return false;
        var pos = set.length;
        var n = Arrays.copyOf(set, pos + 1);
        n[pos] = e;
        set = n;
        inOrder = false;
        return true;
    }
    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean ret = false;
        if(c != null)
            for(var e: c)
                ret |= add(e);
        return ret;
    }
    public T get(String key) {
        var p = slot(key);
        return p<0 ? null : set[p];
    }
    @Override
    public void clear() {
        set = (T[])empty;
        inOrder = true;
    }
    @Override
    public boolean contains(Object o) {
        return o instanceof String s ? contains(s)
            : o instanceof PropertyBase b ? contains(b)
                : false;
    }
    public boolean contains(String o) {
        return o != null && slot(o) >= 0;
    }
    public boolean contains(PropertyBase o) {
        return o != null && contains(o.getName());
    }

    @Override
    @SuppressWarnings("")
    public boolean containsAll(Collection<?> c) {
        if(c != null)
            for(var o: c)
                if(!contains(o)) return false;
        return true;
    }
    @Override
    public boolean isEmpty() {
        return set.length == 0;
    }
    @Override
    public Iterator<T> iterator() {
        return new Iterator() {
            {
                sort();
            }
            T[] s = set;
            int limit = s.length;
            int i = 0;
            @Override
            public boolean hasNext() {
                return i < limit;
            }
            @Override
            public T next() {
                return s[i++];
            }
        };
    }
    @Override
    public boolean remove(Object o) {
        var i = slot(o);
        if(i < 0) return false;
        var len = set.length;
        var ns = Arrays.copyOf(set, len);
        ns[i] = set[len - 1];
        set = ns;
        inOrder = false;
        return true;
    }
    @Override
    public boolean removeAll(Collection<?> c) {
        // TODO aws.WhiskeyJack.nodegraph.PropertySet.removeAll Not implemented
        throw new UnsupportedOperationException("aws.WhiskeyJack.nodegraph.PropertySet.removeAll Not supported yet.");
    }
    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO aws.WhiskeyJack.nodegraph.PropertySet.retainAll Not implemented
        throw new UnsupportedOperationException("aws.WhiskeyJack.nodegraph.PropertySet.retainAll Not supported yet.");
    }
    @Override
    public int size() {
        return set.length;
    }
    @Override
    public Object[] toArray() {
        return Arrays.copyOf(set, set.length);
    }
    @Override
    public <T> T[] toArray(T[] a) {
        System.arraycopy(set, 0, a, 0, Math.min(a.length, set.length));
        return a;
    }
    private void sort() {
        if(!inOrder) {
            Arrays.sort(set);
            inOrder = true;
        }
    }
    private int slot(String key) {
        var s = set;
        var lim = s.length;
        for(var i = 0; i < lim; i++)
            if(set[i].nameEquals(key)) return i;
        return -1;
    }
    private int slot(Object o) {
        return o instanceof String s ? slot(s)
            : o instanceof PropertyBase b ? slot(b.getName())
                : -1;
    }
    @Override
    public void forEach(Consumer<? super T> func) {
        sort();
        var s = set;
        for(var p:s) func.accept((T)p);
    }
}
