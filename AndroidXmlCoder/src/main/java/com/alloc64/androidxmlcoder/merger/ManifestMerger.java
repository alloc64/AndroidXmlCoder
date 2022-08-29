/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner                                   *
 * Distributed under the MIT software license, see the accompanying    *
 * file COPYING or https://www.opensource.org/licenses/mit-license.php.*
 ***********************************************************************/

package com.alloc64.androidxmlcoder.merger;

import com.umeng.editor.android.AndroidManifestAXML;
import com.umeng.editor.decode.nodes.Attribute;
import com.umeng.editor.decode.nodes.BTagNode;
import com.umeng.editor.decode.nodes.BXMLNode;
import com.umeng.editor.decode.visitors.XMLVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ManifestMerger
{
    private static final String ACTIVITY = "activity";
    private static final String SERVICE = "service";
    private static final String RECEIVER = "receiver";
    private static final String META_DATA = "meta-data";
    private static final String PROVIDER = "provider";
    private static final String USES_PERMISSION = "uses-permission";

    private static final String REPLACED_PACKAGE_NAME = "com.android.test";

    private Set<String> mergedTags = new HashSet<>(Arrays.asList(
            ACTIVITY, SERVICE, RECEIVER, META_DATA, USES_PERMISSION, PROVIDER
    ));

    private AndroidManifestAXML inputManifest;
    private AndroidManifestAXML mergedManifest;

    private XMLVisitor xmlVisitor = new XMLVisitor();

    public ManifestMerger(AndroidManifestAXML inputManifest, AndroidManifestAXML mergedManifest)
    {
        this.inputManifest = inputManifest;
        this.mergedManifest = mergedManifest;
    }

    public void merge(File outputManifestFile) throws Exception
    {
        BXMLNode mergedManifestNode = mergedManifest.getManifestNode();
        BXMLNode mergedApplicationNode =  mergedManifest.getApplicationNode();

        // odstranime firebase messaging intent-filtry a jejich sluzby
        removeFirebaseMessagingNodes(inputManifest.getApplicationNode());

        String packageName = inputManifest.getPackageName();

        // mergneme chybejici resource Ids (name, value, authorities atd... jinak android nedokaze nainstalovat APK)
        ResourceMerger resourceMerger = new ResourceMerger(inputManifest, mergedManifest);
        resourceMerger.merge();

        // projde rekurzivne XML a mergne data s inputStringBlock a inputDoc
        NodeAttributeResolver nodeMerger = new NodeAttributeResolver(inputManifest);
        nodeMerger.resolveAttributes(mergedManifest);

        // zmergujeme permissions
        mergePermissions(inputManifest.getManifestNode(), mergedManifestNode.getChildren());

        // zmergujeme activities a ostatni tagy
        mergeApplicationNodes(inputManifest.getApplicationNode(), mergedApplicationNode.getChildren());

        // fixneme permissions & content providery
        fixAttributeWithInjectedPackageName(packageName, inputManifest.getManifestNode(), USES_PERMISSION, "name");
        fixAttributeWithInjectedPackageName(packageName, inputManifest.getApplicationNode(), PROVIDER, "authorities");

        inputManifest.build(new FileOutputStream(outputManifestFile));

        verify(outputManifestFile);
    }

    private void mergePermissions(BXMLNode inputManifestNode, List<BXMLNode> mergedManifestChildren)
    {
        for (BXMLNode n : mergedManifestChildren)
        {
            if(n instanceof BTagNode)
            {
                BTagNode node = ((BTagNode) n);
                String name = node.getName();

                if(!nodeExists(inputManifestNode.getChildren(), node))
                {
                    if(USES_PERMISSION.equals(name))
                    {
                        node.setRoot(inputManifestNode.getRoot());

                        // System.out.println("Merged manifest node: ");
                        //xmlVisitor.visit(node);

                        inputManifestNode.getChildren().add(1, node);
                    }
                }
            }
        }
    }

    private void mergeApplicationNodes(BXMLNode inputApplicationNode, List<BXMLNode> mergedManifestChildren)
    {
        for (BXMLNode n : mergedManifestChildren)
        {
            if(n instanceof BTagNode)
            {
                BTagNode node = ((BTagNode) n);
                String name = node.getName();

                if(!nodeExists(inputApplicationNode.getChildren(), node))
                {
                    if(mergedTags.contains(name))
                    {
                        node.setRoot(inputApplicationNode.getRoot());

                        // System.out.println("Merged manifest node: ");
                        // xmlVisitor.visit(node);

                        inputApplicationNode.getChildren().add(1, node);
                    }
                }
            }
        }
    }

    private void fixAttributeWithInjectedPackageName(String inputPackageName, BXMLNode elem, String tag, String attribute)
    {
        for(BXMLNode n : elem.getChildren())
        {
            if(n instanceof BTagNode)
            {
                BTagNode node = (BTagNode) n;

                String name = node.getName();

                if(tag.equals(name))
                {
                    Attribute attr = node.getAttribute(attribute);

                    if(attr != null)
                    {
                        String value = attr.getString();

                        if(value != null && value.startsWith(REPLACED_PACKAGE_NAME))
                        {
                            String originalValue = value;

                            value = value.replace(REPLACED_PACKAGE_NAME, inputPackageName);

                            System.out.println("Replaced " + tag + " " + attribute + ": " + originalValue + " to: " + value);

                            attr.getStringReference().setValue(value);
                        }
                    }
                }
            }
        }
    }

    private void removeFirebaseMessagingNodes(BXMLNode applicationNode)
    {
        List<BTagNode> list = applicationNode.findTags("service", "provider");

        if(list == null || list.size() < 1)
            return;

        Set<String> targetActions = new LinkedHashSet<>();
        targetActions.add("com.google.firebase.INSTANCE_ID_EVENT");
        targetActions.add("com.google.firebase.MESSAGING_EVENT");

        /*
        Set<String> targetComponents = new LinkedHashSet<>();
        targetComponents.add("com.google.firebase.components:com.google.firebase.inappmessaging.display.FirebaseInAppMessagingDisplayRegistrar");
        targetComponents.add("com.google.firebase.components:com.google.firebase.messaging.FirebaseMessagingRegistrar");
        targetComponents.add("com.google.firebase.components:com.google.firebase.inappmessaging.FirebaseInAppMessagingRegistrar");
        targetComponents.add("com.google.firebase.components:com.google.firebase.analytics.connector.internal.AnalyticsConnectorRegistrar");
        targetComponents.add("com.google.firebase.components:com.google.firebase.iid.Registrar");
        */

        Set<BTagNode> tagsToRemove = new LinkedHashSet<>();

        for(BTagNode n : list)
        {
            List<BTagNode> intentFilters = n.findTag("intent-filter");

            if(intentFilters != null && intentFilters.size() > 0)
            {
                for(BTagNode intent : intentFilters)
                {
                    List<BTagNode> actions = intent.findTag("action");

                    if(actions != null && actions.size() > 0)
                    {
                        for(BTagNode action : actions)
                        {
                            Attribute nameAttr = action.getAttribute(AndroidManifestAXML.NAME);

                            if(nameAttr != null && targetActions.contains(nameAttr.getString()))
                            {
                                tagsToRemove.add(n);
                            }
                        }
                    }
                }
            }

            /*
            appky maji na classes vazby a pada to - nutno killnout pres bytecode
            if("service".equals(n.getName()))
            {
                List<BTagNode> metadataList = n.findTag("meta-data");

                if(metadataList != null && metadataList.size() > 0)
                {
                    for(BTagNode metadata : metadataList)
                    {
                        Attribute nameAttr = metadata.getAttribute(AndroidManifestAXML.NAME);

                        if(nameAttr != null)
                        {
                            String name = nameAttr.getString();

                            if(targetComponents.contains(name))
                            {
                                n.getChildren().remove(metadata);
                                System.out.printf("Removed firebase component %s from discovery\n", name);
                            }
                        }
                    }
                }
            }
            */
        }

        if(tagsToRemove.size() < 1)
            return;

        for(BTagNode node : tagsToRemove)
        {
            Attribute nameAttr = node.getAttribute(AndroidManifestAXML.NAME);

            if(nameAttr != null)
                System.out.printf("Removing firebase messaging node <%s name=\"%s\" />\n", node.getName(), nameAttr.getString());

            applicationNode.getChildren().remove(node);
        }
    }

    private boolean nodeExists(List<BXMLNode> nodeList, BTagNode checkedNode)
    {
        if(nodeList == null || nodeList.size() < 1)
            return false;

        for(BXMLNode n : nodeList)
        {
            if(n instanceof BTagNode)
            {
                BTagNode thizNode = ((BTagNode) n);

                Attribute thizNameAttr = thizNode.getAttribute("name");
                Attribute checkedNameAttr = checkedNode.getAttribute("name");

                if(thizNameAttr != null && checkedNameAttr != null)
                {
                    String thizVal = thizNameAttr.getString();
                    String checkedVal = checkedNameAttr.getString();

                    if(Objects.equals(thizVal, checkedVal))
                        return true;
                }
            }
        }

        return false;
    }

    private void verify(File file) throws Exception
    {
        AndroidManifestAXML doc = new AndroidManifestAXML();
        doc.parse(new FileInputStream(file));

        //doc.print();

        System.out.println("Manifest merged: " + file);
    }
}
