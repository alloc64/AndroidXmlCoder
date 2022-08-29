/***********************************************************************
 * Copyright (c) 2019 Milan Jaitner
 * Distributed under the GPLv2 software license, see the accompanying
 * file COPYING or https://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 ***********************************************************************/

package com.umeng.editor.decode.values;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Resources
{
    public static class Attributes
    {
        private static Attributes instance;

        public static synchronized Attributes get()
        {
            if(instance == null)
                instance = new Attributes();

            return instance;
        }

        private Map<String, Integer> attributesByName = new HashMap<>();
        private Map<Integer, String> attributesById = new HashMap<>();

        private Map<Integer, Integer> prioritizedResourceIds = new LinkedHashMap<>();
        private Map<String, Integer> prioritizedResourceNames = new LinkedHashMap<>();

        private Attributes()
        {
            addAttribute("theme", 0x01010000);
            addAttribute("label", 0x01010001);
            addAttribute("icon", 0x01010002);
            addAttribute("name", 0x01010003);
            addAttribute("permission", 0x01010006);
            addAttribute("exported", 0x01010010);
            addAttribute("grantUriPermissions", 0x0101001b);
            addAttribute("resource", 0x01010025);
            addAttribute("debuggable", 0x0101000f);
            addAttribute("value", 0x01010024);
            addAttribute("versionCode", 0x0101021b);
            addAttribute("versionName", 0x0101021c);
            addAttribute("screenOrientation", 0x0101001e);
            addAttribute("minSdkVersion", 0x0101020c);
            addAttribute("maxSdkVersion", 0x01010271);
            addAttribute("reqTouchScreen", 0x01010227);
            addAttribute("reqKeyboardType", 0x01010228);
            addAttribute("reqHardKeyboard", 0x01010229);
            addAttribute("reqNavigation", 0x0101022a);
            addAttribute("reqFiveWayNav", 0x01010232);
            addAttribute("targetSdkVersion", 0x01010270);
            addAttribute("testOnly", 0x01010272);
            addAttribute("anyDensity", 0x0101026c);
            addAttribute("glEsVersion", 0x01010281);
            addAttribute("smallScreens", 0x01010284);
            addAttribute("normalScreens", 0x01010285);
            addAttribute("largeScreens", 0x01010286);
            addAttribute("xlargeScreens", 0x010102bf);
            addAttribute("required", 0x0101028e);
            addAttribute("installLocation", 0x010102b7);
            addAttribute("screenSize", 0x010102ca);
            addAttribute("screenDensity", 0x010102cb);
            addAttribute("requiresSmallestWidthDp", 0x01010364);
            addAttribute("compatibleWidthLimitDp", 0x01010365);
            addAttribute("largestWidthLimitDp", 0x01010366);
            addAttribute("publicKey", 0x010103a6);
            addAttribute("category", 0x010103e8);
            addAttribute("banner", 0x10103f2);
            addAttribute("isGame", 0x10103f4);
            addAttribute("requiredFeature", 0x1010557);
            addAttribute("requiredNotFeature", 0x1010558);
            addAttribute("compileSdkVersion", 0x01010572); // NOT FINALIZED
            addAttribute("compileSdkVersionCodename", 0x01010573); // NOT FINALIZED
            addAttribute("isSplitRequired", 0x01010591);

            try
            {
                Field[] fs = android.R.attr.class.getFields();

                for (Field f : fs)
                {
                    Integer id = (Integer) f.get(null);
                    String name = f.getName();

                    if (id != null && !StringUtils.isEmpty(name))
                        addAttribute(name, id);
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }

            ArrayList<Integer> sortedAttributeIds = new ArrayList<>(attributesById.keySet());
            Collections.sort(sortedAttributeIds);

            for (int resourceId : sortedAttributeIds)
            {
                String attrName = getAttributeName(resourceId);

                if (!StringUtils.isEmpty(attrName))
                {
                    prioritizedResourceIds.put(resourceId, resourceId);
                    prioritizedResourceNames.put(attrName, resourceId);
                }
            }
        }

        private void addAttribute(String name, int id)
        {
            attributesById.put(id, name);
            attributesByName.put(name, id);
        }

        public Integer getAttributeId(String attributeName)
        {
            return attributesByName.get(attributeName);
        }

        public String getAttributeName(Integer id)
        {
            String name = attributesById.get(id);

            if (StringUtils.isEmpty(name))
                System.out.println("Missing attribute: " + id);

            return name;
        }

        public Map<Integer, Integer> getPrioritizedResourceIds()
        {
            return prioritizedResourceIds;
        }

        public Map<String, Integer> getPrioritizedResourceNames()
        {
            return prioritizedResourceNames;
        }
    }
}
