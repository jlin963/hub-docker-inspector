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
package com.blackducksoftware.integration.hub.docker.extractor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.simple.DependencyNodeBuilder;
import com.blackducksoftware.integration.hub.bdio.simple.model.BdioComponent;
import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.blackducksoftware.integration.hub.docker.OperatingSystemEnum;
import com.blackducksoftware.integration.hub.docker.PackageManagerEnum;
import com.blackducksoftware.integration.hub.docker.executor.DpkgExecutor;

@Component
class DpkgExtractor extends Extractor {
    private final Logger logger = LoggerFactory.getLogger(DpkgExtractor.class);

    @Autowired
    private DpkgExecutor executor;

    @Override
    @PostConstruct
    public void init() {
        final List<String> forges = new ArrayList<>();
        forges.add(OperatingSystemEnum.DEBIAN.getForge());
        forges.add(OperatingSystemEnum.UBUNTU.getForge());
        initValues(PackageManagerEnum.DPKG, executor, forges);
    }

    @Override
    public ExtractionResults extractComponents(final String dockerImageRepo, final String dockerImageTag, final ExtractionDetails extractionDetails, final String[] packageList) {
        final List<BdioComponent> components = new ArrayList<>();
        final DependencyNode rootNode = createDependencyNode(OperatingSystemEnum.UBUNTU.getForge(), dockerImageRepo, dockerImageTag, extractionDetails.getArchitecture());
        final DetectCodeLocation codeLocation = new DetectCodeLocation(BomToolType.DOCKER, String.format("%s_%s", dockerImageRepo, dockerImageTag), rootNode);

        final DependencyNodeBuilder dNodeBuilder = new DependencyNodeBuilder(rootNode);
        boolean startOfComponents = false;
        for (final String packageLine : packageList) {

            if (packageLine != null) {
                if (packageLine.matches("\\+\\+\\+-=+-=+-=+-=+")) {
                    startOfComponents = true;
                } else if (startOfComponents) {
                    final char packageStatus = packageLine.charAt(1);
                    if (isInstalledStatus(packageStatus)) {
                        final String componentInfo = packageLine.substring(3);
                        final String[] componentInfoParts = componentInfo.trim().split("[  ]+");
                        String name = componentInfoParts[0];
                        final String version = componentInfoParts[1];
                        final String architecture = componentInfoParts[2];
                        if (name.contains(":")) {
                            name = name.substring(0, name.indexOf(":"));
                        }
                        final String externalId = String.format("%s/%s/%s", name, version, architecture);
                        logger.debug(String.format("Constructed externalId: %s", externalId));

                        createBdioComponent(dNodeBuilder, rootNode, components, name, version, externalId, architecture);
                    } else {
                        logger.debug(String.format("Package \"%s\" is listed but not installed (package status: %s)", packageLine, packageStatus));
                    }
                }
            }
        }
        logger.trace(String.format("DependencyNode tree: %s", rootNode));
        return new ExtractionResults(components, codeLocation);
    }

    private boolean isInstalledStatus(final Character packageStatus) {
        final String packageStatusString = packageStatus.toString();
        if ("iWt".contains(packageStatusString)) {
            return true;
        }
        return false;
    }
}