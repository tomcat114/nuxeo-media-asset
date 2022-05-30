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

package nuxeo.media.asset.listener;

import static org.nuxeo.ecm.platform.picture.api.ImagingDocumentConstants.PICTURE_FACET;
import static org.nuxeo.ecm.platform.video.VideoConstants.HAS_STORYBOARD_FACET;
import static org.nuxeo.ecm.platform.video.VideoConstants.HAS_VIDEO_PREVIEW_FACET;
import static org.nuxeo.ecm.platform.video.VideoConstants.VIDEO_FACET;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.UUID;

import javax.inject.Inject;

import nuxeo.media.asset.test.utils.SampleContent;
import nuxeo.media.asset.test.utils.TestContentBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import nuxeo.media.asset.test.features.MediaAssetTestFeature;

@RunWith(FeaturesRunner.class)
@Features(MediaAssetTestFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
public class TestMediaAssetListener {

    @Inject
    protected CoreSession session;

    protected static final String TEST_DOC_TYPE = "FileWithAsset";
    
    @Test
    public void testUnsupportedType() {
        DocumentModel compound = TestContentBuilder.newInstance(session)
                                                   .setType("File")
                                                   .setFilePath(SampleContent.JPEG_PATH)
                                                   .build();
        Assert.assertFalse(compound.hasFacet(PICTURE_FACET));
    }

    @Test
    public void testCompoundPicture() {
        DocumentModel compound = TestContentBuilder.newInstance(session)
                                                   .setType(TEST_DOC_TYPE)
                                                   .setFilePath(SampleContent.JPEG_PATH)
                                                   .build();
        Assert.assertTrue(compound.hasFacet(PICTURE_FACET));
    }

    @Test
    public void testCompoundVideo() {
        DocumentModel compound = TestContentBuilder.newInstance(session)
                                                   .setType(TEST_DOC_TYPE)
                                                   .setFilePath(SampleContent.MP4_PATH)
                                                   .build();
        Assert.assertTrue(compound.hasFacet(VIDEO_FACET));
        Assert.assertTrue(compound.hasFacet(HAS_STORYBOARD_FACET));
        Assert.assertTrue(compound.hasFacet(HAS_VIDEO_PREVIEW_FACET));
    }

    @Test
    public void testCompoundFile() {
        DocumentModel compound = TestContentBuilder.newInstance(session)
                                                   .setType(TEST_DOC_TYPE)
                                                   .setFilePath(SampleContent.PDF_PATH)
                                                   .build();
        Assert.assertFalse(compound.hasFacet(PICTURE_FACET));
        Assert.assertFalse(compound.hasFacet(VIDEO_FACET));
        Assert.assertFalse(compound.hasFacet("Audio"));
    }

    @Test
    public void testEmptyCompound() {
        DocumentModel compound = TestContentBuilder.newInstance(session)
                                                   .setType(TEST_DOC_TYPE)
                                                   .build();
        Assert.assertFalse(compound.hasFacet(PICTURE_FACET));
        Assert.assertFalse(compound.hasFacet(VIDEO_FACET));
        Assert.assertFalse(compound.hasFacet("Audio"));
    }

    @Test
    public void testEmptyCompoundAndPictureAddedLater() throws IOException {
        DocumentModel compound = TestContentBuilder.newInstance(session)
                                                   .setType(TEST_DOC_TYPE)
                                                   .build();
        Assert.assertFalse(compound.hasFacet(PICTURE_FACET));
        Assert.assertFalse(compound.hasFacet(VIDEO_FACET));
        Assert.assertFalse(compound.hasFacet("Audio"));

        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext(SampleContent.JPEG_PATH));
        compound.setPropertyValue("file:content", (Serializable) blob);
        compound = session.saveDocument(compound);
        Assert.assertTrue(compound.hasFacet(PICTURE_FACET));
    }

    @Test
    public void testUpdatePicture() throws IOException {
        DocumentModel compound = TestContentBuilder.newInstance(session)
                                                   .setType(TEST_DOC_TYPE)
                                                   .setFilePath(SampleContent.JPEG_PATH)
                                                   .build();
        Assert.assertTrue(compound.hasFacet(PICTURE_FACET));

        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext(SampleContent.JPEG_PATH));
        compound.setPropertyValue("file:content", (Serializable) blob);
        compound = session.saveDocument(compound);
        Assert.assertTrue(compound.hasFacet(PICTURE_FACET));
    }

    @Test
    public void testChangePicturetoVideo() throws IOException {
        DocumentModel compound = TestContentBuilder.newInstance(session)
                                                   .setType(TEST_DOC_TYPE)
                                                   .setFilePath(SampleContent.JPEG_PATH)
                                                   .build();
        Assert.assertTrue(compound.hasFacet(PICTURE_FACET));

        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext(SampleContent.MP4_PATH));
        compound.setPropertyValue("file:content", (Serializable) blob);
        compound = session.saveDocument(compound);
        Assert.assertFalse(compound.hasFacet(PICTURE_FACET));
        Assert.assertTrue(compound.hasFacet(VIDEO_FACET));
    }

    @Test
    public void testChangeVideotoPicture() throws IOException {
        DocumentModel compound = TestContentBuilder.newInstance(session)
                                                   .setType(TEST_DOC_TYPE)
                                                   .setFilePath(SampleContent.MP4_PATH)
                                                   .build();
        Assert.assertTrue(compound.hasFacet(VIDEO_FACET));
        Assert.assertTrue(compound.hasFacet(HAS_STORYBOARD_FACET));
        Assert.assertTrue(compound.hasFacet(HAS_VIDEO_PREVIEW_FACET));

        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext(SampleContent.JPEG_PATH));

        compound.setPropertyValue("file:content", (Serializable) blob);
        compound = session.saveDocument(compound);
        Assert.assertTrue(compound.hasFacet(PICTURE_FACET));
        Assert.assertFalse(compound.hasFacet(VIDEO_FACET));
        Assert.assertFalse(compound.hasFacet(HAS_STORYBOARD_FACET));
        Assert.assertFalse(compound.hasFacet(HAS_VIDEO_PREVIEW_FACET));
    }

    @Test
    public void testChangePictureToEmpty() {
        DocumentModel compound = TestContentBuilder.newInstance(session)
                                                   .setType(TEST_DOC_TYPE)
                                                   .setFilePath(SampleContent.JPEG_PATH)
                                                   .build();
        Assert.assertTrue(compound.hasFacet(PICTURE_FACET));

        compound.setPropertyValue("file:content", null);
        compound = session.saveDocument(compound);
        Assert.assertFalse(compound.hasFacet(PICTURE_FACET));
        Assert.assertTrue(compound.hasSchema("file"));
    }

    @Test
    public void testImport() throws IOException {
        DocumentModel compound = session.createDocumentModel(null, "TEST_IMPORT",
        		TEST_DOC_TYPE);
        // uuid must be set when using the import API
        ((DocumentModelImpl) compound).setId(UUID.randomUUID().toString());
        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext(SampleContent.JPEG_PATH));
        compound.setPropertyValue("file:content", (Serializable) blob);
        session.importDocuments(Collections.singletonList(compound));
        Assert.assertTrue(compound.hasFacet(PICTURE_FACET));
    }

}
