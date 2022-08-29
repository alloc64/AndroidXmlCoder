/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.refs;

import com.umeng.editor.decode.nodes.BTagNode;
import com.umeng.editor.decode.nodes.BXMLNode;

public class ResourceReference extends AXMLReference
{
    public static ResourceReference resolve(BXMLNode node, int id)
    {
        if(node == null)
            return null;

        int value = node.getResBlock().getResourceAt(id);

        if(value < 0)
            value = id;

        return new ResourceReference(node, value);
    }

    public static AXMLReference raw(BTagNode node, int id)
    {
        ResourceReference ref = new ResourceReference(node, id);
        ref.setResolveId(false);

        return ref;
    }

    private int value;
    private boolean resolveId;

    @Override
    public int getId()
    {
        int value = getValue();

        if(resolveId)
            return node.getResBlock().getResourceId(value);

        return value;
    }

    public void setResolveId(boolean resolveId)
    {
        this.resolveId = resolveId;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    private ResourceReference(BXMLNode node, int value)
    {
        super(node);

        this.setValue(value);
    }

    @Override
    public String toString()
    {
        return String.format("0x0%X", getValue());
    }
}
