/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.WhiskeyJack.properties;

import aws.WhiskeyJack.nodegraph.*;
import java.util.function.*;

public class ArcEndpoint {
    private final boolean in;
    private final Arc containingArc;
    private final Property containingProperty;
    private ArcEndpoint next;
    private ArcEndpoint(boolean i) { // only used for markers
        in = i;
        containingArc = null;
        containingProperty = null;
        next = null;
    }
    @SuppressWarnings("LeakingThisInConstructor")
    private ArcEndpoint(Arc cn, Property cp) {
        var ae = (ArcEndpoint) cp.get();
        in = ae.in;
        containingArc = cn;
        containingProperty = cp;
        next = ae;
    }
    public static void connect(Arc cn, Property cp) {
        if(cp != null) {
            var ep = new ArcEndpoint(cn, cp);
            cp.set(ep);
        }
    }
    public void forEach(Consumer<ArcEndpoint> func) {
        if(next != null) {
            // if next==null, then this is just a marker
            func.accept(this);
            next.forEach(func);
        }
    }
    public ArcEndpoint delete(ArcEndpoint ae) {
        if(next == null) return this;
        if(this == ae) {
            if(containingProperty.immediateGet() == this)
                containingProperty.set(next);
            return next;
        }
        next = next.delete(ae);
        return this;
    }
    public Arc getArc() {
        return containingArc;
    }
    public Property getProperty() {
        return containingProperty;
    }
    public boolean isIn() {
        return in;
    }
    @Override
    public String toString() {
        var nm = containingProperty == null ? "□" : containingProperty.getName();
        return in
            ? "➔" + nm
            : nm + "➔";
    }
    public static ArcEndpoint of(boolean b) {
        return b ? emptyInEndpoint : emptyOutEndpoint;
    }
    public static ArcEndpoint emptyInEndpoint = new ArcEndpoint(true);
    public static ArcEndpoint emptyOutEndpoint = new ArcEndpoint(false);
}
