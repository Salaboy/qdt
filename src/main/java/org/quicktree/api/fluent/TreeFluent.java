/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quicktree.api.fluent;

import java.util.ArrayDeque;
import java.util.Deque;

import org.quicktree.api.ConditionalNode;
import org.quicktree.api.Node;
import org.quicktree.api.Path;
import org.quicktree.api.Path.Operator;
import org.quicktree.api.Tree;
import org.quicktree.impl.ConditionalNodeImpl;
import org.quicktree.impl.EndNodeImpl;
import org.quicktree.impl.PathImpl;
import org.quicktree.impl.TreeImpl;

public class TreeFluent {

    private Tree tree;
    private Node rootNode;
    private Path currentPath;
    private final Deque<Node> nodeStack = new ArrayDeque<>();
    private static int nodeIdGenerator = 0;

    public TreeFluent newTree(Class clazz) {
        tree = new TreeImpl(clazz);
        return this;
    }

    public TreeFluent condition(String name) {
        //push stack

        ConditionalNodeImpl node = new ConditionalNodeImpl("n" + nodeIdGenerator++, name);
        if (rootNode == null) {
            tree.setRootNode(node);
            rootNode = node;
        } else {
            currentPath.setNodeTo(node);
        }
        nodeStack.push(node);
        return this;

    }

    public TreeFluent end(String name) {
        currentPath.setNodeTo(new EndNodeImpl("n" + nodeIdGenerator++, name));
        return this;
    }

    public TreeFluent path(Operator operator, String condition) {
        currentPath = new PathImpl(operator, condition);
        if (nodeStack.peek() instanceof ConditionalNode) {
            ((ConditionalNode) nodeStack.peek()).addPath(currentPath);
        }
        return this;
    }

    public TreeFluent endCondition() {
        nodeStack.pop();
        return this;
    }

    public Tree build() {
        if (!nodeStack.isEmpty()) {
            throw new IllegalStateException("Make sure that you closed all the conditions");
        }
        return tree;
    }
}
