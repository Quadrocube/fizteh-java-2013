package ru.fizteh.fivt.students.sterzhanovVladislav.fileMap.storeable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

public class StoreableUtils {

    public static final HashMap<String, Class<?>> TYPENAMES = new HashMap<String, Class<?>>();
    public static final HashMap<Class<?>, String> CLASSES = new HashMap<Class<?>, String>();
    
    static {
        TYPENAMES.put("int", Integer.class);
        TYPENAMES.put("long", Long.class);
        TYPENAMES.put("byte", Byte.class);
        TYPENAMES.put("float", Float.class);
        TYPENAMES.put("double", Double.class);
        TYPENAMES.put("boolean", Boolean.class);
        TYPENAMES.put("String", String.class);
        
        for (Map.Entry<String, Class<?>> entry : TYPENAMES.entrySet()) {
            CLASSES.put(entry.getValue(), entry.getKey());
        }
    }

    public static String serialize(Storeable s, List<Class<?>> types) {
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = document.createElement("row") ;
            for (int i = 0; i < types.size(); ++i) {
                Object obj = s.getColumnAt(i);
                Element child;
                if (obj == null) {
                    child = document.createElement("null");
                } else {
                    child = document.createElement("col");
                    child.setTextContent(obj.toString());
                }
                root.appendChild(child);
            }
            document.appendChild(root);
        } catch (IndexOutOfBoundsException | ParserConfigurationException e) {
            throw new ColumnFormatException();
        }
        StringWriter writer;
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
        } catch (TransformerException e) {
            throw new RuntimeException("FATAL: Unable to initialize converter");
        }
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }

    public static Storeable deserialize(String s, List<Class<?>> types) throws ParseException {
        StoreableRow row = new StoreableRow(types);
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(s.getBytes()));
        } catch (SAXException | IOException | ParserConfigurationException e1) {
            throw new RuntimeException("Unable to initialize parser");
        }
        Element root = document.getDocumentElement();
        String rootTag = root.getTagName();
        if (!rootTag.equals("row")) {
            throw new ParseException("Illegal root tag, expected \"row\", but got: \"" + rootTag + "\"", 0);
        }
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node node = children.item(i);
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element e = (Element) node;
                    String tagName = e.getTagName();
                    if (tagName.equals("null")) {
                        row.setColumnAt(i, null);
                    } else if (tagName.equals("col")) {
                        String value = e.getTextContent();
                        row.setColumnAt(i, dynamicParse(value, types.get(i)));
                    } else {
                        throw new ParseException("Illegal tag, expected: \"col\", but got: \"" + tagName + "\"", i);
                    }
                    break;
                default :
                    throw new ParseException("Illegal node type", i);
            }
        }
        return row;
    }

    public static boolean validate(Storeable s, List<Class<?>> types) {
        for (int classID = 0; classID < types.size(); ++classID) {
            try {
                Object object = s.getColumnAt(classID);
                if (object == null) {
                    continue;
                }
                if (!object.getClass().equals(types.get(classID))) {
                    return false;
                }
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        }
        try {
            s.getColumnAt(types.size());
            return false;
        } catch (IndexOutOfBoundsException e) {
            return true;
        }
    }
    
    private static Object dynamicParse(String s, Class<?> type) {
        try {
            if (type.equals(String.class)) {
                return s;
            } else if (type.equals(Integer.class)) {
                return Integer.parseInt(s);
            } else if (type.equals(Long.class)) {
                return Long.parseLong(s);
            } else if (type.equals(Byte.class)) {
                return Byte.parseByte(s);
            } else if (type.equals(Float.class)) {
                return Float.parseFloat(s);
            } else if (type.equals(Double.class)) {
                return Double.parseDouble(s);
            } else if (type.equals(Boolean.class)) {
                return Boolean.parseBoolean(s);
            } 
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("wrong type (" + e.getMessage() + ")");
        }

        throw new ColumnFormatException("wrong type (unable to handle type " + type.toString() + ")");
    }
    
    public static Class<?> resolveClass(String className) {
        return TYPENAMES.get(className);
    }
    
    public static List<Class<?>> generateSignature(Table table) {
        List<Class<?>> classList = new ArrayList<Class<?>>();
        for (int classID = 0; classID < table.getColumnsCount(); ++classID) {
            classList.add(table.getColumnType(classID));
        }
        return classList;
    }
    
}
