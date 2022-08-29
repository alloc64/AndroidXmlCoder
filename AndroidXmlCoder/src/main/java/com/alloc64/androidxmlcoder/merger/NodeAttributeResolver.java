/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.androidxmlcoder.merger;

import com.umeng.editor.android.AndroidManifestAXML;
import com.umeng.editor.decode.nodes.Attribute;
import com.umeng.editor.decode.nodes.BTagNode;
import com.umeng.editor.decode.nodes.BXMLNode;

import java.util.List;

public class NodeAttributeResolver
{
    private AndroidManifestAXML inputManifest;

    public NodeAttributeResolver(AndroidManifestAXML inputManifest)
    {
        this.inputManifest = inputManifest;
    }

    public void resolveAttributes(AndroidManifestAXML mergedManifest)
    {
        resolveAttributesRecursive(mergedManifest.getManifestNode());
    }

    private void resolveAttributesRecursive(BXMLNode startingNode)
    {
        List<BXMLNode> children = startingNode.getChildren();

        if(children == null || children.size() < 1)
            return;

        for(BXMLNode n : children)
        {
            if(n instanceof BTagNode)
            {
                resolveAttributes(((BTagNode)n));
                resolveAttributesRecursive(n);
            }
        }
    }

    private void resolveAttributes(BTagNode node)
    {
        String namespace = node.getNamespace();
        String name = node.getName();

        mergeString(namespace);
        mergeString(name);

        Attribute[] attrs = node.getAttributes();

        for(Attribute attr : attrs)
        {
            String attrNamespace = attr.getNamespace();
            String attrName = attr.getName();

            mergeString(attrNamespace);
            mergeString(attrName);

            String attrString = attr.getString();

            if(attrString != null)
                mergeString(attrString);
        }
    }

    private void mergeString(String key)
    {
        inputManifest
                .getStringBlock()
                .getStringIdOrCreate(key);
    }
}
