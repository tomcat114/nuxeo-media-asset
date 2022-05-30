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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject("mediatype")
public class MediaTypeDescriptor implements Serializable {

    public static final List<String> DEFAULT_MIMETYPES = new ArrayList<>();

    public static final List<String> DEFAULT_EXTENSIONS = new ArrayList<>();

    public static final List<String> DEFAULT_FACETS = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    @XNode("@name")
    protected String name;

    @XNodeList(value = "mimetype", type = ArrayList.class, componentType = String.class)
    protected List<String> mimetypes = DEFAULT_MIMETYPES;

    @XNodeList(value = "extension", type = ArrayList.class, componentType = String.class)
    protected List<String> extensions = DEFAULT_EXTENSIONS;

    @XNodeList(value = "facet", type = ArrayList.class, componentType = String.class)
    protected List<String> facets = DEFAULT_FACETS;

    @XNode("@enabled")
    boolean enabled = true;

    @XNode("@order")
    private Integer order;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMimetypes() {
        return mimetypes;
    }

    public void setMimetypes(List<String> mimetypes) {
        this.mimetypes = mimetypes;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public List<String> getFacets() {
        return facets;
    }

    public void setFacets(List<String> facets) {
        this.facets = facets;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Integer getOrder() {
        return order;
    }

}
