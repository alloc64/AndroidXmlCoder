/***********************************************************************
 * Copyright (c) 2014 https://github.com/ntop001/AXMLEditor
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.nodes;

import com.umeng.editor.decode.blocks.ResBlock;
import com.umeng.editor.decode.blocks.StringBlock;
import com.umeng.editor.decode.coders.IntReader;
import com.umeng.editor.decode.coders.IntWriter;
import com.umeng.editor.decode.values.Pair;
import com.umeng.editor.decode.visitors.IVisitable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BXMLNode implements IVisitable
{
    private BXMLTree root;

    public Pair<Integer, Integer> chunkSize = new Pair<>();
    public Pair<Integer, Integer> lineNumber = new Pair<>(); { lineNumber.first = 0; lineNumber.second = 0; }

    private List<BXMLNode> children = new ArrayList<>();

    public BXMLNode(BXMLTree root)
    {
        setRoot(root);
    }

    public BXMLTree getRoot()
    {
        return root;
    }

    public void setRoot(BXMLTree root)
    {
        this.root = root;

        if(children != null && children.size() > 0)
        {
            for(BXMLNode c : children)
                c.setRoot(root);
        }
    }

    public StringBlock getStringBlock()
    {
        return root.getStringBlock();
    }

    public ResBlock getResBlock()
    {
        return root.getResBlock();
    }

    public void checkTag(int expect, int value) throws IOException
    {
        if (value != expect)
            throw new IOException("Can't read current node");
    }

    public void readStart(IntReader reader) throws IOException
    {
        chunkSize.first = reader.readInt();
        lineNumber.first = reader.readInt();
    }

    public void readEnd(IntReader reader) throws IOException
    {
        chunkSize.second = reader.readInt();
        lineNumber.second = reader.readInt();
    }

    public void writeStart(IntWriter writer) throws IOException
    {
        writer.writeInt(chunkSize.first);
        writer.writeInt(lineNumber.first);
    }

    public void writeEnd(IntWriter writer) throws IOException
    {
        writer.writeInt(chunkSize.second);
        writer.writeInt(lineNumber.second);
    }

    public boolean hasChild()
    {
        return (children != null && !children.isEmpty());
    }

    public List<BXMLNode> getChildren()
    {
        return children;
    }

    public List<BTagNode> findTags(String... tags)
    {
        final LinkedHashSet tagSet = new LinkedHashSet<>(Arrays.asList(tags));

        return children.stream()
                .filter(r -> r instanceof BTagNode && tagSet.contains(((BTagNode)r).getName()))
                .map(r -> (BTagNode)r)
                .collect(Collectors.toList());
    }

    public List<BTagNode> findTag(String tag)
    {
        return findTags(tag);
    }

    public void addChild(BXMLNode node)
    {
        if (node != null)
            children.add(node);
    }

    public abstract void prepare();

    public Pair<Integer, Integer> getSize()
    {
        return chunkSize;
    }

    public Pair<Integer, Integer> getLineNumber()
    {
        return lineNumber;
    }
}
