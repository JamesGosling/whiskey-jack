/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.WhiskeyJack.nodegraph;

import aws.WhiskeyJack.util.*;
import static aws.WhiskeyJack.util.Utils.*;
import java.util.*;
import java.util.function.*;

public abstract class PropertyBase implements Comparable<PropertyBase> {
    private Object value;

    protected PropertyBase(Object v) {
        value = v;
    }
    protected PropertyBase() {
        value = EMPTY;
    }
    public boolean isEmpty() {
        return isImmediatelyEmpty();
    }
    public final boolean isPresent() {
        return !isEmpty();
    }
    public Object get() {
        return immediateGet();
    }
    public abstract String getName();
    protected final boolean isImmediatelyEmpty() {
        var v = value;
        if(v instanceof PropertyBase p) v = p.value;
        return v == EMPTY;
    }
    protected final Object immediateGet() {
        var v = value;
        if(isImmediatelyEmpty())
            throw new NoSuchElementException("No value present");
        return v instanceof PropertyBase p ? p.immediateGet() : value;
    }
    public final Object get(Object dflt) {
        return isEmpty() ? dflt : get();
    }
    public void set(Object v) {
        value = v;
    }
    public void set(PropertyBase v) {
        value = v.get();
    }
    public void clear() {
        value = EMPTY;
    }
    public final void ifPresent(Consumer action) {
        if(!isEmpty())
            action.accept(get());
    }
    public final void ifPresentOrElse(Consumer action, Runnable emptyAction) {
        if(isPresent())
            action.accept(get());
        else
            emptyAction.run();
    }
    public boolean asBoolean() {
        return Coerce.toBoolean(get());
    }
    public int asInt() {
        return Coerce.toInt(get());
    }
    public String asString() {
        return Coerce.toString(get());
    }
    public abstract Object getMeta(String k);
    public abstract String getMeta(String k, String dflt);
    public abstract boolean getMeta(String k, boolean dflt);
    public boolean nameEquals(String b) {
        return getName().equalsIgnoreCase(b);
    }
    public boolean nameEquals(PropertyBase b) {
        return nameEquals(b.getName());
    }
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        return obj instanceof PropertyBase other
            ? Objects.equals(value, other.value)
            : Objects.equals(value, obj);
    }
    @Override public int compareTo(PropertyBase other) {
        return String.CASE_INSENSITIVE_ORDER.compare(getName(),other.getName());
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
    @Override
    public String toString() {
        return getName()+":"+deepToString(value);
    }
    private static final Object EMPTY = new Object() {
        @Override
        public String toString() {
            return "EMPTY";
        }
    };

}
