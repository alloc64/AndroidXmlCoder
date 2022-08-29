/***********************************************************************
 * Copyright (c) 2014 https://github.com/ntop001/AXMLEditor
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.visitors;

import com.umeng.editor.decode.nodes.Attribute;
import com.umeng.editor.decode.nodes.BNSNode;
import com.umeng.editor.decode.nodes.BTXTNode;
import com.umeng.editor.decode.nodes.BTagNode;
import com.umeng.editor.decode.nodes.BXMLNode;
import com.umeng.editor.decode.values.TypedValue;

import org.apache.commons.lang3.StringUtils;

public class XMLVisitor implements IVisitor
{
    private static final float RADIX_MULTS[] = { 0.00390625F, 3.051758E-005F, 1.192093E-007F, 4.656613E-010F };
    private static final String DIMENSION_UNITS[] = { "px", "dip", "sp", "pt", "in", "mm", "", "" };
    private static final String FRACTION_UNITS[] = { "%", "%p", "", "", "", "", "", "" };

    private static final String intent = "                                ";
    private static final int step = 4;

    private int depth;

    @Override
    public void visit(BNSNode node)
    {
        String namespace = String.format("xmlns:%s=%s", node.getPrefix(), node.getUri());

        System.out.println(namespace);

        if (node.hasChild())
            for (BXMLNode child : node.getChildren())
                child.accept(this);
    }

    @Override
    public void visit(BTagNode node)
    {
        String name = node.getName();

        if (!node.hasChild())
        {
            print("<" + name);
            printAttribute(node.getAttributes());
            print("/>");
        }
        else
        {
            print("<" + name);
            depth++;
            printAttribute(node.getAttributes());
            print(">");

            for (BXMLNode child : node.getChildren())
                child.accept(this);

            depth--;
            print("</" + name + ">");
        }
    }

    public void visit(BTXTNode node)
    {
        print("Text node");
    }

    private void printAttribute(Attribute[] attrs)
    {
        for (Attribute attr : attrs)
        {
            StringBuilder sb = new StringBuilder();

            boolean hasNamespace = !StringUtils.isEmpty(attr.getNamespace());

            if (hasNamespace)
                sb.append("android").append(':');

            String name = attr.getName();

            if ("id".equals(name))
                System.out.println("hehe");

            sb.append(name).append('=');
            sb.append('\"').append(getAttributeValue(attr)).append('\"');

            print(sb.toString());
        }
    }

    private void print(String str)
    {
        System.out.println(intent.substring(0, depth * step) + str);
    }

    private String getAttributeValue(Attribute attr)
    {
        int type = attr.getType() >> 24;
        int data = attr.getResourceRaw();

        if (type == TypedValue.TYPE_STRING)
            return attr.getString();

        if (type == TypedValue.TYPE_ATTRIBUTE)
            return String.format("?%s%08X", getPackage(data), data);

        if (type == TypedValue.TYPE_REFERENCE)
            return String.format("@%s%08X", getPackage(data), data);

        if (type == TypedValue.TYPE_FLOAT)
            return String.valueOf(Float.intBitsToFloat(data));

        if (type == TypedValue.TYPE_INT_HEX)
            return String.format("0x%08X", data);

        if (type == TypedValue.TYPE_INT_BOOLEAN)
            return data != 0 ? "true" : "false";

        if (type == TypedValue.TYPE_DIMENSION)
            return Float.toString(complexToFloat(data)) + DIMENSION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];

        if (type == TypedValue.TYPE_FRACTION)
            return Float.toString(complexToFloat(data)) + FRACTION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];

        if (type >= TypedValue.TYPE_FIRST_COLOR_INT && type <= TypedValue.TYPE_LAST_COLOR_INT)
            return String.format("#%08X", data);

        if (type >= TypedValue.TYPE_FIRST_INT && type <= TypedValue.TYPE_LAST_INT)
            return String.valueOf(data);

        return String.format("<0x%X, type 0x%02X>", data, type);
    }

    private String getPackage(int id)
    {
        if (id >>> 24 == 1)
            return "android:";

        return "";
    }

    private float complexToFloat(int complex)
    {
        return (float) (complex & 0xFFFFFF00) * RADIX_MULTS[(complex >> 4) & 3];
    }
}
