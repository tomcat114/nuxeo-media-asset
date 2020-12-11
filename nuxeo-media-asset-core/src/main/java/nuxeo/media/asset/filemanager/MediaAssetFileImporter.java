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

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.filemanager.api.FileImporterContext;
import org.nuxeo.ecm.platform.filemanager.service.extension.DefaultFileImporter;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.runtime.api.Framework;

import nuxeo.media.asset.service.MediaAssetService;

public class MediaAssetFileImporter extends DefaultFileImporter {

    @Override
    public DocumentModel createOrUpdate(FileImporterContext context) throws IOException {
        // check if the parent document accepts the Asset type
        try {
            MediaAssetService mediaAssetService = Framework.getService(MediaAssetService.class);
            if (mediaAssetService.isBlobSupported(context.getBlob())) {
                checkAllowedSubtypes(context.getSession(), context.getParentPath(), getDocType());
                return super.createOrUpdate(context);
            } else {
                return null;
            }
        } catch (NuxeoException e) {
            return null;
        }
    }
    
    protected void checkAllowedSubtypes(CoreSession session, String path, String typeName) {
        PathRef containerRef = new PathRef(path);
        DocumentModel container = session.getDocument(containerRef);
        TypeManager typeService = Framework.getService(TypeManager.class);
        Type containerType = typeService.getType(container.getType());
        if (containerType == null) {
            return;
        }

        if (!typeService.isAllowedSubType(typeName, container.getType(), container)) {
            throw new NuxeoException(String.format("Cannot create document of type %s in container with type %s",
                    typeName, containerType.getId()));
        }
    }
}
