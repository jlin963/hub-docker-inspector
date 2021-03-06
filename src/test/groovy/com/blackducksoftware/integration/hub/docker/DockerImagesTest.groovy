/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.docker;


import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import com.blackducksoftware.integration.hub.docker.client.ProgramVersion
import com.blackducksoftware.integration.hub.docker.image.DockerImages

class DockerImagesTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() {
        ProgramVersion mockedProgramVersion = [
            getProgramVersion: { '1.2.3' }
        ] as ProgramVersion

        DockerImages osMapper = new DockerImages()
        osMapper.programVersion = mockedProgramVersion
        assertEquals("blackducksoftware/hub-docker-inspector-centos", osMapper.getDockerImageName(OperatingSystemEnum.CENTOS))
        assertEquals("1.2.3", osMapper.getDockerImageVersion(OperatingSystemEnum.CENTOS))
        assertEquals(OperatingSystemEnum.CENTOS, osMapper.getDockerImageOs(OperatingSystemEnum.CENTOS))

        assertEquals("blackducksoftware/hub-docker-inspector", osMapper.getDockerImageName(OperatingSystemEnum.UBUNTU))
        assertEquals("1.2.3", osMapper.getDockerImageVersion(OperatingSystemEnum.UBUNTU))
        assertEquals(OperatingSystemEnum.UBUNTU, osMapper.getDockerImageOs(OperatingSystemEnum.UBUNTU))

        assertEquals("blackducksoftware/hub-docker-inspector-alpine", osMapper.getDockerImageName(OperatingSystemEnum.ALPINE))
        assertEquals("1.2.3", osMapper.getDockerImageVersion(OperatingSystemEnum.ALPINE))
        assertEquals(OperatingSystemEnum.ALPINE, osMapper.getDockerImageOs(OperatingSystemEnum.ALPINE))
    }
}