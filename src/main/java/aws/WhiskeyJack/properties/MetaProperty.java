/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.WhiskeyJack.properties;

import aws.WhiskeyJack.util.*;
import java.util.*;

public class MetaProperty extends PropertyBase {
    public static final String unEditable = "un_editable";
    private final Map properties;
    public MetaProperty(Map p) {
        properties = p;
        if(mandatory_uneditable.contains(getName()))
            properties.put(unEditable, true);
    }
    @Override
    public Object getMeta(String k) {
        return properties.get(k);
    }
    @Override
    public String getMeta(String k, String dflt) {
        var v = getMeta(k);
        return v == null ? dflt : Coerce.toString(v);
    }
    @Override
    public boolean getMeta(String k, boolean dflt) {
        var v = getMeta(k);
        return v == null ? dflt : Coerce.toBoolean(v);
    }
    @Override
    public final String getName() {
        return getMeta("name", "unnamed");
    }
    private static final Set<String> mandatory_uneditable = Set.of(
        "expanded",
        "inferred",
        "x",
        "y",
        "uid"
    );
    public static MetaProperty whatever = new MetaProperty(mutableMapOf("name", "whatever",
        "type", "object",
        "description", "Property with no metadata",
        unEditable, true
    ));
    public static Map mutableMapOf(Object... objs) {
        var ret = new HashMap();
        if(objs!=null) {
            var limit = objs.length-1;
            for(var i = 0; i<limit; i+=2)
                ret.put(objs[i], objs[i+1]);
        }
        return ret;
    }
}
