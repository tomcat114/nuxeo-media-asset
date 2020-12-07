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

import java.util.List;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * A service that provides methods to handle multiple media asset types (picture, video, audio ...) with a single
 * document type
 */
public interface MediaAssetService {

    /**
     * @param doc a DocumentModel object
     * @return true if the document is supported by the service
     */
    boolean isDocumentSupported(DocumentModel doc);

    /**
     * @param blob a blob
     * @return true if the blob is supported by the service
     */
    boolean isBlobSupported(Blob blob);

    /**
     * Update and Applies Facets to the input document depending on the blob stored in file:content
     * 
     * @param doc a DocumentModel object
     * @return the input DocumentModel object
     */
    DocumentModel updateDocumentMediaFacet(DocumentModel doc);

    /**
     * Get the Document facets corresponding to the input blob
     * 
     * @param blob a blob
     * @return A list of Facets
     */
    List<String> getMediaFacets(Blob blob);

    /**
     * Get the Document facets corresponding to the input mimtype
     * 
     * @param mimetype a file mimetype
     * @return A list of Facets
     */
    List<String> getMediaFacets(String mimetype);

}
