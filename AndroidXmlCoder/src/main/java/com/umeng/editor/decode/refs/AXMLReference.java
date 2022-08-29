/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.refs;

import com.umeng.editor.decode.nodes.BXMLNode;

public abstract class AXMLReference
{
    public static int getId(AXMLReference ref)
    {
        if(ref == null)
            return -1;

        return ref.getId();
    }

    protected BXMLNode node;

    public abstract int getId();

    public BXMLNode getNode()
    {
        return node;
    }

    protected AXMLReference(BXMLNode node)
    {
        this.node = node;
    }
}
