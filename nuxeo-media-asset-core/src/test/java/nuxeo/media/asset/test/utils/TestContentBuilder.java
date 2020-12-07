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

package nuxeo.media.asset.test.utils;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;

public class TestContentBuilder {

    protected CoreSession session;

    protected String path;

    protected String name = "DOCUMENT";

    protected String type = "File";

    protected String filePath;

    protected Map<String, Serializable> properties = new HashMap<>();

    protected Map<String, String> permissions = new HashMap<>();

    private TestContentBuilder(CoreSession session) {
        this.session = session;
        this.path = session.getRootDocument().getPathAsString();
    }

    public TestContentBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public TestContentBuilder setParent(DocumentModel doc) {
        this.path = doc.getPathAsString();
        return this;
    }

    public TestContentBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public TestContentBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public TestContentBuilder setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public TestContentBuilder setProperty(String xpath, Serializable property) {
        this.properties.put(xpath, property);
        return this;
    }

    public TestContentBuilder setProperties(Map<String, Serializable> properties) {
        this.properties = properties;
        return this;
    }

    public TestContentBuilder setPermission(String username, String permission) {
        this.permissions.put(username, permission);
        return this;
    }

    public TestContentBuilder setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
        return this;
    }

    public static TestContentBuilder newInstance(CoreSession session) {
        return new TestContentBuilder(session);
    }

    public DocumentModel build() {
        DocumentModel doc = session.createDocumentModel(path, name, type);
        doc.setPropertyValue("dc:title", name);
        if (filePath != null) {
            Blob blob = new FileBlob(new File(getClass().getResource(filePath).getPath()));
            MimetypeRegistry registry = Framework.getService(MimetypeRegistry.class);
            String mimetype = registry.getMimetypeFromFilenameAndBlobWithDefault(blob.getFilename(), blob, null);
            if (mimetype != null) {
                blob.setMimeType(mimetype);
            }
            doc.setPropertyValue("file:content", (Serializable) blob);
        }
        for (Map.Entry<String, Serializable> entry : properties.entrySet()) {
            doc.setPropertyValue(entry.getKey(), entry.getValue());
        }

        doc = session.createDocument(doc);

        ACP acp = doc.getACP() != null ? doc.getACP() : new ACPImpl();
        for (Map.Entry<String, String> entry : permissions.entrySet()) {
            ACE ace = ACE.builder(entry.getKey(), entry.getValue()).creator(session.getPrincipal().getName()).build();
            acp.addACE("test", ace);
        }
        doc.setACP(acp, true);
        doc = session.saveDocument(doc);

        return doc;
    }
}
