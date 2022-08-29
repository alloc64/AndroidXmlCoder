/**
 * Copyright 2011 Ryszard Wiśniewski <brut.alll@gmail.com>
 * Copyright 2019 Milan Jaitner
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.umeng.editor.decode.blocks;

import com.umeng.editor.decode.coders.IAXMLSerialize;
import com.umeng.editor.decode.coders.IntReader;
import com.umeng.editor.decode.coders.IntWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StringBlock implements IAXMLSerialize
{
    private static final int TAG = 0x001C0001;
    private static final int INT_SIZE = 4;

    private int chunkSize;
    private int stringsCount;
    private int stylesCount;
    private int encodingType;

    private int stringBlockOffset;
    private int stylesBlockOffset;

    private int[] perStringOffset;
    private int[] perStyleOffset;

    private List<String> strings;

    // android can identify HTML tags in a string，all the styles are kept here
    private List<Style> styles;

    public int getStringIdOrCreate(String str)
    {
        if(str == null)
            return -1;

        if (containsString(str))
            return getStringId(str);

        return addString(str);
    }

    private int getStringId(String string) //TODO: reindex lookup into hashmap
    {
        int size = strings.size();
        for (int i = 0; i < size; i++)
            if (strings.get(i).equals(string))
                return i;

        return -1;
    }

    private int addString(String str)
    {
        strings.add(str);
        return (strings.size() - 1);
    }

    private String setString(int index, String str)
    {
        return strings.set(index, str);
    }

    private boolean containsString(String str)
    {
        return strings.contains(str);
    }

    public List<String> getStringList()
    {
        return strings;
    }

    /**
     *
     * Reads whole (including chunk type) string block from stream.
     * Stream must be at the chunk type.
     * @param reader
     * @throws IOException
     */
    public void read(IntReader reader) throws IOException
    {
        this.chunkSize = reader.readInt();
        this.stringsCount = reader.readInt();
        this.stylesCount = reader.readInt();

        this.encodingType = reader.readInt(); // utf-8 or uft16

        this.stringBlockOffset = reader.readInt();
        this.stylesBlockOffset = reader.readInt();

        if (stringsCount > 0)
        {
            perStringOffset = reader.readIntArray(stringsCount);
            this.strings = new ArrayList<>(stringsCount);
        }

        if (stylesCount > 0)
        {
            perStyleOffset = reader.readIntArray(stylesCount);
            this.styles = new ArrayList<>();
        }

        if (stringsCount > 0)
        {
            int size = ((stylesBlockOffset == 0) ? chunkSize : stylesBlockOffset) - stringBlockOffset;
            byte[] rawStrings = reader.readByteArray(size);

            for (int i = 0; i < stringsCount; i++)
            {
                int offset = perStringOffset[i];
                short len = toShort(rawStrings[offset], rawStrings[offset + 1]);
                String string = new String(rawStrings, offset + 2, len * 2, Charset.forName("UTF-16LE"));

                strings.add(i, string);
            }
        }

        if (stylesCount > 0)
        {
            int size = chunkSize - stylesBlockOffset;
            int[] styles = reader.readIntArray(size / 4);

            for (int i = 0; i < stylesCount; i++)
            {
                int offset = perStyleOffset[i];
                int j = offset;
                for (; j < styles.length; j++)
                {
                    if (styles[j] == -1) break;
                }

                int[] array = new int[j - offset];
                System.arraycopy(styles, offset, array, 0, array.length);
                Style style = Style.parse(array);

                this.styles.add(style);
            }
        }
    }

    @Override
    public void write(IntWriter writer) throws IOException
    {
        //base seven
        int size = 0;
        size += writer.writeInt(TAG);
        size += writer.writeInt(chunkSize);
        size += writer.writeInt(stringsCount);
        size += writer.writeInt(stylesCount);
        size += writer.writeInt(encodingType);
        size += writer.writeInt(stringBlockOffset);
        size += writer.writeInt(stylesBlockOffset);

        if (perStringOffset != null)
            for (int offset : perStringOffset)
                size += writer.writeInt(offset);

        if (perStyleOffset != null)
            for (int offset : perStyleOffset)
                size += writer.writeInt(offset);

        if (strings != null)
        {
            for (String s : strings)
            {
                byte[] raw = s.getBytes("UTF-16LE");
                size += writer.writeShort((short) (s.length()));
                size += writer.writeByteArray(raw);
                size += writer.writeShort((short) 0);
            }
        }

        if (styles != null)
            for (Style style : styles)
                size += style.write(writer);

        if (chunkSize > size)
            writer.writeShort((short) 0);
    }

    public void prepare() throws IOException
    {
        this.stringsCount = strings == null ? 0 : strings.size();
        this.stylesCount = styles == null ? 0 : styles.size();

        //string & style block offset
        int base = INT_SIZE * 7;//from 0 to string array

        int strSize = 0;
        int[] perStrSize = null;

        if (strings != null)
        {
            int size = 0;
            perStrSize = new int[strings.size()];
            for (int i = 0; i < strings.size(); i++)
            {
                perStrSize[i] = size;

                size += 2 + strings.get(i).getBytes(StandardCharsets.UTF_16LE).length + 2;
            }

            strSize = size;
        }

        int styleSize = 0;
        int[] perStyleSize = null;
        if (styles != null)
        {
            int size = 0;
            perStyleSize = new int[styles.size()];

            for (int i = 0; i < styles.size(); i++)
            {
                perStyleSize[i] = size;
                size += styles.get(i).getSize();
            }

            styleSize = size;
        }

        int stringArraySize = perStrSize == null ? 0 : perStrSize.length * INT_SIZE;
        int styleArraySize = perStyleSize == null ? 0 : perStyleSize.length * INT_SIZE;

        if (strings != null && strings.size() > 0)
        {
            this.stringBlockOffset = base + stringArraySize + styleArraySize;
            this.perStringOffset = perStrSize;
        }
        else
        {
            this.stringBlockOffset = 0;
            this.perStringOffset = null;
        }

        if (styles != null && styles.size() > 0)
        {
            this.stylesBlockOffset = base + stringArraySize + styleArraySize + strSize;
            this.perStyleOffset = perStyleSize;
        }
        else
        {
            this.stylesBlockOffset = 0;
            this.perStyleOffset = null;
        }

        this.chunkSize = base + stringArraySize + styleArraySize + strSize + styleSize;

        int align = chunkSize % 4;
        if (align != 0)
            this.chunkSize += (INT_SIZE - align);
    }

    public int getSize()
    {
        return chunkSize;
    }

    public String getStringAt(int index)
    {
        if(index < 0 || index >= strings.size())
            return null;

        return strings.get(index);
    }

    private short toShort(short byte1, short byte2)
    {
        return (short) ((byte2 << 8) + byte1);
    }

    public Style getStyle(int index)
    {
        return styles.get(index);
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

    public static class Style
    {
        List<Decorator> decoratorList;

        public Style()
        {
            this.decoratorList = new ArrayList<>();
        }

        public List<Decorator> getDecoratorList()
        {
            return decoratorList;
        }

        public void addStyle(Decorator style)
        {
            decoratorList.add(style);
        }

        public int getSize()
        {
            int size = 0;
            size += getCount() * Decorator.SIZE;
            size += 1;//[-1] as a seperator
            return size;
        }

        public int getCount()
        {
            return decoratorList.size();
        }

        public static Style parse(int[] data) throws IOException
        {
            if (data == null || (data.length % Decorator.SIZE != 0))
                throw new IOException("Fail to parse style");

            Style d = new Style();

            Decorator decorator = null;
            for (int i = 0; i < data.length; i++)
            {
                if (i % Decorator.SIZE == 0)
                    new Decorator(); //TODO: toto je typo / chyba

                switch (i % 3)
                {
                    case 0:
                        decorator = new Decorator();
                        decorator.tag = data[i];
                    break;

                    case 1:
                        decorator.start = data[i];
                    break;

                    case 2:
                        decorator.end = data[i];
                        d.decoratorList.add(decorator);
                    break;
                }
            }

            return d;
        }

        public int write(IntWriter writer) throws IOException
        {
            int size = 0;

            if (decoratorList != null && decoratorList.size() > 0)
            {
                for (Decorator decorator : decoratorList)
                    size += decorator.write(writer);

                size += writer.writeInt(-1);
            }

            return size;
        }
    }

    public static class Decorator
    {
        public static final int SIZE = 3;

        public int tag;
        public int start;
        public int end;

        public Decorator()
        {
        }

        public int write(IntWriter writer) throws IOException
        {
            int size = 0;

            size += writer.writeInt(tag);
            size += writer.writeInt(start);
            size += writer.writeInt(end);

            return size;
        }
    }
}