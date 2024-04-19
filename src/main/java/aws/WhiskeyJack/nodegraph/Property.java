/*
 * SPDX-FileCopyrightText:  Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package aws.WhiskeyJack.nodegraph;

public class Property extends PropertyBase {
    public Property(MetaProperty meta, Object value) {
        super(value);
        metadata = meta;
    }
    public Property(MetaProperty meta) {
        super();
        metadata = meta;
    }
    final MetaProperty metadata;
    @Override
    public Object get() {
        return isImmediatelyEmpty() ? immediateGet() : metadata.get();
    }
    @Override
    public Object getMeta(String k) {
        return metadata.getMeta(k);
    }
    @Override
    public String getMeta(String k, String dflt) {
        return metadata.getMeta(k, dflt);   }
    @Override
    public boolean getMeta(String k, boolean dflt) {
       return metadata.getMeta(k,dflt);   }
    @Override
    public String getName() {
        return metadata.getName();
    }
    @Override
    public boolean isEmpty() {
        return isImmediatelyEmpty() && metadata.isImmediatelyEmpty();
    }
}
