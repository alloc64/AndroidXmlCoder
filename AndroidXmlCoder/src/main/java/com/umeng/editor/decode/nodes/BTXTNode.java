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
import com.umeng.editor.decode.visitors.IVisitable;
import com.umeng.editor.decode.visitors.IVisitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BTXTNode extends BXMLNode implements IVisitable
{
    private final int TAG = 0x00100104;
    private int rawNameId;

    private StringReference string;

    private int ignored;

    public String getValue()
    {
        return string == null ? null : string.getValue();
    }

    public BTXTNode(BXMLTree root)
    {
        super(root);
    }

    public void checkTag(int value) throws IOException
    {
        super.checkTag(TAG, value);
    }

    public void readStart(IntReader reader) throws IOException
    {
        super.readStart(reader);

        this.rawNameId = reader.readInt();
        int stringId = reader.readInt();
        this.string = StringReference.resolve(this, stringId);
        this.ignored = reader.readInt(); // 0x0
    }

    public void readEnd(IntReader reader) throws IOException
    {
        chunkSize.second = reader.readInt();
        lineNumber.second = 0;
    }

    public void prepare()
    {

    }

    public void writeStart(IntWriter writer) throws IOException
    {
        writer.writeInt(TAG);

        super.writeStart(writer);

        writer.writeInt(rawNameId);

        writer.writeInt(AXMLReference.getId(string));
        writer.writeInt(ignored);
    }

    public void writeEnd(IntWriter writer) throws IOException
    {
        writer.writeInt(chunkSize.second);
    }

    public int getNameId()
    {
        return rawNameId;
    }

    public boolean hasChild()
    {
        return false;
    }

    public List<BXMLNode> getChildren()
    {
        return new ArrayList<>();
    }

    public void addChild(BXMLNode node)
    {
        throw new RuntimeException("Can't add child to Text node.");
    }

    @Override
    public void accept(IVisitor v)
    {
        v.visit(this);
    }

}
