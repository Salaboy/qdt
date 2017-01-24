/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quicktree.tests;

import com.google.gson.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javassist.CannotCompileException;

import javassist.NotFoundException;
import org.junit.Test;
import org.quicktree.api.*;
import org.quicktree.impl.*;

/**
 * @author salaboy
 */
public class ParseJsonGraphTest {

    @Test
    public void hello() throws FileNotFoundException, ClassNotFoundException, NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        Gson gson = new GsonBuilder().create();

        InputStream modelStream = ParseJsonGraphTest.class.getResourceAsStream("/model.json");
        JsonElement jsonElement = gson.fromJson(new InputStreamReader(modelStream), JsonElement.class);
        JsonObject jsonObj = jsonElement.getAsJsonObject();
        Map<String, Class<?>> fields = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
            if (!entry.getKey().equals("type")) {
                fields.put(entry.getKey(), Class.forName(entry.getValue().getAsString()));
            }
        }


        Class type = PojoGenerator.generate("org.quicktree.generated."+jsonObj.get("type").getAsString(), fields);

        InputStream graphStream = ParseJsonGraphTest.class.getResourceAsStream("/graph.json");


        jsonElement = gson.fromJson(new InputStreamReader(graphStream), JsonElement.class);
        jsonObj = jsonElement.getAsJsonObject();
        JsonArray nodesArray = jsonObj.getAsJsonArray("nodes");
        Tree t = new TreeImpl(type);

        Iterator<JsonElement> iteratorNodes = nodesArray.iterator();
        Map<String, Node> nodes = new HashMap<>();

        while (iteratorNodes.hasNext()) {
            JsonElement next = iteratorNodes.next();
            String id = next.getAsJsonObject().get("data").getAsJsonObject().get("id").getAsString();
            String name = next.getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString();

            JsonElement typeElement = next.getAsJsonObject().get("data").getAsJsonObject().get("type");

            String nodeType = "";
            if (typeElement != null) {
                nodeType = typeElement.getAsString();
            }
            System.out.println("node id = " + id + " - name = " + name + " - type = " + nodeType);
            Node node = null;
            if (nodeType.equals("root")) {
                node = new RootNodeImpl(id, name);
                t.setRootNode(node);
            } else if(nodeType.equals("condition")) {
                node = new ConditionalNodeImpl(id, name);
            } else if(nodeType.equals("terminal")){
                node = new EndNodeImpl(id, name);
            }

            nodes.put(id, node);
        }

        JsonArray edgesArray = jsonObj.getAsJsonArray("edges");
        Iterator<JsonElement> iteratorEdges = edgesArray.iterator();
        Map<String, Map<String, String>> edges = new HashMap<>();
        while (iteratorEdges.hasNext()) {
            JsonElement next = iteratorEdges.next();
            String source = next.getAsJsonObject().get("data").getAsJsonObject().get("source").getAsString();
            String target = next.getAsJsonObject().get("data").getAsJsonObject().get("target").getAsString();
            JsonElement valueElement = next.getAsJsonObject().get("data").getAsJsonObject().get("value");
            String value = "";
            if (valueElement != null) {
                value = valueElement.getAsString();
            }
            String operator = "";

            JsonElement operatorElement = next.getAsJsonObject().get("data").getAsJsonObject().get("operator");
            if (operatorElement != null) {
                operator = operatorElement.getAsString();
            }

            System.out.println("edge source = " + source + " - target = " + target + " - operator = " + operator + " - value = " + value);
            Path path = null;
            if (nodes.get(source) instanceof RootNode) {
                path = new PathImpl(value);
                ((RootNode)nodes.get(source)).addOnlyPath(path);
            } else {
                path = new PathImpl(Path.Operator.valueOf(operator), value);
                ((ConditionalNode)nodes.get(source)).addPath(path);
            }
            path.setNodeTo(nodes.get(target));



        }

        Object instance = type.newInstance();
        type.getMethod("setAge", Integer.class).invoke(instance, 20);
        type.getMethod("setMarried", Boolean.class).invoke(instance, true);
        type.getMethod("setCity", String.class).invoke(instance, "London");

        System.out.println("Clazz: " + type);
        System.out.println("Object: " + instance);
        for (final Method method : type.getDeclaredMethods()) {
            System.out.println(method);
        }


        String generated = QuickTree.generateCode(t);
        System.out.println(generated);
        Map<String, Handler> handlers = new HashMap<>();
        handlers.put("Send Ad 1", new PrintoutHandler("Sending Ad 1..."));
        handlers.put("Send Ad 2", new PrintoutHandler("Sending Ad 2..."));
        handlers.put("Too Old", new PrintoutHandler("Too Old..."));
        handlers.put("Doesn't Apply", new PrintoutHandler("Doesn't apply..."));

        TreeInstance treeInstance = QuickTree.createTreeInstance(generated);
        treeInstance.eval(instance, handlers);
    }


}
