package com.bsd.say.util;


import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @version 1.0
 * @date 创建时间：2019年3月6日 下午4:29:13
 */
public class Xml2MapUtil {

    public static Map<String, Object> xml2map(String xml){
        Map<String, Object> rootmap = new HashMap<>();
        if(StringUtils.isBlank(xml)){
            return rootmap;
        }
        try{
            Document doc;
            doc = DocumentHelper.parseText(xml);
            Map<String, Object> map = new HashMap<>();
            if (doc == null)
                return map;
            Element rootElement = doc.getRootElement();
            map = xml2map2(rootElement);
            rootmap.put(rootElement.getName(), map);
        }catch(Exception e){
            e.printStackTrace();
        }

        return rootmap;
    }

    private static Map<String, Object> xml2map2(Element _elem) {
        Map<String, Object> result = new HashMap<>();

        String _elemName = _elem.getName();
        // String _elemAttriName = _elem.attributeValue("name");

        List<Element> list = _elem.elements();

//        if (!"struct".equals(_elemName) && !"field".equals(_elemName) && (null == list || list.size() <= 0)) {
//            return result;
//        }

        if ("array".equals(_elemName)) {
            List<Map<String,Object>> arayList = new ArrayList<>();
            for (int i = 0;list != null &&  i < list.size(); i++) {
                Element elem = list.get(i);
                arayList.add(xml2map2(elem));
            }
            result.put(_elem.getParent().attributeValue("name"), arayList);
        }
        else if ("struct".equals(_elemName)) {
            Map tempMap = new HashMap();
            for (int i = 0;list != null && i < list.size(); i++) {
                Element elem = list.get(i);
                tempMap.putAll(xml2map2(elem));
            }
            Element __elem = _elem.getParent();
            if("data".equals(__elem.getName())){
                result.put(_elem.getParent().attributeValue("name"), tempMap);
            }
            if("array".equals(__elem.getName())){
                result.putAll(tempMap);
            }
        }
        else if ("field".equals(_elemName)) {
            result.put(_elem.getParent().attributeValue("name"), _elem.getStringValue());
        }
        else if ("data".equals(_elemName)) {
            for (int i = 0;list != null && i < list.size(); i++) {
                Element elem = list.get(i);
                String elemName = elem.getName();
                if ("field".equals(elemName)) {
                    //字段 map单个元素
                    result = xml2map2(elem);
                }
                if ("struct".equals(elemName)) {
                    //对象 map1个元素（map<key,value>）
                    result = xml2map2(elem);
                }
                if ("array".equals(elemName)) {
                    //数组 map1个元素（List<>）
                    result = xml2map2(elem);
                }
            }
        }
        else if ("body".equals(_elemName)) {
            Map tempMap = new HashMap();
            for (int i = 0; list != null &&  i < list.size(); i++) {
                Element elem = list.get(i);
                tempMap.putAll(xml2map2(elem));
            }
            result.put(_elemName, tempMap);
        }
        else if("service".equals(_elemName)){
            for (int i = 0;list != null && i < list.size(); i++) {
                Element elem = list.get(i);
                result.putAll(xml2map2(elem));
            }
        }
        else {
            for (int i = 0; list != null && i < list.size(); i++) {
                Element elem = list.get(i);
                result=xml2map2(elem);
            }
        }
        return  result;
    }

    private static Map<String, Object> xml2map22(Element _elem) {
        Map<String, Object> result = new HashMap<>();
        List<Element> list = _elem.elements();
        String _elemName = _elem.getName();
        String _elemAttriName = _elem.attributeValue("name");
        if (null == list || list.size() <= 0) {
            return result;
        }

        int size = list.size();
        for (int i = 0; i < size; i++) {
            Element elem = list.get(i);

            String nodeName = elem.getName().toString();
            String nodeAttriName = elem.attributeValue("name");
            if ("struct".equals(nodeName)) {
                List subNodes = elem.elements();
                if (null != subNodes && subNodes.size() != 0) {
                    Map subMap;
                    for (int j = 0; j < subNodes.size(); j++) {
                        Element subElem = (Element) subNodes.get(j);
                        subMap = xml2map2(subElem);
                        result.putAll(subMap);
                    }
                } else {
                    // result = new HashMap<>();
                }
            } else if ("data".equals(nodeName)) {
                List subNodes = elem.elements();
                if (null != subNodes && subNodes.size() > 0) {
                    for (int j = 0; j < subNodes.size(); j++) {
                        Element subElem = (Element) subNodes.get(j);
                        String subNodeAttriName = subElem.attributeValue("name");
                        String subNodeName = subElem.getName();
                        if ("struct".equals(subNodeName)) {
                            Map subMap = xml2map2(subElem);
                            result.put(nodeAttriName, subMap);
                        }
                        if ("array".equals(subNodeName)) {
                            List arrayList = (ArrayList) result.get(nodeAttriName);
                            if (null == arrayList) {
                                arrayList = new ArrayList();
                                arrayList.add(xml2map2(elem));
                                result.put(nodeAttriName, arrayList);
                            } else {
                                arrayList.add(xml2map2(elem));
                                result.put(nodeAttriName, arrayList);
                            }
                        }
                        if ("field".equals(subNodeName)) {
                            result.putAll(xml2map2(elem));
                        }

                    }
                }
            } else if ("field".equals(nodeName)) {
                String nodeAttriType = elem.attributeValue("type");
                if ("string".equals(nodeAttriType)) {
                    result.put(_elemAttriName, elem.getStringValue());
                } else {
                    result.put(_elemAttriName, elem.getStringValue());
                }
            } else if ("array".equals(nodeName)) {
                result.putAll(xml2map2(elem));
            } else if ("body".equals(nodeName)) {
                Map subMap = xml2map2(elem);
                result.put(nodeName, subMap);
            } else {
                if (elem.elements() != null && elem.elements().size() > 0) {
                    Map subMap = xml2map2(elem);
                    result.putAll(subMap);
                } else {
                    String nodeText = elem.getText().trim();
                    if (isEmpty(nodeText)) {
                        result.put(nodeName, null);
                    } else {
                        result.put(nodeName, nodeText);
                    }
                }
            }
        }

        return result;
    }


//                	}
//                	// TODO
//                	Map arrayMap = new HashMap();
//                	temp.add(arrayMap);
//
//        	        List subNodes = elem.elements();
//        	        if (null !=subNodes && subNodes.size() > 0) {
//        	        	for (int j = 0; j < subNodes.size(); j++) {
//        	                Element subElem = (Element) subNodes.get(j);
//        	                String subNodeName = subElem.getName();
//        	                subNodeName = subNodeName.toLowerCase(Locale.ENGLISH);
//                        	List arrayList = (ArrayList)arrayMap.get(subNodeName);
//                        	if(null == arrayList){
//                        		arrayList = new ArrayList();
//                        		arrayMap.put(subNodeName, arrayList);
//                        	}
//
//                        	Map subMap = xml2map2(subElem);
//                        	arrayList.add(subMap);
//						}
//        	        }

    public static boolean isEmpty(String text) {
        return text != null && !"".equals(text);
    }

    private static String readString3() {

        String str ;
        File file = new File("E:\\work\\众邦银行众易贷\\9、ESB\\request_std.xml");
        try {
            FileInputStream in = new FileInputStream(file);
            // size 为字串的长度 ，这里一次性读完
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            str = new String(buffer, "utf-8");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return str;
    }
}
