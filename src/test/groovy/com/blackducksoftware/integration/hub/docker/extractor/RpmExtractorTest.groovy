package com.blackducksoftware.integration.hub.docker.extractor;


import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import com.blackducksoftware.integration.hub.bdio.simple.BdioWriter
import com.blackducksoftware.integration.hub.docker.OperatingSystemEnum
import com.blackducksoftware.integration.hub.docker.PackageManagerEnum
import com.blackducksoftware.integration.hub.docker.TestUtils
import com.blackducksoftware.integration.hub.docker.dependencynode.DependencyNodeWriter
import com.blackducksoftware.integration.hub.docker.executor.ExecutorMock
import com.blackducksoftware.integration.hub.docker.tar.ImagePkgMgr
import com.google.gson.Gson

class RpmExtractorTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    void testRpmFile1() {
        testRpmExtraction('centos_rpm_output_1.txt', 'testRpmBdio1.jsonld', 'testRpmDependencies1.json')
    }

    void testRpmExtraction(String resourceName, String bdioOutputFileName, String dependenciesOutputFileName){
        URL url = this.getClass().getResource("/$resourceName")
        File resourceFile = new File(URLDecoder.decode(url.getFile(), 'UTF-8'))

        RpmExtractor extractor = new RpmExtractor()
        ExecutorMock executor = new ExecutorMock(resourceFile)
        def forges = [
            OperatingSystemEnum.CENTOS.forge
        ]
        extractor.initValues(PackageManagerEnum.RPM, executor, forges)

        File bdioOutputFile = new File("test")
        bdioOutputFile = new File(bdioOutputFile, bdioOutputFileName)
        if(bdioOutputFile.exists()){
            bdioOutputFile.delete()
        }
        bdioOutputFile.getParentFile().mkdirs()
        BdioWriter bdioWriter = new BdioWriter(new Gson(), new FileWriter(bdioOutputFile))

        File dependenciesOutputFile = new File("test")
        dependenciesOutputFile = new File(dependenciesOutputFile, dependenciesOutputFileName)
        if(dependenciesOutputFile.exists()){
            dependenciesOutputFile.delete()
        }
        dependenciesOutputFile.getParentFile().mkdirs()
        DependencyNodeWriter dependenciesWriter = new DependencyNodeWriter(new Gson(), new FileWriter(dependenciesOutputFile))

        ExtractionDetails extractionDetails = new ExtractionDetails(OperatingSystemEnum.CENTOS, 'x86')
        ImagePkgMgr imagePkgMgr = new ImagePkgMgr(new File("nonexistentdir"), PackageManagerEnum.RPM)
        extractor.extract(imagePkgMgr, bdioWriter, dependenciesWriter, extractionDetails, "CodeLocationName", "Test", "1")
        bdioWriter.close()
        dependenciesWriter.close()

        File file1 = new File("src/test/resources/testRpmBdio1.jsonld");
        File file2 = new File("test/testRpmBdio1.jsonld");
        println "Comparing ${file2.getAbsolutePath()} to ${file1.getAbsolutePath()}"
        boolean filesAreEqual = TestUtils.contentEquals(file1, file2, [
            "\"@id\":",
            "\"externalSystemTypeId\":"
        ])
        assertTrue(filesAreEqual)

        file1 = new File("src/test/resources/testRpmDependencies1.json");
        file2 = new File("test/testRpmDependencies1.json");
        println "Comparing ${file2.getAbsolutePath()} to ${file1.getAbsolutePath()}"
        filesAreEqual = TestUtils.contentEquals(file1, file2, [
            "\"@id\":",
            "\"externalSystemTypeId\":"
        ])
        assertTrue(filesAreEqual)
    }
}