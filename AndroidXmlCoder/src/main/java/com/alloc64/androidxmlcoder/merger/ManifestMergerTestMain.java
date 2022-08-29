/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.androidxmlcoder.merger;

import com.umeng.editor.android.AndroidManifestAXML;

import java.io.File;
import java.io.FileInputStream;

public class ManifestMergerTestMain
{
    public static void main(String[] args) throws Exception
    {
        try
        {
            AndroidManifestAXML input = new AndroidManifestAXML();
            input.parse(new FileInputStream("input-AndroidManifest.xml"));

            AndroidManifestAXML merged = new AndroidManifestAXML();
            merged.parse(new FileInputStream("merged-AndroidManifest.xml"));

            File output = new File("output-AndroidManifest.xml");

            ManifestMerger merger = new ManifestMerger(input, merged);
            merger.merge(output);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
