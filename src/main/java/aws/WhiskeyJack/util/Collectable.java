/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.WhiskeyJack.util;

import aws.WhiskeyJack.properties.*;
import aws.WhiskeyJack.nodeviewerfx.*;
import static aws.WhiskeyJack.util.Collectable.*;
import static aws.WhiskeyJack.util.EZOutput.*;
import static aws.WhiskeyJack.util.Utils.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Create an object that can be easily be serialized as JSON or YAML
 */
public abstract class Collectable <T extends PropertyBase> {
    /* I should probably be using Jackson's built-in autoserializer, but I
     * like the control I get by hand-rolling */
    public Object collect() {
        var ret = new HashMap<String, Object>();
        collectMore(ret);
        if(!properties.isEmpty()) {
            Map<String, Object> props = new HashMap<>();
            properties.values().forEach(p -> {
                if(!p.isImmediatelyEmpty()) {
                    var vo = asObject(p.immediateGet());
                    if(!Utils.isEmpty(vo))
                        props.put(p.getName(), vo);
                }
            });
            if(!props.isEmpty())
                putOpt(ret, "properties", props);

        }
        return ret;
    }
    protected void collectMore(Map<String, Object> map) {
    }
    public static Object asObject(Object c) {
        return switch(c) {
            case null ->
                null;
            case Collectable o ->
                o.collect();
            case Collection l ->
                l.isEmpty() ? null : l.stream().map(o -> asObject(o)).collect(Collectors.toList());
            case Map m -> {
                if(m.isEmpty())
                    yield null;
                var rm = new LinkedHashMap<Object, Object>();
                m.forEach((k, v) -> rm.put(asObject(k), asObject(v)));
                yield rm;
            }
            case CharSequence cs ->
                cs;
            case Number n ->
                n;
            case Boolean b ->
                b;
            default -> {
                if(c.getClass().isArray()) {
                    var len = Array.getLength(c);
                    var list = new ArrayList<Object>();
                    for(var i = 0; i < len; i++)
                        list.add(asObject(Array.get(c, i)));
                    yield list;
                }
                System.out.println("Couldn't serialize " + c.getClass() + ": " + c);
                yield c;
            }
        };
    }
    public static void putOpt(Map<String, Object> map, String key, Object value0) {
        var value = asObject(value0);
        if(value == null || value == Boolean.FALSE)
            return;
        if(value instanceof CharSequence cs && cs.isEmpty())
            return;
        if(value instanceof Number n && n.doubleValue() == 0)
            return;
        map.put(key, value);
    }
    public static <T> T getOpt(Map<String, Object> map, String key, T dflt) {
        if(map == null)
            return dflt;
        var s = map.get(key);
        return s == null ? dflt
            : (T) (dflt instanceof String || !(s instanceof String ss) ? s
                : parseObject(ss));
    }
    public static String get(Map m, String k, String dflt) {
        var v = m.get(k);
        return v == null ? dflt : v.toString();
    }
    public static double get(Map m, String k, double dflt) {
        var v = m.get(k);
        return v == null ? dflt : Coerce.toDouble(v);
    }
    public static boolean get(Map m, String k, boolean dflt) {
        var v = m.get(k);
        return v == null ? dflt : Coerce.toBoolean(v);
    }
    public static Collection<Object> getCollection(Map m, String k) {
        return m == null ? Collections.emptyList() : Coerce.toCollection(m.get(k));
    }
    public static Map<String, Object> getMap(Map m, String k) {
        if(m == null)
            return Map.of();
        var v = m.get(k);
        if(v == null)
            return Map.of();
        if(v instanceof Map map)
            return map;
        Dlg.error("getMap: bad value for " + k + " (" + v + ")");
        return Map.of();
    }
    public abstract void appendRefTo(StringBuilder sb);
    /**
     * get string that can be parsed as an object reference
     *
     * @return reference string
     */
    public final String getRef() {
        var sb = new StringBuilder();
        appendRefTo(sb);
        return sb.toString();
    }
    public static void dump(Object c, String title) {
        System.out.println(title);
        Collectable.dump(c, 1);
    }
    public static void dump(Object c) {
        Collectable.dump(c, 0);
    }
    public static void dump(Object c, int depth) {
        for(var i = depth; --i >= 0;)
            System.out.print("  ");
        switch(c) {
            case Collectable o ->
                System.out.println("Collectable? " + o.getClass() + " = " + o);
            case Collection l -> {
                System.out.println("Collection " + l.size());
                l.forEach((e -> Collectable.dump(e, depth + 1)));
            }
            case Map m -> {
                System.out.println("{}");
                m.forEach((k, v) -> {
                    Collectable.dump(k, depth + 1);
                    Collectable.dump(v, depth + 2);
                });
            }
            case CharSequence cs ->
                System.out.println(cs);
            case Number n ->
                System.out.println(n);
            case Boolean b ->
                System.out.println(b);
            default -> {
                if(c.getClass().isArray()) {
                    var len = Array.getLength(c);
                    System.out.println("[" + len + "]");
                    for(var i = 0; i < len; i++)
                        Collectable.dump(Array.get(c, i), depth + 1);
                } else
                    System.out.println("what? " + c.getClass());
            }
        }
    }
    protected final Map<String, T> properties = new HashMap<>();

    public final T getProperty(String s) {
        return properties.get(s);
    }
    public final Object getProp0(String s, Object dflt) {
        var p = properties.get(s);
        if("domain".equals(s)) D."getProp0: \{p}";
        return p == null || p.isEmpty() ? dflt : p.get();
    }
    public Object getProp(String s, Object dflt) {
        return getProp0(s, dflt);
    }
    public String getStringProp(String s, String dflt) {
        return Coerce.toString(getProp(s, dflt));
    }
    public List<String> getStringListProp(String s) {
        return Coerce.toStringList(getProp(s, null));
    }
    public boolean getBooleanProp(String s, boolean dflt) {
        return Coerce.toBoolean(getProp(s, dflt));
    }
    public int getIntProp(String s, int dflt) {
        return Coerce.toInt(getProp(s, dflt));
    }
    public Map getMapProp(String s, String sakey) {
        var m = getProp(s, null);
        return m instanceof Map map ? map
            : m == null || sakey == null ? Collections.emptyMap()
                : Map.of(sakey, m);
    }
    public void putProp(String k, Object v) {
        if(!isEmpty(v))
            if(v instanceof PropertyBase pb)
                properties.put(k, (T) pb);
            else properties.computeIfAbsent(k, K ->
                (T) new Property(MetaPropertyProvider.dflt.getMetaProperty(K), null))
                .setValue(v);
    }
    public void putProps(Map m) {
        if(m != null)
            m.forEach((k, v) -> putProp(Coerce.toString(k), v));
    }
    public void putMissingProps(Map<String, Object> m) {
        if(m != null)
            m.forEach((k, v) -> putProp(k, v));
    }
    public void forAllProperties(Consumer<T> func) {
        properties.values().forEach(func);
    }
}
