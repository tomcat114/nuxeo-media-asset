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

package nuxeo.media.asset.filemanager;

import java.io.IOException;

import javax.inject.Inject;

import nuxeo.media.asset.test.features.FilemanagerTestFeature;
import nuxeo.media.asset.test.features.MediaAssetTestFeature;
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
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.filemanager.api.FileImporterContext;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features({ MediaAssetTestFeature.class, FilemanagerTestFeature.class })
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
public class TestMediaAssetFileImporter {

    @Inject
    protected CoreSession session;

    @Inject
    protected FileManager fileManager;


    @Test
    @Deploy("nuxeo.media.asset.core:test-media-asset-service-with-custom-folder.xml")
    public void testNewFileInWorkspace() throws IOException {
        DocumentModel workspace = TestContentBuilder.newInstance(session)
                                                    .setType("CustomFolder")
                                                    .build();
        Blob blob = Blobs.createBlob(FileUtils.getResourceFileFromContext(SampleContent.PDF_PATH));

        FileImporterContext context = FileImporterContext.builder(session, blob, workspace.getPathAsString())
                                                         .overwrite(false)
                                                         .build();

        DocumentModel file = fileManager.createOrUpdateDocument(context);
        Assert.assertEquals("File", file.getType());
    }

}
