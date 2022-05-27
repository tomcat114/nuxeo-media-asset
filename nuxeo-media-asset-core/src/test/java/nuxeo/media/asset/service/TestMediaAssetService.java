/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Michael Vachette
 */

package nuxeo.media.asset.service;

import static org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants.PICTURE_FACET;
import static org.nuxeo.ecm.platform.video.VideoConstants.HAS_STORYBOARD_FACET;
import static org.nuxeo.ecm.platform.video.VideoConstants.HAS_VIDEO_PREVIEW_FACET;
import static org.nuxeo.ecm.platform.video.VideoConstants.VIDEO_FACET;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import nuxeo.media.asset.test.utils.SampleContent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import nuxeo.media.asset.test.features.MediaAssetTestFeature;

@RunWith(FeaturesRunner.class)
@Features(MediaAssetTestFeature.class)
@Deploy("nuxeo.media.asset.core:test-media-asset-mapping-exts.xml")
@Deploy("nuxeo.media.asset.core:test-media-asset-service-with-custom-facet.xml")
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
public class TestMediaAssetService {

    @Inject
    protected CoreSession session;

    @Inject
    protected MediaAssetService mediaAssetService;

    @Test
    public void testServiceIsDeployed() {
        Assert.assertNotNull(mediaAssetService);
    }

    @Test
    public void testGetPictureFacetFromJPEG() {
        List<String> facets = mediaAssetService.getMediaFacets("image/jpeg",null);
        Assert.assertEquals(1, facets.size());
        Assert.assertEquals(PICTURE_FACET, facets.get(0));
    }

    @Test
    public void testGetPictureFacetFromPSD() {
        List<String> facets = mediaAssetService.getMediaFacets("application/photoshop", null);
        Assert.assertEquals(1, facets.size());
        Assert.assertEquals(PICTURE_FACET, facets.get(0));
    }

    @Test
    public void testGetVideoFacetFromMimetype() {
        List<String> facets = mediaAssetService.getMediaFacets("video/mp4", null);
        Assert.assertEquals(3, facets.size());
        Assert.assertTrue(facets.contains(VIDEO_FACET));
        Assert.assertTrue(facets.contains(HAS_STORYBOARD_FACET));
        Assert.assertTrue(facets.contains(HAS_VIDEO_PREVIEW_FACET));
    }

    @Test
    public void testGetAudioFacetFromMimetype() {
        List<String> facets = mediaAssetService.getMediaFacets("audio/mp3", null);
        Assert.assertEquals(1, facets.size());
        Assert.assertEquals("Audio", facets.get(0));
    }

    @Test
    public void testGetNoFacetFromMimetype() {
        List<String> facets = mediaAssetService.getMediaFacets("application/pdf", null);
        Assert.assertEquals(0, facets.size());
    }

    @Test
    public void testGetFacetFromExtension() {
        List<String> facets = mediaAssetService.getMediaFacets("application/octet-stream", "abc");
        Assert.assertEquals(1, facets.size());
        Assert.assertEquals("Custom", facets.get(0));
    }

    @Test
    public void testGetPictureFacetFromBlob() throws IOException {
        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext(SampleContent.JPEG_PATH));
        List<String> facets = mediaAssetService.getMediaFacets(blob);
        Assert.assertEquals(1, facets.size());
        Assert.assertEquals(PICTURE_FACET, facets.get(0));
    }

    @Test
    public void testGetCustomFacetFromBlobBasedOnExtension() throws IOException {
        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext(SampleContent.ABC_PATH));
        List<String> facets = mediaAssetService.getMediaFacets(blob);
        Assert.assertEquals(1, facets.size());
        Assert.assertEquals("Custom", facets.get(0));
    }

    @Test
    public void testGetNoFacetFromBlob() throws IOException {
        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext(SampleContent.PDF_PATH));

        List<String> facets = mediaAssetService.getMediaFacets(blob);
        Assert.assertEquals(0, facets.size());
    }

    @Test
    public void testGetNoFacetZipBlob() throws IOException {
        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext(SampleContent.ZIP_PATH));

        List<String> facets = mediaAssetService.getMediaFacets(blob);
        Assert.assertEquals(0, facets.size());
    }

    @Test
    @Deploy("nuxeo.media.asset.core:test-media-asset-service-with-custom-facet.xml")
    public void testWithCustomFacet() {
        List<String> allFacets = ((MediaAssetServiceImpl) mediaAssetService).allMediaFacets;
        Assert.assertTrue(allFacets.contains("Custom"));
    }

    @Test
    public void testWithUnsupportedDocument() {
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(),
                "File", "Document");
        Assert.assertFalse(mediaAssetService.isDocumentSupported(doc));
    }

    @Test
    @Deploy("nuxeo.media.asset.core:test-media-asset-service-filter.xml")
    public void testWithCustomFilter() {
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "Document",
                "FileWithAsset");

        Assert.assertTrue(mediaAssetService.isDocumentSupported(doc));
    }

}
