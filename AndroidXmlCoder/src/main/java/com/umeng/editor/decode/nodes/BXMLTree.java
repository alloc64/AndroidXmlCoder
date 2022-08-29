/***********************************************************************
 * Copyright (c) 2014 https://github.com/ntop001/AXMLEditor
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/
package com.umeng.editor.decode.nodes;

import com.umeng.editor.decode.blocks.ResBlock;
import com.umeng.editor.decode.blocks.StringBlock;
import com.umeng.editor.decode.coders.IAXMLSerialize;
import com.umeng.editor.decode.coders.IntReader;
import com.umeng.editor.decode.coders.IntWriter;
import com.umeng.editor.decode.visitors.IVisitor;
import com.umeng.editor.decode.values.Pair;

import java.io.IOException;
import java.util.Stack;

public class BXMLTree implements IAXMLSerialize
{
    private final int NS_START = 0x00100100;
    private final int NS_END = 0x00100101;
    private final int NODE_START = 0x00100102;
    private final int NODE_END = 0x00100103;
    private final int TEXT = 0x00100104;

    private final StringBlock stringBlock;
    private final ResBlock resBlock;

    private Stack<BXMLNode> visitorStack;
    private BNSNode rootNode;
    private int size;

    public BXMLTree(StringBlock stringBlock, ResBlock resBlock)
    {
        this.stringBlock = stringBlock;
        this.resBlock = resBlock;

        this.rootNode = new BNSNode(this);
        this.visitorStack = new Stack<>();
    }

    public void print(IVisitor visitor)
    {
        rootNode.accept(visitor);
    }

    public void write(IntWriter writer) throws IOException
    {
        write(rootNode, writer);
    }

    public void prepare()
    {
        this.size = 0;
        prepare(rootNode);
    }

    private void write(BXMLNode node, IntWriter writer) throws IOException
    {
        node.writeStart(writer);

        if (node.hasChild())
            for (BXMLNode child : node.getChildren())
                write(child, writer);

        node.writeEnd(writer);
    }

    private void prepare(BXMLNode node)
    {
        node.prepare();
        Pair<Integer, Integer> p = node.getSize();

        if(p.first == null || p.second == null)
            System.currentTimeMillis();

        this.size += p.first + p.second;

        if (node.hasChild())
        {
            for (BXMLNode child : node.getChildren())
            {
                prepare(child);
            }
        }
    }

    public StringBlock getStringBlock()
    {
        return stringBlock;
    }

    public ResBlock getResBlock()
    {
        return resBlock;
    }

    public int getSize()
    {
        return size;
    }

    public BXMLNode getRoot()
    {
        return rootNode;
    }

    public void read(IntReader reader) throws IOException
    {
        rootNode.checkStartTag(NS_START);
        visitorStack.push(rootNode);
        rootNode.readStart(reader);

        int chunkType;

        end:
        while (true)
        {
            chunkType = reader.readInt();

            switch (chunkType)
            {
                case NODE_START:
                {
                    BTagNode node = new BTagNode(this);
                    node.checkStartTag(NODE_START);
                    BXMLNode parent = visitorStack.peek();
                    parent.addChild(node);
                    visitorStack.push(node);

                    node.readStart(reader);
                }
                break;

                case NODE_END:
                {
                    BTagNode node = (BTagNode) visitorStack.pop();
                    node.checkEndTag(NODE_END);
                    node.readEnd(reader);
                }
                break;

                case TEXT:
                {
                    BTXTNode node = new BTXTNode(this);

                    BXMLNode parent = visitorStack.peek();
                    parent.addChild(node);

                    node.readStart(reader);
                    node.readEnd(reader);
                }
                break;

                case NS_END:
                    break end;
            }
        }

        //if (!rootNode.equals(visitorStack.pop()))
        //    throw new IOException("AXML Document has invalid end");

        rootNode.checkEndTag(chunkType);
        rootNode.readEnd(reader);
    }

    @Override
    public int getType()
    {
        return 0;
    }

    @Override
    public void setSize(int size)
    {
    }

    @Override
    public void setType(int type)
    {
    }
}
