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

package org.quicktree.impl;

import org.quicktree.api.Node;
import org.quicktree.api.Path;

public class PathImpl implements Path {

    private Operator operator;
    private String condition;
    private Node nodeTo;

    public PathImpl() {
    }

    public PathImpl( String condition ) {
        this.condition = condition;
    }

    public PathImpl( Operator operator, String condition ) {
        this.condition = condition;
        this.operator = operator;
    }

    public PathImpl( Operator operator, String condition, Node nodeTo ) {
        this.condition = condition;
        this.operator = operator;
        this.nodeTo = nodeTo;
    }

    @Override
    public String getCondition() {
        return condition;
    }

    @Override
    public void setCondition( String condition ) {
        this.condition = condition;
    }

    @Override
    public Node getNodeTo() {
        return nodeTo;
    }

    @Override
    public void setNodeTo( Node node ) {
        this.nodeTo = node;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public void setOperator( Operator operator ) {
        this.operator = operator;
    }

}
