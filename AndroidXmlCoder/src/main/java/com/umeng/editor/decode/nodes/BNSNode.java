/***********************************************************************
 * Copyright (c) 2014 https://github.com/ntop001/AXMLEditor
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.nodes;

import com.umeng.editor.decode.coders.IntReader;
import com.umeng.editor.decode.coders.IntWriter;
import com.umeng.editor.decode.refs.AXMLReference;
import com.umeng.editor.decode.refs.StringReference;
import com.umeng.editor.decode.visitors.IVisitor;

import java.io.IOException;

public class BNSNode extends BXMLNode
{
    private final int TAG_START = 0x00100100;
    private final int TAG_END = 0x00100101;

    private StringReference prefix;
    private StringReference uri;

    public BNSNode(BXMLTree root)
    {
        super(root);
    }

    public void checkStartTag(int tag) throws IOException
    {
        checkTag(TAG_START, tag);
    }

    public void checkEndTag(int tag) throws IOException
    {
        checkTag(TAG_END, tag);
    }

    public void readStart(IntReader reader) throws IOException
    {
        super.readStart(reader);

        reader.readInt(); //unused int value(0xFFFF)

        this.prefix = StringReference.resolve(this, reader.readInt());
        this.uri = StringReference.resolve(this, reader.readInt());
    }

    public void readEnd(IntReader reader) throws IOException
    {
        super.readEnd(reader);

        reader.readInt();//skip unused value

        int endPrefix = reader.readInt();
        int endUri = reader.readInt();

        int startPrefix = AXMLReference.getId(this.prefix);
        int startUri = AXMLReference.getId(this.uri);

        //if ((endPrefix != startPrefix) || (endUri != startUri))
        //    throw new IOException("Invalid end element.");
    }

    public void prepare()
    {
        //TODO line number
    }

    public void writeStart(IntWriter writer) throws IOException
    {
        writer.writeInt(TAG_START);
        super.writeStart(writer);

        writer.writeInt(0xFFFFFFFF);
        writer.writeInt(AXMLReference.getId(prefix));
        writer.writeInt(AXMLReference.getId(uri));
    }

    public void writeEnd(IntWriter writer) throws IOException
    {
        writer.writeInt(TAG_END);
        super.writeEnd(writer);

        writer.writeInt(0xFFFFFFFF);
        writer.writeInt(AXMLReference.getId(prefix));
        writer.writeInt(AXMLReference.getId(uri));
    }

    public String getPrefix()
    {
        return prefix.getValue();
    }

    public String getUri()
    {
        return uri.getValue();
    }

    @Override
    public void accept(IVisitor v)
    {
        v.visit(this);
    }
}
