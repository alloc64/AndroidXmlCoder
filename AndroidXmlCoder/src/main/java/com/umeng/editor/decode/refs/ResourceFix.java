/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.refs;

import com.umeng.editor.android.AndroidManifestAXML;
import com.umeng.editor.decode.AXMLDocument;
import com.umeng.editor.decode.nodes.Attribute;
import com.umeng.editor.decode.nodes.BNSNode;
import com.umeng.editor.decode.nodes.BTXTNode;
import com.umeng.editor.decode.nodes.BTagNode;
import com.umeng.editor.decode.nodes.BXMLNode;
import com.umeng.editor.decode.values.Resources;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ResourceFix
{
    private class AttributeMapping
    {
        private String name;
        private int resourceId;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public int getResourceId()
        {
            return resourceId;
        }

        public void setResourceId(int resourceId)
        {
            this.resourceId = resourceId;
        }
    }
    
    private List<String> attributeNames = new ArrayList<>();
    private Set<String> usedStrings = new LinkedHashSet<>();

    public void collect(AXMLDocument doc)
    {
        usedStrings.clear();

        collectRecursive(doc.getBXMLTree().getRoot());

        fixResourceIds(doc);
        
        collectGarbageStrings(doc);
        
        System.currentTimeMillis();
    }

    private void collectRecursive(BXMLNode startingNode)
    {
        collect(startingNode);

        List<BXMLNode> children = startingNode.getChildren();

        if(children == null || children.size() < 1)
            return;

        for(BXMLNode n : children)
        {
            collect(n);
            collectRecursive(n);
        }
    }

    private void collect(BXMLNode node)
    {
        if(node instanceof BNSNode)
        {
            collect((BNSNode)node);
        }
        else if(node instanceof BTagNode)
        {
            collect((BTagNode)node);
        }
        else if(node instanceof BTXTNode)
        {
            collect((BTXTNode)node);
        }
    }

    private void collect(BNSNode node)
    {
        markString(node.getPrefix());
        markString(node.getUri());
    }

    private void collect(BTagNode node)
    {
        markString(node.getNamespace());
        markString(node.getName());
        markString(node.getName());

        for(Attribute attr : node.getAttributes())
        {
            String attrNamespace = attr.getNamespace();
            String attrName = attr.getName();

            markString(attrNamespace);
            markString(attrName);
            markString(attr.getString());

            if(AndroidManifestAXML.ANDROID_NS.equals(attrNamespace) && !StringUtils.isEmpty(attrName) && !attributeNames.contains(attrName))
            {
                attributeNames.add(attrName);
            }
        }
    }

    private void collect(BTXTNode node)
    {
        markString(node.getValue());
    }

    private void markString(String string)
    {
        if(string != null)
            usedStrings.add(string);
    }

    // region Resource IDs

    private void fixResourceIds(AXMLDocument doc)
    {
        attributeNames.sort(
                Comparator.comparing(name -> Resources.Attributes.get()
                        .getPrioritizedResourceNames()
                        .getOrDefault(name, Integer.MAX_VALUE)
                )
        );

        List<AttributeMapping> attributeMappingList = new ArrayList<>();

        for(String attrName : attributeNames)
        {
            Integer resourceId = Resources.Attributes.get().getAttributeId(attrName);

            if(resourceId != null)
            {
                AttributeMapping mapping = new AttributeMapping();
                mapping.setName(attrName);
                mapping.setResourceId(resourceId);

                attributeMappingList.add(mapping);
            }
            else
            {
                System.out.println("Skipped unknown attribute: " + attrName);
            }
        }

        List<String> originalStringList = doc.getStringBlock().getStringList();
        List<Integer> originalResourceIdList = doc.getResBlock().getResourceIdList();

        List<String> stringList = new ArrayList<>();
        List<Integer> resourceIdList = new ArrayList<>();

        for(AttributeMapping attrMapping : attributeMappingList)
        {
            stringList.add(attrMapping.getName());
            resourceIdList.add(attrMapping.getResourceId());
        }

        for(String string : originalStringList)
        {
            if(!stringList.contains(string))
                stringList.add(string);
        }

        originalStringList.clear();
        originalStringList.addAll(stringList);

        originalResourceIdList.clear();
        originalResourceIdList.addAll(resourceIdList);

        System.currentTimeMillis();
    }
    
    // endregion
    
    // region String GC

    private void collectGarbageStrings(AXMLDocument doc)
    {
        System.out.println("Garbage collecting unused strings...");

        List<String> existingStringList = doc.getStringBlock().getStringList();

        int originalSize = existingStringList.size();

        int collected = 0;
        for (int i = 0; i < existingStringList.size(); i++)
        {
            String string = existingStringList.get(i);

            if (!usedStrings.contains(string))
            {
                System.out.println("Removing unused string: \"" + string + "\"");
                existingStringList.remove(i);
                collected++;
            }
        }

        System.out.printf("Collected %d unused strings...\n", collected);
        System.out.printf("Result size changes original: %d, collected: %d, diff: %d\n", originalSize, existingStringList.size(), (originalSize - existingStringList.size()));

    }
    // endregion
}
