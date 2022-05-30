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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.actions.ActionContext;
import org.nuxeo.ecm.platform.actions.ELActionContext;
import org.nuxeo.ecm.platform.actions.ejb.ActionManager;
import org.nuxeo.ecm.platform.filemanager.utils.FileManagerUtils;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.runtime.services.config.ConfigurationService;

public class MediaAssetServiceImpl extends DefaultComponent implements MediaAssetService {

    public static final String MIMETYPE_ZIP = "application/zip";

    public static String MEDIA_TYPE_EXT_POINT = "mediatype";

    public static String SUPPORTED_ZIP_CONTENT_EXT_POINT = "supportedZipContent";

    public static String INPUT_FILTER_KEY = "nuxeo.media.asset.service.default.filter.name";

    public List<String> allMediaFacets = new ArrayList<>();

    protected Map<String, MediaTypeDescriptor> mediatypes = new HashMap<>();

    protected MimetypeListDescriptor supportedZipContent;

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (MEDIA_TYPE_EXT_POINT.equals(extensionPoint)) {
            MediaTypeDescriptor descriptor = (MediaTypeDescriptor) contribution;
            mediatypes.put(descriptor.name, descriptor);
            for (String facet : descriptor.getFacets()) {
                if (!allMediaFacets.contains(facet)) {
                    allMediaFacets.add(facet);
                }
            }
        } else if (SUPPORTED_ZIP_CONTENT_EXT_POINT.equals(extensionPoint)) {
            supportedZipContent = (MimetypeListDescriptor) contribution;
        }
    }

    @Override
    public boolean isDocumentSupported(DocumentModel doc) {
        ActionManager actionService = Framework.getService(ActionManager.class);
        ConfigurationService configurationService = Framework.getService(ConfigurationService.class);
        ActionContext actionContext = new ELActionContext();
        actionContext.setCurrentDocument(doc);
        Optional<String> filterId = configurationService.getString(INPUT_FILTER_KEY);
        return filterId.isPresent() && actionService.checkFilter(filterId.get(), actionContext);
    }

    @Override
    public boolean isBlobSupported(Blob blob) {
        if (blob == null) {
            return false;
        }
        MimetypeRegistry registry = Framework.getService(MimetypeRegistry.class);
        String mimetype = registry.getMimetypeFromFilenameAndBlobWithDefault(blob.getFilename(), blob, null);

        if (MIMETYPE_ZIP.equals(mimetype)) {
            return getZipContentMimetype(blob) != null;
        } else {
            return true;
        }
    }

    @Override
    public DocumentModel updateDocumentMediaFacet(DocumentModel doc) {
        Blob blob = (Blob) doc.getPropertyValue("file:content");
        List<String> facets = getMediaFacets(blob);
        for (String mediaFacet : allMediaFacets) {
            if (facets.contains(mediaFacet)) {
                doc.addFacet(mediaFacet);
            } else if (doc.hasFacet(mediaFacet)) {
                doc.removeFacet(mediaFacet);
            }
        }
        return doc;
    }

    @Override
    public List<String> getMediaFacets(Blob blob) {
        if (blob == null) {
            return new ArrayList<>();
        }

        String extension = FileUtils.getFileExtension(blob.getFilename());

        MimetypeRegistry registry = Framework.getService(MimetypeRegistry.class);
        String mimetype = registry.getMimetypeFromFilenameAndBlobWithDefault(blob.getFilename(), blob, null);

        if (MIMETYPE_ZIP.equals(mimetype)) {
            mimetype = getZipContentMimetype(blob);
        }

        return mimetype != null || extension != null ? getMediaFacets(mimetype, extension) : new ArrayList<>();
    }

    @Override
    public List<String> getMediaFacets(String mimetype, String extension) {
        List<MediaTypeDescriptor> types = new ArrayList<>(mediatypes.values());
        types.sort(Comparator.comparingInt(MediaTypeDescriptor::getOrder));
        List<String> facets = new ArrayList<>();
        Iterator<MediaTypeDescriptor> iterator = types.iterator();
        while (facets.size() == 0 && iterator.hasNext()) {
            MediaTypeDescriptor type = iterator.next();
            if (type.isEnabled() &&
                    (doesMimetypeMatchesType(mimetype, type) || doesMatchExtension(extension, type.getExtensions()))) {
                facets = type.getFacets();
            }
        }
        return facets;
    }

    protected boolean doesMimetypeMatchesType(String mimetype, MediaTypeDescriptor type) {
        if (mimetype != null) {
            for (String mimetypePattern : type.mimetypes) {
                Pattern pattern = Pattern.compile(mimetypePattern);
                if (pattern.matcher(mimetype).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean doesMatchExtension(String extension, List<String> extensions) {
        if (StringUtils.isNotBlank(extension)) {
            for (String typeExtension : extensions) {
                if (typeExtension.equalsIgnoreCase(extension)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected String getZipContentMimetype(Blob zipBlob) {
        MimetypeRegistry registry = Framework.getService(MimetypeRegistry.class);
        String mimetype = null;

        ZipEntry zipEntry;
        try (ZipInputStream zipInputStream = new ZipInputStream(zipBlob.getStream())) {
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                // skip if the entry is a directory, if it's not a supported extension or
                // if it's hidden (by convention)
                if (zipEntry.isDirectory() || zipEntry.getName().startsWith(".")) {
                    continue;
                }

                String name = FileManagerUtils.fetchTitle(zipEntry.getName());
                String extension = FileUtils.getFileExtension(name);

                String entryMimetype = registry.getMimetypeFromFilename(name);
                if (supportedZipContent!= null
                        && (supportedZipContent.getMimetypes().contains(entryMimetype)
                            || doesMatchExtension(extension, supportedZipContent.getExtensions()))) {
                    mimetype = entryMimetype;
                    break;
                }
            }
        } catch (IOException e) {
            // just return
        }
        return mimetype;
    }

}
