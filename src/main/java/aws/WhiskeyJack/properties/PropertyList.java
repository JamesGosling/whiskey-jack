/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package aws.WhiskeyJack.properties;

import java.util.*;

public class PropertyList {
    private PropertyList parent;
    private final HashMap<String,Property> properties = new HashMap<>();
    public PropertyList(PropertyList p) {
        parent = p;
    }
    public PropertyList() {
        this(null);
    }
    public Property get(String k) {
        var ret = properties.get(k);
        if(ret==null && parent!=null) ret = parent.get(k);
        return ret;
    }
    public Object get(String k, Object dflt) {
        var p = get(k);
        return p!=null ? p.get() : dflt;
    }
    public String get(String k, String dflt) {
        var p = get(k);
        return p!=null ? p.asString(): dflt;
    }
    public boolean get(String k, boolean dflt) {
        var p = get(k);
        return p!=null ? p.asBoolean(): dflt;
    }
    public int get(String k, int dflt) {
        var p = get(k);
        return p!=null ? p.asInt(): dflt;
    }
}
