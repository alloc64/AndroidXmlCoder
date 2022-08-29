/***********************************************************************
 * Copyright (c) 2014 https://github.com/ntop001/AXMLEditor
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.nodes;

import com.umeng.editor.decode.coders.IntWriter;
import com.umeng.editor.decode.refs.AXMLReference;
import com.umeng.editor.decode.refs.ResourceReference;
import com.umeng.editor.decode.refs.StringReference;
import com.umeng.editor.decode.values.TypedValue;

import java.io.IOException;

public class Attribute
{
    public static final int SIZE = 5;

    private BTagNode node;

    private StringReference namespace;
    private StringReference name;
    private AXMLReference string;

    private int type;
    private int typedValue;
    private AXMLReference resource;

    public Attribute(BTagNode node, int namespaceId, int nameId, int stringId, int type, int resourceId)
    {
        this(node);

        this.namespace = StringReference.resolve(node, namespaceId);
        this.name = StringReference.resolve(node, nameId);
        this.setType(type);

        if(typedValue == TypedValue.TYPE_REFERENCE)
        {
            this.resource = ResourceReference.resolve(node, resourceId);
        }
        else if(typedValue == TypedValue.TYPE_STRING)
        {
            this.string = StringReference.resolve(node, stringId);
            this.resource = this.string;
        }
        else
        {
            this.resource = ResourceReference.raw(node, resourceId);
        }
    }

    public Attribute(BTagNode node, String namespace, String name, int type)
    {
        this(node);

        this.namespace = new StringReference(node, namespace);
        this.name = new StringReference(node, name);
        this.setTypeFromTypedValue(type);
    }

    private Attribute(BTagNode node)
    {
        this.setNode(node);
    }

    public void setNode(BTagNode node)
    {
        this.node = node;
    }

    public String getNamespace()
    {
        return namespace == null ? null : namespace.getValue();
    }

    public String getName()
    {
        return name == null ? null : name.getValue();
    }

    public String getString()
    {
        if(string instanceof StringReference)
            return ((StringReference)string).getValue();

        return null;
    }

    public void setString(String value)
    {
        if ((type >> 24) != TypedValue.TYPE_STRING)
            throw new RuntimeException("Can't set string to non string type.");

        StringReference ref = StringReference.create(node, value);

        this.string = ref;
        this.resource = ref;
    }

    public StringReference getStringReference()
    {
        return (StringReference) string;
    }

    public int getType()
    {
        return type;
    }

    public int getTypedValue()
    {
        return typedValue;
    }

    private void setTypeFromTypedValue(int typedValue)
    {
        this.type = typedValue << 24;
        this.typedValue = typedValue;
    }

    public void setType(int type)
    {
        this.type = type;
        this.typedValue = type >> 24;
    }

    public int getResourceRaw()
    {
        if(resource instanceof ResourceReference)
            return ((ResourceReference)resource).getValue();

        return -1;
    }

    public String getResource()
    {
        int resourceId = getResourceRaw();

        // vzhledem k tomu, ze zde neresime resources co jsou v arsc, tak pouhy resource ktery je zde validni je string
        // zbytek jsou pouze resource refy mimo, ktery nedokazeme resolvnout

        return node.getStringBlock()
                .getStringAt(resourceId);
    }

    public void setResourceValue(int value)
    {
        if(!(resource instanceof ResourceReference))
            throw new IllegalStateException("Unable to set resource value. Resource not set.");

        ((ResourceReference)resource).setValue(value);
    }

    public void write(IntWriter writer) throws IOException
    {
        writer.writeInt(AXMLReference.getId(namespace));
        writer.writeInt(AXMLReference.getId(name));
        writer.writeInt(AXMLReference.getId(string));
        writer.writeInt(type);
        writer.writeInt(AXMLReference.getId(resource));
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        if(namespace != null)
        {
            sb.append(namespace.getValue());
            sb.append(":");
        }

        if(name != null)
        {
            sb.append(name.getValue());
            sb.append("=");
        }

        if(string instanceof StringReference)
        {
            sb.append("\"");
            sb.append(((StringReference)string).getValue());
            sb.append("\"");
        }
        else if(resource instanceof ResourceReference)
        {
            sb.append("\"");
            sb.append(((ResourceReference)resource).getValue());
            sb.append("\"");
        }
        else
        {
            sb.append("\"\"");
        }

        sb.append(String.format("(0x0%s)", type));

        return sb.toString();
    }
}
