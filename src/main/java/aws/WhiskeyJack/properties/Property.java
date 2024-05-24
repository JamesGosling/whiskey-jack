/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.WhiskeyJack.properties;

public class Property extends PropertyBase {
    public Property(MetaProperty meta, Object value) {
        super(value);
        propMetadata = meta == null ? MetaProperty.whatever : meta;
    }
    public Property(MetaProperty meta) {
        super();
        propMetadata = meta == null ? MetaProperty.whatever : meta;
    }
    public Property() {
        super();
        propMetadata = MetaProperty.whatever;
    }
    private final MetaProperty propMetadata;
    @Override
    public Object get() {
        return !isImmediatelyEmpty() ? immediateGet() : propMetadata.get();
    }
    @Override
    public Object getMeta(String k) {
        return propMetadata.getMeta(k);
    }
    @Override
    public String getMeta(String k, String dflt) {
        return propMetadata.getMeta(k, dflt);
    }
    @Override
    public boolean getMeta(String k, boolean dflt) {
        return propMetadata.getMeta(k, dflt);
    }
    @Override
    public String getName() {
        return propMetadata.getName();
    }
    @Override
    public boolean isEmpty() {
        return isImmediatelyEmpty() && propMetadata.isImmediatelyEmpty();
    }
    @Override
    public String toString() {
        return "[property " + propMetadata.getName()+":"+System.identityHashCode(this) + "="
               + immediateGet() + "/" + get() + "]";
    }
}
