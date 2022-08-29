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
import com.umeng.editor.decode.values.Resources;
import com.umeng.editor.decode.values.TypedValue;
import com.umeng.editor.decode.visitors.IVisitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.umeng.editor.android.AndroidManifestAXML.ANDROID_NS;


public class BTagNode extends BXMLNode
{
    private static final int INT_SIZE = 4;

    private static final int TAG_START = 0x00100102;
    private static final int TAG_END = 0x00100103;

    private StringReference rawNSUri;
    private StringReference rawName;

    private short rawClassAttr; // class
    private short rawIdAttr;    // android:id
    private short rawStyleAttr; // style

    private List<Attribute> rawAttrs = new ArrayList<>();

    public BTagNode(BXMLTree root)
    {
        super(root);
    }

    @Override
    public void setRoot(BXMLTree root)
    {
        super.setRoot(root);

        if(rawAttrs != null && rawAttrs.size() > 0)
            for(Attribute attr : rawAttrs)
                attr.setNode(this);
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

        reader.readInt(); //unused int value(0xFFFF_FFFF)

        this.rawNSUri = StringReference.resolve(this, reader.readInt()); // ns uri (0xFFFF)
        this.rawName = StringReference.resolve(this, reader.readInt()); //name for element

        reader.readInt();  // unknown field

        short rawAttrCount = (short) reader.readShort(); // attribute count

        this.rawIdAttr = (short) reader.readShort(); // id attribute
        this.rawClassAttr = (short) reader.readShort(); // class
        this.rawStyleAttr = (short) reader.readShort();

        for (int i = 0; i < rawAttrCount; i++)
        {
            Attribute attribute = new Attribute(
                    this,
                    reader.readInt(), // namespace
                    reader.readInt(), // name
                    reader.readInt(), // string id
                    reader.readInt(), // type
                    reader.readInt()  // value (used for resources + strings)
            );

            rawAttrs.add(attribute);
        }
    }

    public void readEnd(IntReader reader) throws IOException
    {
        super.readEnd(reader);

        reader.readInt(); // unused int value(0xFFFF_FFFF)

        int endNsUri = reader.readInt();
        int endName = reader.readInt();

        int startNsUri = AXMLReference.getId(rawNSUri);
        int startName = AXMLReference.getId(rawName);

        //if ((endNsUri != startNsUri) || (endName != startName))
        //    throw new IOException("Invalid end element.");
    }

    public void prepare()
    {
        int base = INT_SIZE * 9;

        // android uses indexed attributes, so all attributes must be sorted by R.attr ids
        rawAttrs.sort(
                Comparator.comparing(attr -> Resources.Attributes.get().getPrioritizedResourceNames().getOrDefault(attr.getName(), Integer.MAX_VALUE))
        );

        //ignore id, class, style attribute's bee's way

        int attrSize = rawAttrs.size() * Attribute.SIZE * INT_SIZE;
        this.chunkSize.first = base + attrSize;
        this.chunkSize.second = INT_SIZE * 6;

        //TODO: ~ line number ~
    }

    public void writeStart(IntWriter writer) throws IOException
    {
        writer.writeInt(TAG_START);

        super.writeStart(writer);

        writer.writeInt(0xFFFFFFFF);
        writer.writeInt(AXMLReference.getId(rawNSUri));
        writer.writeInt(AXMLReference.getId(rawName));
        writer.writeInt(0x00140014);

        short rawAttrCount = (short) rawAttrs.size();

        writer.writeShort(rawAttrCount);
        writer.writeShort(rawIdAttr);
        writer.writeShort(rawClassAttr);
        writer.writeShort(rawStyleAttr);

        for (int i = 0; i < rawAttrs.size(); i++)
        {
            Attribute attr = rawAttrs.get(i);

            if(i == 1 && attr.getTypedValue() == TypedValue.TYPE_STRING)
                System.currentTimeMillis();

            attr.write(writer);
        }
    }

    public void writeEnd(IntWriter writer) throws IOException
    {
        writer.writeInt(TAG_END);

        super.writeEnd(writer);

        writer.writeInt(0xFFFFFFFF);
        writer.writeInt(AXMLReference.getId(rawNSUri));
        writer.writeInt(AXMLReference.getId(rawName));
    }

    /**
     * Eg:android:id="@+id/xxx". Equivalent to getAttributeValue(null, "id").
     *
     * @return Attribute(name = " id ").mString
     */
    public int getIdAttribute()
    {
        return rawIdAttr;
    }

    /**
     * Eg:android:class="com.foo.example". Equivalent to getAttributeValue(null, "class").
     *
     * @return Attribute(name = " class ").mString
     */
    public int getClassAttribute()
    {
        return rawClassAttr;
    }

    /**
     * Eg:style=""@style/Button". Equivalent to getAttributeValue(null, "style").
     *
     * @return Attribute(name = " style ").mString
     */
    public int getStyleAttribute()
    {
        return rawStyleAttr;
    }

    public Attribute getAttribute(String name)
    {
        for (Attribute attr : rawAttrs)
            if (name.equals(attr.getName()))
                return attr;

        return null;
    }

    public Attribute[] getAttributes()
    {
        return rawAttrs.toArray(new Attribute[0]);
    }

    public void setAttribute(Attribute attribute)
    {
        rawAttrs.add(attribute);
    }

    public Attribute setAttribute(String attrName, String value)
    {
        Attribute attr = getAttribute(attrName);

        if(attr == null)
        {
            attr = new Attribute(this, ANDROID_NS, attrName, TypedValue.TYPE_STRING);
            attr.setType(50331656); // TODO: fix endianity issue
            attr.setString(value);

            setAttribute(attr);
        }
        else
        {
            attr.setString(value);
        }

        return attr;
    }

    public String getName()
    {
        return rawName.getValue();
    }

    public String getNamespace()
    {
        return rawName.getValue();
    }

    @Override
    public void accept(IVisitor v)
    {
        v.visit(this);
    }
}
