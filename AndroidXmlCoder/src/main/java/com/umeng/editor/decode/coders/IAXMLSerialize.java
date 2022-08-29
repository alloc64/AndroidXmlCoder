/***********************************************************************
 * Copyright (c) 2014 https://github.com/ntop001/AXMLEditor
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.coders;

import java.io.IOException;

public interface IAXMLSerialize
{
    int getSize();

    int getType();

    void setSize(int size);

    void setType(int type);

    void read(IntReader reader) throws IOException;

    void write(IntWriter writer) throws IOException;
}
