## Description

This plugin extends the Nuxeo Platform to automatically apply the correct Media preview facets to Documents

## How to build
```
git clone https://github.com/nuxeo-sandbox/nuxeo-media-asset
cd nuxeo-media-asset
mvn clean install
```

## Java Plugin Features
### Single Asset Type
Managing several media types like images and videos with a single document type improves the UX. Users don't have to chose anymore between document types like Audio, Picture, Video, File and ThreeD which are all same from a functional point of view.
Depending on the main file mimetype, the correct media facet is dynamically applied to the single document.

#### MediaAssetService
The service provides several extension points:
- mimetype / document facets mapping

```
<extension target="nuxeo.media.asset.service" point="mediatype">
    <mediatype name="Video" order="2">
        <mimetype>video/.*</mimetype>
        <mimetype>application/gxf</mimetype>
        <mimetype>application/mxf</mimetype>
        <facet>Video</facet>
        <facet>HasVideoPreview</facet>
        <facet>HasStoryboard</facet>
    </mediatype>
</extension>
```

- for zip files, a list of supported mimetypes within the archive

```
<extension target="nuxeo.media.asset.service" point="supportedZipContent">
    <mimetypes>
        <mimetype>model/vnd.collada\+xml</mimetype>
        <mimetype>application/x-3ds</mimetype>
        <mimetype>text/x-c</mimetype>
        <mimetype>text/wavefront-obj</mimetype>
        <mimetype>model/x3d\+xml</mimetype>
        <mimetype>application/sla</mimetype>
        <mimetype>model/gltf\+json</mimetype>
    </mimetypes>
</extension>
```  

- A filter to determine if a document should be processed by the service

```
<extension point="filters" target="org.nuxeo.ecm.platform.actions.ActionService">
    <filter id="nuxeo.media.asset.service.default.filter">
        <rule grant="true">
            <schema>file</schema>
        </rule>
    </filter>
</extension>
```

#### MediaAssetListener
The listener runs for the event aboutTocreate, beforeDocumentModification and aboutToImport. It uses the Service described above to apply the correct facets to the document depending on the type of the main file

#### MediaAssetFileImporter
A custom filemanager importer plugin which leverages the MediaAssetService

## Known limitations
This plugin is a work in progress.

## About Nuxeo
[Nuxeo](www.nuxeo.com), developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia.

Learn more at www.nuxeo.com.
