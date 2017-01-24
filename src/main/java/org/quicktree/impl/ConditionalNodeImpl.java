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

import java.util.Collection;
import java.util.HashSet;
import org.quicktree.api.ConditionalNode;
import org.quicktree.api.Path;

public class ConditionalNodeImpl implements ConditionalNode {

    private String name;
    private String id;
    private Collection<Path> paths;
    

    public ConditionalNodeImpl() {

    }

    public ConditionalNodeImpl( String id,  String name ) {
        this.id = id;
        this.name = name;
    }

    @Override
    public Collection<Path> getPaths() {
        return paths;
    }

    @Override
    public void setPaths( Collection<Path> paths ) {
        this.paths = paths;
    }

    @Override
    public void addPath( Path p ) {
        if ( this.paths == null ) {
            paths = new HashSet<>();
        }
        this.paths.add( p );
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setName( String name ) {
        this.name = name;
    }



}
