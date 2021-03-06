package com.blackducksoftware.integration.hub.docker.tar.manifest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Manifest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final File tarExtractionDirectory;
    private final String dockerTarFileName;

    private ManifestLayerMappingFactory manifestLayerMappingFactory;

    public Manifest(final File tarExtractionDirectory, final String dockerTarFileName) {
        this.tarExtractionDirectory = tarExtractionDirectory;
        this.dockerTarFileName = dockerTarFileName;
    }

    public void setManifestLayerMappingFactory(final ManifestLayerMappingFactory manifestLayerMappingFactory) {
        this.manifestLayerMappingFactory = manifestLayerMappingFactory;
    }

    public List<ManifestLayerMapping> getLayerMappings(final String targetImageName, final String targetTagName) throws HubIntegrationException, IOException {
        logger.debug(String.format("getLayerMappings(): targetImageName: %s; targetTagName: %s", targetImageName, targetTagName));
        final List<ManifestLayerMapping> mappings = new ArrayList<>();
        final List<ImageInfo> images = getManifestContents();
        logger.debug(String.format("getLayerMappings(): images.size(): %d", images.size()));
        validateImageSpecificity(images, targetImageName, targetTagName);
        for (final ImageInfo image : images) {
            logger.debug(String.format("getLayerMappings(): image: %s", image));
            final String foundRepoTag = findRepoTag(images.size(), image, targetImageName, targetTagName);
            if (foundRepoTag == null) {
                continue;
            }
            logger.debug(String.format("foundRepoTag: %s", foundRepoTag));
            final String[] foundRepoTagParts = foundRepoTag.split(":");
            addMapping(mappings, image, foundRepoTagParts[0], foundRepoTagParts[1]);
        }
        return mappings;
    }

    private String findRepoTag(final int numImages, final ImageInfo image, final String targetImageName, final String targetTagName) throws HubIntegrationException {
        // user didn't specify which image, and there is only one: return it
        if ((numImages == 1) && StringUtils.isBlank(targetImageName) && StringUtils.isBlank(targetTagName)) {
            logger.debug(String.format("User did not specify a repo:tag, and there's only one; inspecting that one: %s", image.repoTags.get(0)));
            return image.repoTags.get(0);
        }
        final String targetRepoTag = deriveSpecifiedRepoTag(targetImageName, targetTagName);
        logger.debug(String.format("findRepoTag(): specifiedRepoTag: %s", targetRepoTag));
        for (final String repoTag : image.repoTags) {
            logger.trace(String.format("Target repo tag %s; checking %s", targetRepoTag, repoTag));
            if (StringUtils.compare(repoTag, targetRepoTag) == 0) {
                logger.trace(String.format("Found the targetRepoTag %s", targetRepoTag));
                return repoTag;
            }
        }
        return null;
    }

    private void addMapping(final List<ManifestLayerMapping> mappings, final ImageInfo image, final String imageName, final String tagName) {
        final List<String> layerIds = new ArrayList<>();
        for (final String layer : image.layers) {
            layerIds.add(layer.substring(0, layer.indexOf('/')));
        }
        final ManifestLayerMapping mapping = manifestLayerMappingFactory.createManifestLayerMapping(imageName, tagName, layerIds);
        addMapping(mappings, mapping);
    }

    private void addMapping(final List<ManifestLayerMapping> mappings, final ManifestLayerMapping mapping) {
        logger.debug("Adding layer mapping");
        logger.debug(String.format("Image %s, Tag %s", mapping.getImageName(), mapping.getTagName()));
        logger.debug(String.format("Layers %s", mapping.getLayers()));
        mappings.add(mapping);
    }

    private String deriveSpecifiedRepoTag(final String dockerImageName, final String dockerTagName) {
        String specifiedRepoTag = "";
        if (StringUtils.isNotBlank(dockerImageName)) {
            specifiedRepoTag = String.format("%s:%s", dockerImageName, dockerTagName);
        }
        return specifiedRepoTag;
    }

    private void validateImageSpecificity(final List<ImageInfo> images, final String targetImageName, final String targetTagName) throws HubIntegrationException {
        if ((images.size() > 1) && (StringUtils.isBlank(targetImageName) || StringUtils.isBlank(targetTagName))) {
            final String msg = "When the manifest contains multiple images or tags, the target image and tag to inspect must be specified";
            logger.debug(msg);
            throw new HubIntegrationException(msg);
        }
    }

    private List<ImageInfo> getManifestContents() throws IOException {
        logger.trace("getManifestContents()");
        final List<ImageInfo> images = new ArrayList<>();
        logger.debug("getManifestContents(): extracting manifest file content");
        final String manifestContentString = extractManifestFileContent(dockerTarFileName);
        logger.debug(String.format("getManifestContents(): parsing: %s", manifestContentString));
        final JsonParser parser = new JsonParser();
        final JsonArray manifestContent = parser.parse(manifestContentString).getAsJsonArray();
        final Gson gson = new Gson();
        for (final JsonElement element : manifestContent) {
            logger.debug(String.format("getManifestContents(): element: %s", element.toString()));
            images.add(gson.fromJson(element, ImageInfo.class));
        }
        return images;
    }

    private String extractManifestFileContent(final String dockerTarName) throws IOException {
        final File dockerTarDirectory = new File(tarExtractionDirectory, dockerTarName);
        final File manifest = new File(dockerTarDirectory, "manifest.json");
        final String manifestFileContents = StringUtils.join(FileUtils.readLines(manifest, StandardCharsets.UTF_8), "\n");
        return manifestFileContents;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }
}
