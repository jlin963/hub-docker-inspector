package com.blackducksoftware.integration.hub.docker.tar.manifest;

import java.io.File;

public class HardwiredManifestFactory implements ManifestFactory {

    @Override
    public Manifest createManifest(final File tarExtractionDirectory, final String dockerTarFileName) {
        final Manifest manifest = new Manifest(tarExtractionDirectory, dockerTarFileName);
        manifest.setManifestLayerMappingFactory(new HardwiredManifestLayerMappingFactory());
        return manifest;
    }

}
