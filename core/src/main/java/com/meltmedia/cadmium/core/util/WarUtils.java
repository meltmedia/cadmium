/**
 *   Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.meltmedia.cadmium.core.FileSystemManager;

public class WarUtils {
  public static void updateWar(String templateWar, String war, List<String> newWarNames, String repoUri, String branch, String domain, String context) throws Exception {
    ZipFile inZip = null;
    ZipOutputStream outZip = null;
    InputStream in = null;
    OutputStream out = null;
    try{
      if(war != null) {
        if(war.equals(newWarNames.get(0))) {
          File tmpZip = File.createTempFile(war, null);
          tmpZip.delete();
          tmpZip.deleteOnExit();
          new File(war).renameTo(tmpZip);
          war = tmpZip.getAbsolutePath();
        }
        inZip = new ZipFile(war);
      } else {
        File tmpZip = File.createTempFile("cadmium-war", "war");
        tmpZip.delete();
        tmpZip.deleteOnExit();
        in = WarUtils.class.getClassLoader().getResourceAsStream(templateWar);
        out = new FileOutputStream(tmpZip);
        FileSystemManager.streamCopy(in, out);
        inZip = new ZipFile(tmpZip);
      }
      outZip = new ZipOutputStream(new FileOutputStream(newWarNames.get(0)));

      ZipEntry cadmiumPropertiesEntry = null;
      cadmiumPropertiesEntry = inZip.getEntry("WEB-INF/cadmium.properties");

      Properties cadmiumProps = updateProperties(inZip,
          cadmiumPropertiesEntry, repoUri, branch);

      ZipEntry jbossWeb = null;
      jbossWeb = inZip.getEntry("WEB-INF/jboss-web.xml");

      Enumeration<? extends ZipEntry> entries = inZip.entries();
      while (entries.hasMoreElements()) {
        ZipEntry e = entries.nextElement();
        if(e.getName().equals(cadmiumPropertiesEntry.getName())) {
          storeProperties(outZip, cadmiumPropertiesEntry, cadmiumProps, newWarNames);
        } else if (((domain != null && domain.length() > 0) || (context != null && context.length() > 0)) && e.getName().equals(jbossWeb.getName())) {
          updateDomain(inZip, outZip, jbossWeb, domain, context);
        } else {
          outZip.putNextEntry(e);
          if (!e.isDirectory()) {
            FileSystemManager.streamCopy(inZip.getInputStream(e), outZip, true);
          }
          outZip.closeEntry();
        }
      }
    } finally {
      if(FileSystemManager.exists("tmp_cadmium-war.war")) {
        new File("tmp_cadmium-war.war").delete();
      }
      try{
        if(inZip != null) {
          inZip.close();
        }
      } catch(Exception e) {
        System.err.println("ERROR: Failed to close "+war);
      }
      try{
        if(outZip != null) {
          outZip.close();
        }
      } catch(Exception e) {
        System.err.println("ERROR: Failed to close "+newWarNames.get(0));
      }
      try{
        if(out != null) {
          out.close();
        }
      } catch(Exception e) {
      }
      try{
        if(in != null) {
          in.close();
        }
      } catch(Exception e) {
      }
    }

}

public static void updateDomain(ZipFile inZip, ZipOutputStream outZip, ZipEntry jbossWeb, String domain, String context)
throws Exception {
DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
Document doc = docBuilder.parse(inZip.getInputStream(jbossWeb));

Element rootNode = null;
NodeList nodes = doc.getElementsByTagName("jboss-web");
if(nodes.getLength() == 1) {
  rootNode = (Element) nodes.item(0);
}

if(domain != null && domain.length() > 0) {
  Element vHost = doc.createElement("virtual-host");
  removeNodesByTagName(rootNode, "virtual-host");
  vHost.appendChild(doc.createTextNode(domain));
  rootNode.appendChild(vHost);
}

if(context != null && context.length() > 0) {
  Element cRoot = doc.createElement("context-root");
  removeNodesByTagName(rootNode, "context-root");
  cRoot.appendChild(doc.createTextNode(context));
  rootNode.appendChild(cRoot);
}


storeXmlDocument(outZip, jbossWeb, doc);
}

public static void storeXmlDocument(ZipOutputStream outZip, ZipEntry jbossWeb,
  Document doc) throws IOException, TransformerFactoryConfigurationError,
  TransformerConfigurationException, TransformerException {
jbossWeb = new ZipEntry(jbossWeb.getName());
outZip.putNextEntry(jbossWeb);

TransformerFactory tFactory = TransformerFactory.newInstance();
Transformer transformer = tFactory.newTransformer();

DOMSource source = new DOMSource(doc);
StreamResult result = new StreamResult(outZip);
transformer.transform(source, result);

outZip.closeEntry();
}

public static void removeNodesByTagName(Element doc, String tagname) {
NodeList nodes = doc.getElementsByTagName(tagname);
for(int i = 0; i < nodes.getLength(); i++) {
  Node n = nodes.item(i);
  doc.removeChild(n);
}
}

public static void storeProperties(ZipOutputStream outZip,
  ZipEntry cadmiumPropertiesEntry, Properties cadmiumProps, List<String>newWarNames)
throws IOException {
ZipEntry newCadmiumEntry = new ZipEntry(cadmiumPropertiesEntry.getName());
outZip.putNextEntry(newCadmiumEntry);
cadmiumProps.store(outZip, "Initial git properties for " + newWarNames.get(0));
outZip.closeEntry();
}

public static Properties updateProperties(ZipFile inZip,
  ZipEntry cadmiumPropertiesEntry, String repoUri, String branch) throws IOException {
Properties cadmiumProps = new Properties();
cadmiumProps.load(inZip.getInputStream(cadmiumPropertiesEntry));

if(repoUri != null) {
  cadmiumProps.setProperty("com.meltmedia.cadmium.git.uri", repoUri);
}
if(branch != null) {
  cadmiumProps.setProperty("com.meltmedia.cadmium.branch", branch);
}
return cadmiumProps;
}
}
