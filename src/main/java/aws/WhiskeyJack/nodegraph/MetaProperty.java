/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.WhiskeyJack.nodegraph;

import aws.WhiskeyJack.util.*;
import java.util.*;

public class MetaProperty extends PropertyBase {
    private final Map properties;
    public MetaProperty(Map p) { properties = p; }
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
    public String getName() {
        return getMeta("name", "unnamed");
    }
}
