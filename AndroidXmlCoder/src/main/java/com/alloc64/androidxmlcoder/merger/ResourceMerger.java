/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.androidxmlcoder.merger;

import com.umeng.editor.android.AndroidManifestAXML;
import com.umeng.editor.decode.blocks.ResBlock;
import com.umeng.editor.decode.values.Resources;

import java.util.List;

public class ResourceMerger
{
    private final AndroidManifestAXML inputManifest;
    private final AndroidManifestAXML mergedManifest;

    public ResourceMerger(AndroidManifestAXML inputManifest, AndroidManifestAXML mergedManifest)
    {
        this.inputManifest = inputManifest;
        this.mergedManifest = mergedManifest;
    }

    public void merge()
    {
        List<Integer> inputResourceIds = inputManifest.getResBlock()
                .getResourceIdList();

        ResBlock mergedManifestResBlock = mergedManifest.getResBlock();

        List<Integer> mergedResourceIds = mergedManifestResBlock
                .getResourceIdList();

        if(mergedResourceIds != null && mergedResourceIds.size() > 0)
        {
            for(int resourceId : mergedResourceIds)
            {
                if(!inputResourceIds.contains(resourceId))
                {
                    System.out.printf("Merged missing resource: %s (0x0%X)\n", Resources.Attributes.get().getAttributeName(resourceId), resourceId);

                    inputResourceIds.add(resourceId);
                }
            }
        }

        System.out.println("Finished merging resources.");
    }
}
