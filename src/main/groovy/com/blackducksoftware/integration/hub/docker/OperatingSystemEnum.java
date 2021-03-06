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
package com.blackducksoftware.integration.hub.docker;

public enum OperatingSystemEnum {
    ALPINE("alpine"),
    CENTOS("centos"),
    DEBIAN("debian"),
    FEDORA("fedora"),
    UBUNTU("ubuntu"),
    REDHAT("redhat"), // TODO: remove?
    RHEL("redhat");

    private final String forge;

    private OperatingSystemEnum(final String forge) {
        this.forge = forge;
    }

    public String getForge() {
        return forge;
    }

    public static OperatingSystemEnum determineOperatingSystem(String operatingSystemName) {
        OperatingSystemEnum result = null;
        if (operatingSystemName != null) {
            operatingSystemName = operatingSystemName.toUpperCase();
            result = OperatingSystemEnum.valueOf(operatingSystemName);
        }
        return result;
    }
}
