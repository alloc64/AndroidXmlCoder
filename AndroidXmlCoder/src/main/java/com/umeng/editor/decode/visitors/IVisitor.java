/***********************************************************************
 * Copyright (c) 2014 https://github.com/ntop001/AXMLEditor
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.visitors;

import com.umeng.editor.decode.nodes.BNSNode;
import com.umeng.editor.decode.nodes.BTXTNode;
import com.umeng.editor.decode.nodes.BTagNode;

public interface IVisitor
{
    void visit(BNSNode node);

    void visit(BTagNode node);

    void visit(BTXTNode node);
}
