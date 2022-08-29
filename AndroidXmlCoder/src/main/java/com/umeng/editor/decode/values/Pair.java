/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.values;

public class Pair<T1, T2>
{
    public T1 first;
    public T2 second;

    public Pair(T1 t1, T2 t2)
    {
        this.first = t1;
        this.second = t2;
    }

    public Pair()
    {
    }
}
