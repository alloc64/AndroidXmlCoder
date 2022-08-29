/***********************************************************************
 * Copyright (c) 2014 https://github.com/ntop001/AXMLEditor
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.blocks;

import com.umeng.editor.decode.coders.IAXMLSerialize;
import com.umeng.editor.decode.coders.IntReader;
import com.umeng.editor.decode.coders.IntWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ResBlock implements IAXMLSerialize
{
    private static final int TAG = 0x00080180;
    private static final int INT_SIZE = 4;

    private int chunkSize;
    private List<Integer> resourceIdList = new ArrayList<>();

    public void print()
    {
        StringBuilder sb = new StringBuilder();

        for (int id : getResourceIdList())
        {
            sb.append(id);
            sb.append(" ");
        }

        System.out.println(sb.toString());
    }

    public void read(IntReader reader) throws IOException
    {
        this.chunkSize = reader.readInt();

        if (chunkSize < 8 || (chunkSize % 4) != 0)
            throw new IOException("Invalid resource ids size (" + chunkSize + ").");

        int[] resIdsArray = reader.readIntArray(chunkSize / 4 - 2); //subtract base offset (type + size)

        if(resIdsArray != null)
            for(int resourceId : resIdsArray)
                resourceIdList.add(resourceId);
    }

    public void prepare()
    {
        int base = 2 * INT_SIZE;
        int resSize = resourceIdList == null ? 0 : resourceIdList.size() * INT_SIZE;

        this.chunkSize = base + resSize;
    }

    @Override
    public void write(IntWriter writer) throws IOException
    {
        writer.writeInt(TAG);
        writer.writeInt(chunkSize);

        if (resourceIdList != null)
            for (int id : resourceIdList)
                writer.writeInt(id);
    }

    public List<Integer> getResourceIdList()
    {
        return resourceIdList;
    }

    public int getResourceAt(int index)
    {
        if(index < 0 || index >= resourceIdList.size()-1)
            return -1;

        return resourceIdList.get(index);
    }

    public int getResourceId(int value)
    {
        for (int i = 0; i < resourceIdList.size(); i++) //TODO: toto by chtelo reindexovat, je to pooooooooomale
        {
            int resId = resourceIdList.get(i);
            if (resId == value)
                return i;
        }

        return -1;
    }

    @Override
    public int getSize()
    {
        return chunkSize;
    }

    @Override
    public int getType()
    {
        return TAG;
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
