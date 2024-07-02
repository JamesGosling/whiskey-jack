/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.WhiskeyJack.nodegraph;

import aws.WhiskeyJack.properties.*;
import static aws.WhiskeyJack.util.EZOutput.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import javax.annotation.*;

public class Node<T extends Node> extends GraphPart<T, Property> {
    private String uid;
    protected Domain domain = Domain.unknown;
    private final Graph context;
    public final MetaNode nodeMetadata;
    public Node<T> copiedFrom;
    private boolean inferred;
    public final Map<String, Port> ports = new LinkedHashMap<>();
    @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    public Node(@Nonnull Graph parent, MetaNode mn) {
        context = parent;
        if(mn == null) {
            mn = (MetaNode) this; // for the initial metaMeta circularity
            setName("NoName");
        } else
            setName(mn.getName());
        nodeMetadata = mn;
        parent.add(this);
        mn.ports.entrySet().forEach(e -> {
            var p = context.newPort(this, e.getValue().metadata);
            ports.put(e.getKey(), p);
            p.populateFrom(e.getValue());
        });
        setDomain(getDomain()); // to trigger change notification
    }
    @Override
    public Graph getContext() {
        return context;
    }
    @Override
    public String getDescription() {
        return nodeMetadata.getDescription();
    }
    public void setUid(String u) {
        if(!Objects.equals(u,uid)) {
            if(uid != null)
                getContext().remove(this);
            uid = u;
            if(u != null) {
                uids.add(u);
                getContext().add(this);
            }
        }
    }
    public String getUid() {
        var u = uid;
        if(u == null)
            uid = u = genUID(getName());
        return u;
    }
    private final static HashMap<String, AtomicInteger> uidMap = new HashMap<>();
    private final static Set<String> uids = new HashSet<>();
    public synchronized static String genUID(String pfx) {
        while(true) {
            var candidate = pfx + uidMap.computeIfAbsent(pfx, nm ->
                    new AtomicInteger(-1)).incrementAndGet();
            if(uids.add(candidate))
                return candidate;
        }
    }
    public Domain getDomain() {
        return domain == Domain.unknown ? nodeMetadata.getDomain() : domain;
    }
    public Domain getDomain0() {
        return domain;
    }
    public T setDomain(Domain d) {
//        if("streamManager".equals(getName())) D."Set domain -> \{d}\n\t\{new Throwable()}";
        domain = d == null || d == Domain.any || d == nodeMetadata.getDomain() ? Domain.unknown : d;
        putProp("domain", d);
        return (T) this;
    }

    @Override
    public void appendRefTo(StringBuilder sb) {
        sb.append(uid);
    }
    public boolean hasUid() { // rarely needed
        return uid == null;
    }
    @Override
    public String opcode() {
        return getName();
    }
    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
    public StringBuilder appendTo(StringBuilder sb) {
        appendNameTo(sb);
        sb.append('[');
        var first = true;
        for(var p: ports.entrySet()) {
            if(first)
                first = false;
            else
                sb.append(',');
            sb.append(p.getKey()).append('=');
            var v = p.getValue();
            if(v.isConnected()) {
                v.forEachArc(new Consumer<Arc>() {
                    char pfx = '{';
                    @Override
                    public void accept(Arc a) {
                        sb.append(pfx);
                        pfx = ',';
                        a.otherEnd(v).appendFullNameTo(sb);
                    }
                });
                sb.append('}');
            } else
                sb.append(v.getValue());
        }
        sb.append(']');
        return sb;
    }
    @Override
    public StringBuilder appendNameTo(StringBuilder sb) {
        super.appendNameTo(sb)
                .append('_')
                .append(getUid());
        return sb;
    }
    public void forEachPort(Consumer<Port> f) {
        ports.values().forEach(f);
    }
    public boolean hasPorts() {
        return !ports.isEmpty();
    }
    @Override
    protected void collectMore(Map<String, Object> map) {
        super.collectMore(map);
        putOpt(map, "ports", ports);
        putOpt(map, "uid", getUid());
        putOpt(map, "meta", nodeMetadata.getUid());
        putOpt(map, "metapath", nodeMetadata.getPath());
        putOpt(map, "inferred", inferred);
        var d = getDomain();
        if(d!=nodeMetadata.getDomain()) putOpt(map, "domain", d);
    }
    public Port defaultPort(boolean in) {
        var dp = nodeMetadata.defaultPort(in);
        return dp == null ? null : getPort(dp.getName());
    }
    public void populateFrom(Node other) {
        assert nodeMetadata == other.nodeMetadata;
        assert ports.size() == other.ports.size();
        assert !hasUid();
        setUid(other.getUid());
        copiedFrom = other;
        setInferred(other.isInferred());
        setDomain(other.getDomain());
        forEachPort(p -> p.populateFrom(other.getPort(p.getName())));
    }
    public Port getPort(String s) {
        return ports.get(s);
    }
    @Override
    public Object getProp(String s, Object dflt) {
        var ret = getProp0(s, null);
        if("domain".equals(s)) D."getPropDomain0 = \{ret}";
        return ret == null ? nodeMetadata.getProp(s, dflt) : ret;
    }
    @Override
    public void populateFrom(Map map) {
        super.populateFrom(map);
        setUid(get(map, "uid", null));
        setInferred(getBooleanProp("inferred", false));
        var sd=getStringProp("domain", getDomain().toString());
        var nd = Domain.of(sd);
        if("streamManager".equals(getName())) D."populateFrom -> was \{
            getDomain().toString() }  to \{sd}/\{nd}  prop \{
            getStringProp("domain","missing")}/\{getStringProp("domain", getDomain().toString())}\n\t\{map}";
        setDomain(nd);
//        setDomain(Domain.of(getStringProp("domain", getDomain().toString())));
        populatePorts(map, "ports", false, false);
        populatePorts(map, "in", true, true); // these two lines are compatibility with an old format
        populatePorts(map, "out", false, true);
    }
    public Node setInferred(boolean b) {
        inferred = b;
        return this;
    }
    public boolean isInferred() { return inferred; }
    private void populatePorts(Map map, String key, boolean input, boolean rename) {
        getMap(map, key).forEach((k, v) -> {
            if(rename && k.equals("v"))
                k = input ? "in" : "out";
            var p = ports.get(k);
            if(p == null) {
                if(!(this instanceof MetaNode))
                    return; // not found in the metadata
                p = getContext().newPort(this, MetaPort.meta);
                ports.put(k, p);
                p.setName(k);
            }
            Map vmap;
            if(v instanceof Map pm)
                vmap = pm;
            else {
                vmap = new HashMap();
                var possibleT = Type.of(String.valueOf(v), false);
                /* If it looks like a type, it is a type.  Otherwise it's a value. */
                if(possibleT != null && possibleT != Type.any)
                    vmap.put("type", possibleT.getName());
                else {
                    vmap.put("type", Type.guess(v).toString());
                    vmap.put("value", v);
                }
            }
            if(!input)
                vmap.put("output", true);
            p.populateFrom(vmap);
        });
    }
}
