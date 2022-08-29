/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.refs;

import com.umeng.editor.decode.nodes.BXMLNode;

public class StringReference extends AXMLReference
{
    private String value;

    public static StringReference create(BXMLNode node, String value)
    {
        StringReference ref = new StringReference(node);
        ref.setValue(value);

        return ref;
    }

    public static StringReference resolve(BXMLNode node, int id)
    {
        if(node == null)
            return null;

        String value = node.getStringBlock().getStringAt(id);

        if(value == null)
            return null;

        return new StringReference(node, value);
    }

    @Override
    public int getId()
    {
        return node.getStringBlock().getStringIdOrCreate(value);
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        node.getStringBlock().getStringIdOrCreate(value);

        this.value = value;
    }

    public StringReference(BXMLNode node)
    {
        super(node);
    }

    public StringReference(BXMLNode node, String value)
    {
        this(node);
        this.value = value;
    }

    @Override
    public String toString()
    {
        return String.format("%s(0x0%X)", value, getId());
    }
}
