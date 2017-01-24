/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.quicktree.tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import org.junit.Test;
import org.quicktree.api.*;

import static org.quicktree.api.Path.Operator.EQUALS;
import static org.quicktree.api.Path.Operator.GREATER_THAN;
import static org.quicktree.api.Path.Operator.LESS_THAN;

import org.quicktree.api.fluent.TreeFluent;
import org.quicktree.impl.EndNodeImpl;
import org.quicktree.impl.ConditionalNodeImpl;
import org.quicktree.impl.PathImpl;
import org.quicktree.impl.PrintoutHandler;
import org.quicktree.impl.TreeImpl;

/**
 * @author salaboy
 */
public class QuickTreeTest {


    @Test
    public void hello() throws CannotCompileException, InstantiationException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {


        Tree t = new TreeImpl(Person.class);
        ConditionalNode ageNode = new ConditionalNodeImpl("n0", "age");
        Path lt30Path = new PathImpl(LESS_THAN, "30");
        ConditionalNode cityNode = new ConditionalNodeImpl("n1", "city");
        lt30Path.setNodeTo(cityNode);
        Node endNodeDoesntApply = new EndNodeImpl("end0", "Doesn't Apply");

        Path mendozaPath = new PathImpl(EQUALS, "Mendoza");
        mendozaPath.setNodeTo(endNodeDoesntApply);
        Path londonPath = new PathImpl(EQUALS, "London");

        ConditionalNode marriedNode = new ConditionalNodeImpl("n2", "married");
        londonPath.setNodeTo(marriedNode);
        cityNode.addPath(mendozaPath);
        cityNode.addPath(londonPath);
        ageNode.addPath(lt30Path);
        Path gt30Path = new PathImpl(GREATER_THAN, "30");
        ageNode.addPath(gt30Path);
        Node endNodeTooOld = new EndNodeImpl("end1", "Too Old");
        gt30Path.setNodeTo(endNodeTooOld);
        Path marriedPath = new PathImpl(EQUALS, "true");
        Path notMarriedPath = new PathImpl(EQUALS, "false");
        Node endNodeSendAd2 = new EndNodeImpl("end2", "Send Ad 2");
        marriedPath.setNodeTo(endNodeSendAd2);
        Node endNodeSendAd1 = new EndNodeImpl("end3", "Send Ad 1");

        notMarriedPath.setNodeTo(endNodeSendAd1);
        marriedNode.addPath(marriedPath);
        marriedNode.addPath(notMarriedPath);

        t.setRootNode(ageNode);

        String generated = QuickTree.generateCode(t);

        System.out.println("Generated Code: \n " + generated);

        TreeInstance treeInstance = QuickTree.createTreeInstance(generated);

        Map<String, Handler> handlers = new HashMap<>();
        handlers.put("Send Ad 1", new PrintoutHandler("Sending Ad 1..."));
        handlers.put("Send Ad 2", new PrintoutHandler("Sending Ad 2..."));
        handlers.put("Too Old", new PrintoutHandler("Too Old for me..."));
        handlers.put("Doesn't Apply", new PrintoutHandler("City Doesn't apply..."));

        treeInstance.eval(new Person("London", 17, true), handlers);

        treeInstance.eval(new Person("London", 31, true), handlers);

        String json = generateJson(t);

        System.out.println("JSON: \n" + json);

        String grapviz = generateGraphviz(t);

        System.out.println("Graphviz: \n" + grapviz);

    }

    @Test
    public void fluentAPITest() throws RuntimeException, IllegalAccessException, InstantiationException, CannotCompileException, NoSuchFieldException {
        Tree t = new TreeFluent().newTree(Person.class)
                .condition("age")
                .path(LESS_THAN, "30")
                .condition("city")
                .path(EQUALS, "Mendoza").end("Doesn't Apply")
                .path(EQUALS, "London")
                .condition("married")
                .path(EQUALS, "true").end("Send Ad 2")
                .path(EQUALS, "false").end("Send Ad 1")
                .endCondition()
                .endCondition()
                .path(GREATER_THAN, "30").end("Too Old")
                .endCondition()
                .build();
        String generated = QuickTree.generateCode(t);

        System.out.println("Generated Code: \n " + generated);

        TreeInstance treeInstance = QuickTree.createTreeInstance(generated);

        Map<String, Handler> handlers = new HashMap<>();
        handlers.put("Send Ad 1", new PrintoutHandler("Sending Ad 1..."));
        handlers.put("Send Ad 2", new PrintoutHandler("Sending Ad 2..."));
        handlers.put("Too Old", new PrintoutHandler("Too Old for me..."));
        handlers.put("Doesn't Apply", new PrintoutHandler("City Doesn't apply..."));

        treeInstance.eval(new Person("London", 17, true), handlers);

    }

    private String generateJson(Tree t) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(t);

    }





    /*
    *        digraph G {
    *           rankdir=LR;
    *           "Person" -> "Age?"
    *           "Age?" -> "City?"  [label = ">30"]
    *           "Age?" -> "Too Old"  [label = "<30", style = "bold"]
    *           "City?" -> "London?"
    *           "City?" -> "Mendoza?"
    *           "Mendoza?" -> "Don't apply" [style = "bold"]
    *           "London?" -> "Married?"
    *           "Married?" -> "Send Ad 1"  [label = "false", style = "bold"]
    *           "Married?" -> "Send Ad 2"  [label = "true", style = "bold"]
    *}
     */
    private String generateGraphviz(Tree t) throws NoSuchFieldException {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n \t rankdir=LR; \n");
        Node node = t.getRootNode();
        sb.append("\t \"").append(t.getClazz().getName()).append("\" -> \"").append(node.getName()).append("?\" \n");

        evalNode(t.getClazz(), node, sb);

        sb.append("}");
        return sb.toString();
    }

    private void evalNode(Class type, Node node, StringBuilder sb) throws NoSuchFieldException {
        if (node instanceof ConditionalNode) {
            for (Path p : ((ConditionalNode) node).getPaths()) {
                sb.append("\t \"").append(node.getName()).append("?\" -> \"").append(p.getNodeTo().getName())
                        .append("?\" [label = \"");
                Class<?> fieldType = type.getDeclaredField(node.getName()).getType();
                String operatorString = QuickTree.resolveOperatorBasedOnType(p, fieldType, true);
                sb.append(operatorString);
                sb.append("\"] \n");
                evalNode(type, p.getNodeTo(), sb);

            }
        } else if (node instanceof EndNode) {
            // do some styling here.. 
        }
    }

}
