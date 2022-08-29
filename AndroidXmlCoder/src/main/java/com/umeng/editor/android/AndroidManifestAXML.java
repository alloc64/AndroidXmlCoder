/***********************************************************************
 * Copyright (c) 2014 https://github.com/ntop001/AXMLEditor
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.android;

import com.umeng.editor.decode.AXMLDocument;
import com.umeng.editor.decode.nodes.Attribute;
import com.umeng.editor.decode.nodes.BTagNode;
import com.umeng.editor.decode.nodes.BXMLNode;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AndroidManifestAXML extends AXMLDocument
{
    public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    public static final String MANIFEST = "manifest";
    public static final String USES_SDK = "uses-sdk";
    public static final String APPLICATION = "application";
    public static final String META_DATA = "meta-data";
    public static final String ACTIVITY = "activity";
    public static final String ACTIVITY_ALIAS = "activity-alias";
    public static final String INTENT_FILTER = "intent-filter";
    public static final String NAME = "name";
    public static final String LABEL = "label";
    public static final String VALUE = "value";
    public static final String ACTION = "action";
    public static final String ACTION_MAIN = "android.intent.action.MAIN";
    public static final String TARGET_ACTIVITY = "targetActivity";
    public static final String TARGET_SDK_VERSION = "targetSdkVersion";
    public static final String MIN_SDK_VERSION = "minSdkVersion";
    public static final String PACKAGE = "package";

    public BXMLNode getManifestNode()
    {
        return getTagIn(MANIFEST, xmlTree.getRoot());
    }

    public BXMLNode getApplicationNode()
    {
        return getTagIn(APPLICATION, getManifestNode());
    }

    public BXMLNode getUsesSdkNode()
    {
        return getTagIn(USES_SDK, getManifestNode());
    }

    public String getPackageName()
    {
        BTagNode manifestNode = (BTagNode)getManifestNode();

        Attribute packageAttr = manifestNode.getAttribute(PACKAGE);

        if(packageAttr != null)
            return packageAttr.getString();

        return null;
    }

    public String getMainActivity()
    {
        List<BTagNode> activities = getActivities();

        if (activities.size() < 1)
            return null;

        List<BXMLNode> children = getApplicationNode().getChildren();

        if(children == null)
            return null;

        for (BXMLNode n : children)
        {
            if (n instanceof BTagNode)
            {
                BTagNode node = (BTagNode) n;
                String name = node.getName();

                if(ACTIVITY.equals(name))
                {
                    List<BTagNode> intentFilters = getTagsIn(INTENT_FILTER, node);

                    for (BTagNode intentFilter : intentFilters)
                    {
                        List<BTagNode> actions = getTagsIn(ACTION, intentFilter);

                        for (BTagNode action : actions)
                        {
                            Attribute nameAttr = action.getAttribute(NAME);

                            if (nameAttr != null && ACTION_MAIN.equals(nameAttr.getString()))
                            {
                                Attribute classNameAttr = node.getAttribute(NAME);

                                if (classNameAttr != null)
                                    return classNameAttr.getString();
                            }
                        }
                    }
                }
                else if(ACTIVITY_ALIAS.equals(name))
                {
                    Attribute targetActivityAttr = node.getAttribute(TARGET_ACTIVITY);

                    if(targetActivityAttr != null)
                        return targetActivityAttr.getString();
                }
            }
        }

        return null;
    }

    public List<BTagNode> getActivities()
    {
        return getApplicationNodes(ACTIVITY);
    }

    private List<BTagNode> getApplicationNodes(String name)
    {
        return getTagsIn(name, getApplicationNode());
    }

    public List<String> getAllClassNames()
    {
        List<String> result = new ArrayList<>();

        for(BXMLNode n : getApplicationNode().getChildren())
        {
            if(n instanceof BTagNode)
            {
                BTagNode node = ((BTagNode) n);

                if(!META_DATA.equals(node.getName()))
                {
                    Attribute classNameAttr = node.getAttribute(NAME);

                    if(classNameAttr != null)
                    {
                        String className = classNameAttr.getString();

                        if(!StringUtils.isEmpty(className))
                            result.add(className);
                    }
                }
            }
        }

        return result;
    }

    public void getMetadata(MetadataTraverserCallback callback)
    {
        List<BXMLNode> children = getApplicationNode().getChildren();

        if(children != null)
        {
            for (int i = 0; i < children.size(); i++)
            {
                BXMLNode n = children.get(i);
                if (n instanceof BTagNode)
                {
                    BTagNode node = (BTagNode) n;

                    if (node.getName().equals(META_DATA))
                    {
                        Attribute nameAttr = node.getAttribute(NAME);
                        Attribute valueAttr = node.getAttribute(VALUE);

                        if (nameAttr != null && valueAttr != null)
                        {
                            String name = nameAttr.getString();
                            String value = valueAttr.getString();

                            if (callback != null)
                            {
                                boolean retain = callback.process(name, value);

                                if (!retain)
                                    children.remove(i);
                            }
                        }
                    }
                }
            }
        }
    }

    public Attribute getMinSdkVersionAttribute()
    {
        BTagNode usesSdkNode = (BTagNode) getUsesSdkNode();

        if (usesSdkNode != null)
            return usesSdkNode.getAttribute(MIN_SDK_VERSION);

        return null;
    }

    public Attribute getTargetSdkVersionAttribute()
    {
        BTagNode usesSdkNode = (BTagNode) getUsesSdkNode();

        if (usesSdkNode != null)
            return usesSdkNode.getAttribute(TARGET_SDK_VERSION);

        return null;
    }

    public Integer getMinSdkVersion()
    {
        Attribute attr = getMinSdkVersionAttribute();

        if(attr != null)
            return attr.getResourceRaw();

        return null;
    }

    public void setMinSdkVersion(int minSdkVersion)
    {
        Attribute attr = getMinSdkVersionAttribute();

        if(attr != null)
            attr.setResourceValue(minSdkVersion);
    }

    public Integer getTargetSdk()
    {
        Attribute attr = getTargetSdkVersionAttribute();

        if(attr != null)
            return attr.getResourceRaw();

        return null;
    }

    public void setTargetSdk(int targetSdk)
    {
        Attribute attr = getTargetSdkVersionAttribute();

        if(attr != null)
            attr.setResourceValue(targetSdk);
    }

    public Attribute getApplicationNameAttribute()
    {
        BTagNode applicationNode = (BTagNode) getApplicationNode();

        return applicationNode.getAttribute(NAME);
    }

    public void setApplicationClass(String clazz)
    {
        if(StringUtils.isEmpty(clazz))
            return;

        ((BTagNode) getApplicationNode()).setAttribute(NAME, clazz);
    }

    public String getApplicationClass()
    {
        Attribute nameAttr = getApplicationNameAttribute();

        if(nameAttr != null)
            return nameAttr.getString();

        return null;
    }

    public Attribute getApplicationLabelAttribute()
    {
        BTagNode applicationNode = (BTagNode) getApplicationNode();

        return applicationNode.getAttribute(LABEL);
    }

    public String getApplicationLabel()
    {
        Attribute labelAttr = getApplicationLabelAttribute();

        if(labelAttr != null)
            return labelAttr.getString();

        return null;
    }

    public void setApplicationLabel(String label)
    {
        if(StringUtils.isEmpty(label))
            return;

        ((BTagNode) getApplicationNode()).setAttribute(LABEL, label);
    }
}
