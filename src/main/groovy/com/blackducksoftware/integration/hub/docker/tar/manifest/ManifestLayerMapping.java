/**
 * Hub Docker Inspector
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.docker.tar.manifest;

import java.util.List;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.docker.client.ProgramPaths;

public class ManifestLayerMapping {
    private final String imageName;
    private final String tagName;
    private final List<String> layers;

    @Autowired
    private ProgramPaths programPaths;

    public ManifestLayerMapping(final String imageName, final String tagName, final List<String> layers) {
        this.imageName = imageName;
        this.tagName = tagName;
        this.layers = layers;
    }

    public String getImageName() {
        return imageName;
    }

    public String getTagName() {
        return tagName;
    }

    public List<String> getLayers() {
        return layers;
    }

    public String getTargetImageFileSystemRootDirName() {
        return getProgramPaths().getTargetImageFileSystemRootDirName(imageName, tagName);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

    public ProgramPaths getProgramPaths() {
        return programPaths;
    }

    public void setProgramPaths(ProgramPaths programPaths) {
        this.programPaths = programPaths;
    }
}
