package com.umeng.editor.decode.coders;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IntWriter
{
    private OutputStream stream;
    private boolean isBigEndian;
    private int position;

    private ByteBuffer shortByteBuffer = ByteBuffer.allocate(2);
    private ByteBuffer intByteBuffer = ByteBuffer.allocate(4);

    public IntWriter()
    {
    }

    public IntWriter(OutputStream stream, boolean bigEndian)
    {
        reset(stream, bigEndian);
    }

    public final void reset(OutputStream stream, boolean bigEndian)
    {
        this.stream = stream;
        isBigEndian = bigEndian;
        position = 0;

        ByteOrder order = isBigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        shortByteBuffer.order(order);
        intByteBuffer.order(order);
    }

    public final void close()
    {
        if (stream == null)
            return;

        try
        {
            stream.flush();
            stream.close();
        }
        catch (IOException e)
        {
        }

        reset(null, false);
    }

    public final OutputStream getStream()
    {
        return stream;
    }

    public final boolean isBigEndian()
    {
        return isBigEndian;
    }

    public final void setBigEndian(boolean bigEndian)
    {
        isBigEndian = bigEndian;
    }

    public final void writeByte(byte b) throws IOException
    {
        stream.write(b);
        position += 1;
    }

    public final int writeShort(short s) throws IOException
    {
        shortByteBuffer.clear();
        shortByteBuffer.putShort(s);

        stream.write(shortByteBuffer.array());
        position += 2;

        return 2;
    }

    public final int writeInt(int i) throws IOException
    {
        intByteBuffer.clear();
        intByteBuffer.putInt(i);

        stream.write(intByteBuffer.array());
        position += 4;

        return 4;
    }

    public final void writeIntArray(int[] array) throws IOException
    {
        for (int i : array)
            writeInt(i);
    }

    public final void writeIntArray(int[] array, int offset, int length) throws IOException
    {
        int limit = offset + length;
        for (int i = offset; i < limit; i++)
        {
            writeInt(i);
        }
    }

    public final int writeByteArray(byte[] array) throws IOException
    {
        stream.write(array);
        position += array.length;

        return array.length;
    }

    public final void skip(int n, byte def) throws IOException
    {
        for (int i = 0; i < n; i++)
            stream.write(def);

        position += n;
    }

    public final void skipIntFFFF() throws IOException
    {
        writeInt(Integer.MAX_VALUE);
    }

    public final void skipInt0000() throws IOException
    {
        writeInt(0);
    }

    public final int getPosition()
    {
        return position;
    }
}
