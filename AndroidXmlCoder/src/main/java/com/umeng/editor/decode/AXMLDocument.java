/***********************************************************************
 * Copyright (c) 2014 https://github.com/ntop001/AXMLEditor
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode;

import com.umeng.editor.decode.blocks.ResBlock;
import com.umeng.editor.decode.blocks.StringBlock;
import com.umeng.editor.decode.coders.IntReader;
import com.umeng.editor.decode.coders.IntWriter;
import com.umeng.editor.decode.nodes.BTagNode;
import com.umeng.editor.decode.nodes.BXMLNode;
import com.umeng.editor.decode.nodes.BXMLTree;
import com.umeng.editor.decode.refs.ResourceFix;
import com.umeng.editor.decode.visitors.XMLVisitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AXMLDocument
{
    public interface MetadataTraverserCallback
    {
        boolean process(String name, String value);
    }

    private static final int MAGIC_NUMBER = 0X00080003;
    private static final int CHUNK_STRING_BLOCK = 0X001C0001;
    private static final int CHUNK_RESOURCE_ID = 0X00080180;
    private static final int CHUNK_XML_TREE = 0X00100100;

    protected int documentSize;
    protected StringBlock stringBlock;
    protected ResBlock resBlock;

    protected BXMLTree xmlTree;

    public AXMLDocument()
    {
    }

    public StringBlock getStringBlock()
    {
        return stringBlock;
    }

    public ResBlock getResBlock()
    {
        return resBlock;
    }

    public BXMLTree getBXMLTree()
    {
        return xmlTree;
    }

    protected BXMLNode getTagIn(String tag, BXMLNode parentNode)
    {
        if (tag == null || parentNode == null)
            return null;

        for (BXMLNode node : parentNode.getChildren())
            if (tag.equals(((BTagNode) node).getName()))
                return node;

        return null;
    }

    protected List<BTagNode> getTagsIn(String tag, BXMLNode parentNode)
    {
        List<BTagNode> nodes = new ArrayList<>();

        List<BXMLNode> children = parentNode.getChildren();

        if(children != null)
        {
            for (BXMLNode n : children)
            {
                if (n instanceof BTagNode)
                {
                    BTagNode node = (BTagNode) n;

                    if (tag.equals(node.getName()))
                        nodes.add(node);
                }
            }
        }

        return nodes;
    }


    /**
     * Prepare() should be called, if any resource has changed.
     *
     * @param os
     * @throws IOException
     */
    public void build(OutputStream os) throws IOException
    {
        IntWriter writer = new IntWriter(os, false);

        ResourceFix prioritizer = new ResourceFix();
        prioritizer.collect(this);

        stringBlock.prepare();
        resBlock.prepare();
        xmlTree.prepare();

        int base = 8;
        this.documentSize = base + stringBlock.getSize() + resBlock.getSize() + xmlTree.getSize();

        writer.writeInt(MAGIC_NUMBER);
        writer.writeInt(documentSize);

        stringBlock.write(writer);
        resBlock.write(writer);
        xmlTree.write(writer);

        os.flush();
        os.close();
    }

    public void print()
    {
        xmlTree.print(new XMLVisitor());
    }

    public void parse(InputStream is) throws Exception
    {
        IntReader reader = new IntReader(is, false);

        int magic = reader.readInt();

        if (magic != MAGIC_NUMBER)
            throw new RuntimeException("Not valid AXML format");

        this.documentSize = reader.readInt();

        int chunkType = reader.readInt();

        if (chunkType == CHUNK_STRING_BLOCK)
            parseStringBlock(reader);

        chunkType = reader.readInt();

        if (chunkType == CHUNK_RESOURCE_ID)
            parseResourceBlock(reader);

        chunkType = reader.readInt();

        if (chunkType == CHUNK_XML_TREE)
            parseXMLTree(reader);
    }

    private void parseStringBlock(IntReader reader) throws Exception
    {
        StringBlock block = new StringBlock();
        block.read(reader);

        this.stringBlock = block;
    }

    private void parseResourceBlock(IntReader reader) throws IOException
    {
        ResBlock block = new ResBlock();
        block.read(reader);

        this.resBlock = block;
    }

    private void parseXMLTree(IntReader reader) throws Exception
    {
        BXMLTree tree = new BXMLTree(stringBlock, resBlock);
        tree.read(reader);

        this.xmlTree = tree;
    }
}
