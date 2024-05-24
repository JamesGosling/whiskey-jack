/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.WhiskeyJack.properties;

import java.util.*;
import java.util.concurrent.*;

public interface MetaPropertyProvider {
    public MetaProperty getMetaProperty(String k);
    public static final MetaPropertyProvider dflt = new MetaPropertyProvider() {
        private final Map<String, MetaProperty> dflts = new ConcurrentHashMap<>();
        @Override
        public MetaProperty getMetaProperty(String k) {
            return dflts.computeIfAbsent(k, key -> new MetaProperty(MetaProperty.mutableMapOf(
                "name", key,
                "type", "object",
                "description", STR."Property \{key} has no metadata"
            )));
        }
    };
}
