/**
 *    Copyright 2012 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.meltmedia.cadmium.core.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jodd.lagarto.dom.jerry.Jerry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.meltmedia.cadmium.core.FileSystemManager;
import com.meltmedia.cadmium.core.MavenVector;
import com.meltmedia.cadmium.core.WarInfo;

/**
 * <p>Utility class for war manipulation.</p>
 * 
 * @author Christian Trimble
 * @author John McEntire
 *
 */
public class WarUtils {
  
  /**
   * <p>This method updates a template war with the following settings.</p>
   * @param templateWar The name of the template war to pull from the classpath.
   * @param war The path of an external war to update (optional).
   * @param newWarNames The name to give the new war. (note: only the first element of this list is used.)
   * @param repoUri The uri to a github repo to pull content from.
   * @param branch The branch of the github repo to pull content from.
   * @param configRepoUri The uri to a github repo to pull config from.
   * @param configBranch The branch of the github repo to pull config from.
   * @param domain The domain to bind a vHost to.
   * @param context The context root that this war will deploy to.
   * @param secure A flag to set if this war needs to have its contents password protected.
   * @throws Exception
   */
  public static void updateWar(String templateWar, String war,
      List<String> newWarNames, String repoUri, String branch,
      String configRepoUri, String configBranch, String domain,
      String context, boolean secure) throws Exception {
    ZipFile inZip = null;
    ZipOutputStream outZip = null;
    InputStream in = null;
    OutputStream out = null;
    try {
      if (war != null) {
        if (war.equals(newWarNames.get(0))) {
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

      Properties cadmiumProps = updateProperties(inZip, cadmiumPropertiesEntry,
          repoUri, branch, configRepoUri, configBranch);

      ZipEntry jbossWeb = null;
      jbossWeb = inZip.getEntry("WEB-INF/jboss-web.xml");

      Enumeration<? extends ZipEntry> entries = inZip.entries();
      while (entries.hasMoreElements()) {
        ZipEntry e = entries.nextElement();
        if (e.getName().equals(cadmiumPropertiesEntry.getName())) {
          storeProperties(outZip, cadmiumPropertiesEntry, cadmiumProps,
              newWarNames);
        } else if (((domain != null && domain.length() > 0) || (context != null && context
            .length() > 0)) && e.getName().equals(jbossWeb.getName())) {
          updateDomain(inZip, outZip, jbossWeb, domain, context);
        } else if (secure && e.getName().equals("WEB-INF/web.xml")) {
          addSecurity(inZip, outZip, e);
        } else {
          outZip.putNextEntry(e);
          if (!e.isDirectory()) {
            FileSystemManager.streamCopy(inZip.getInputStream(e), outZip, true);
          }
          outZip.closeEntry();
        }
      }
    } finally {
      if (FileSystemManager.exists("tmp_cadmium-war.war")) {
        new File("tmp_cadmium-war.war").delete();
      }
      try {
        if (inZip != null) {
          inZip.close();
        }
      } catch (Exception e) {
        System.err.println("ERROR: Failed to close " + war);
      }
      try {
        if (outZip != null) {
          outZip.close();
        }
      } catch (Exception e) {
        System.err.println("ERROR: Failed to close " + newWarNames.get(0));
      }
      try {
        if (out != null) {
          out.close();
        }
      } catch (Exception e) {
      }
      try {
        if (in != null) {
          in.close();
        }
      } catch (Exception e) {
      }
    }

  }

  /**
   * This will update the web.xml contained within a zip/war file to add a shiro security filter.
   * @param inZip The zip file to pull the original web.xml file from.
   * @param outZip The zip output stream to write the updated web.xml file to.
   * @param e The zip entry the points to the web.xml file in the zip files.
   * @throws Exception
   */
  private static void addSecurity(ZipFile inZip, ZipOutputStream outZip,
      ZipEntry e) throws Exception {

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(inZip.getInputStream(e));

    Element root = doc.getDocumentElement();

    addListener(doc, root);

    addContextParam(doc, root);

    addFilter(doc, root);

    addFilterMapping(doc, root);

    storeXmlDocument(outZip, e, doc);
  }
  
  /**
   * <p>Adds a child xml element to a parent element relative to other elements with a tagname.</p>
   * <p>If first is true then the child is added before any elements with a tagname, the opposite happens when first is false.</p> 
   * @param parent The parent xml element.
   * @param child The child xml element to add.
   * @param tagname The tagname to be relative to.
   * @param first A flag that describes the relative relationship between the child element and its siblings named by tagname.
   */
  public static void addRelativeTo(Element parent, Element child, String tagname, boolean first) {
    NodeList nodes = parent.getChildNodes();
    if(nodes.getLength() > 0) {
      Node relativeEl = null;
      boolean found = false;
      for(int i=0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if(node.getNodeType() == Node.ELEMENT_NODE) {
          Element el = (Element) node;
          if(el.getTagName().equals(tagname)) {
            if(relativeEl == null || !first) {
              relativeEl = el;
            } else if(first && !found) {
              relativeEl = el;
            }
            found = true;
          }
        }
      }
      if(relativeEl != null && !first) {
        relativeEl = relativeEl.getNextSibling();
      }
      if(relativeEl == null && first) {
        relativeEl = nodes.item(0);
      } else {
        parent.appendChild(child);
      }
      if(relativeEl != null) {
        parent.insertBefore(child, relativeEl);
      }
    } else {
      //There are no elements in the parent node so lets just append child.
      parent.appendChild(child);
    }
  }

  /**
   * Adds a shiro environment listener to load the shiro config file.
   * @param doc The xml DOM document to create the new xml elements with.
   * @param root The xml Element node to add the listener to.
   */
  public static void addListener(Document doc, Element root) {
    Element listener = doc.createElement("listener");
    Element listenerClass = doc.createElement("listener-class");
    listener.appendChild(listenerClass);
    listenerClass.appendChild(doc
        .createTextNode("org.apache.shiro.web.env.EnvironmentLoaderListener"));

    addRelativeTo(root, listener, "listener", true);
  }

  /**
   * Adds a context parameter to a web.xml file to override where the shiro config location is to be loaded from. 
   * The location loaded from will be represented by the "com.meltmedia.cadmium.contentRoot" system property.
   * @param doc The xml DOM document to create the new xml elements with.
   * @param root The xml Element node to add the context param to.
   */
  public static void addContextParam(Document doc, Element root) {
    Element ctxParam = doc.createElement("context-param");
    Element paramName = doc.createElement("param-name");
    paramName.appendChild(doc.createTextNode("shiroConfigLocations"));
    ctxParam.appendChild(paramName);
    Element paramValue = doc.createElement("param-value");
    paramValue.appendChild(doc.createTextNode("file:" + new File(System
        .getProperty("com.meltmedia.cadmium.contentRoot"), "shiro.ini")
        .getAbsoluteFile().getAbsolutePath()));
    ctxParam.appendChild(paramValue);

    addRelativeTo(root, ctxParam, "listener", false);
  }

  /**
   * Adds the shiro filter to a web.xml file.
   * @param doc The xml DOM document to create the new xml elements with.
   * @param root The xml Element node to add the filter to.
   */
  public static void addFilter(Document doc, Element root) {
    Element filter = doc.createElement("filter");
    Element filterName = doc.createElement("filter-name");
    filterName.appendChild(doc.createTextNode("ShiroFilter"));
    filter.appendChild(filterName);
    Element filterClass = doc.createElement("filter-class");
    filterClass.appendChild(doc
        .createTextNode("org.apache.shiro.web.servlet.ShiroFilter"));
    filter.appendChild(filterClass);

    addRelativeTo(root, filter, "filter", true);
  }

  /**
   * Adds the filter mapping for the shiro filter to a web.xml file.
   * @param doc The xml DOM document to create the new xml elements with.
   * @param root The xml Element node to add the filter mapping to.
   */
  public static void addFilterMapping(Document doc, Element root) {
    Element filterMapping = doc.createElement("filter-mapping");
    Element filterName = doc.createElement("filter-name");
    filterName.appendChild(doc.createTextNode("ShiroFilter"));
    filterMapping.appendChild(filterName);
    Element urlPattern = doc.createElement("url-pattern");
    urlPattern.appendChild(doc.createTextNode("/*"));
    filterMapping.appendChild(urlPattern);

    addDispatchers(doc, filterMapping, "REQUEST", "FORWARD", "INCLUDE", "ERROR");

    addRelativeTo(root, filterMapping, "filter-mapping", true);
  }

  /**
   * Adds dispatchers for each item in the vargs parameter names to a 
   * filter mapping element of a web.xml file.
   * @param doc The xml DOM document to create the new xml elements with.
   * @param filterMapping The filter mapping element to append to from a web.xml document.
   * @param names The names of the dispatchers to add.
   */
  public static void addDispatchers(Document doc, Element filterMapping,
      String... names) {
    if (names != null) {
      for (String name : names) {
        Element dispatcher = doc.createElement("dispatcher");
        dispatcher.appendChild(doc.createTextNode(name));
        filterMapping.appendChild(dispatcher);
      }
    }
  }

  /**
   * Adds vHost and context-root mappings to a jboss-web.xml file contained withing a zip/war file.
   * @param inZip The zip file that the original jboss-web.xml file is in.
   * @param outZip The zip output stream to write the updated jboss-web.xml file to.
   * @param jbossWeb The zip element that represents the jboss-web.xml file.
   * @param domain The domain to add a vHost for.
   * @param context The context to add a context-root for.
   * @throws Exception
   */
  public static void updateDomain(ZipFile inZip, ZipOutputStream outZip,
      ZipEntry jbossWeb, String domain, String context) throws Exception {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(inZip.getInputStream(jbossWeb));

    Element rootNode = null;
    NodeList nodes = doc.getElementsByTagName("jboss-web");
    if (nodes.getLength() == 1) {
      rootNode = (Element) nodes.item(0);
    }

    if (domain != null && domain.length() > 0) {
      Element vHost = doc.createElement("virtual-host");
      removeNodesByTagName(rootNode, "virtual-host");
      vHost.appendChild(doc.createTextNode(domain));
      rootNode.appendChild(vHost);
    }

    if (context != null && context.length() > 0) {
      Element cRoot = doc.createElement("context-root");
      removeNodesByTagName(rootNode, "context-root");
      cRoot.appendChild(doc.createTextNode(context));
      rootNode.appendChild(cRoot);
    }

    storeXmlDocument(outZip, jbossWeb, doc);
  }

  /**
   * Writes a xml document to a zip file with an entry specified by the jbossWeb parameter.
   * @param outZip The zip output stream to write to.
   * @param jbossWeb The zip entry to add to the zip file.
   * @param doc The xml DOM document to write to the zip file.
   * @throws IOException 
   * @throws TransformerFactoryConfigurationError
   * @throws TransformerConfigurationException
   * @throws TransformerException
   */
  public static void storeXmlDocument(ZipOutputStream outZip,
      ZipEntry jbossWeb, Document doc) throws IOException,
      TransformerFactoryConfigurationError, TransformerConfigurationException,
      TransformerException {
    jbossWeb = new ZipEntry(jbossWeb.getName());
    outZip.putNextEntry(jbossWeb);

    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer();

    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(outZip);
    transformer.transform(source, result);

    outZip.closeEntry();
  }

  /**
   * Removes elements by a specified tag name from the xml Element passed in.
   * @param doc The xml Element to remove from.
   * @param tagname The tag name to remove.
   */
  public static void removeNodesByTagName(Element doc, String tagname) {
    NodeList nodes = doc.getElementsByTagName(tagname);
    for (int i = 0; i < nodes.getLength(); i++) {
      Node n = nodes.item(i);
      doc.removeChild(n);
    }
  }

  /**
   * Adds a properties file to a war.
   * @param outZip The zip output stream to add to.
   * @param cadmiumPropertiesEntry The entry to add.
   * @param cadmiumProps The properties to store in the zip file.
   * @param newWarNames The first element of this list is used in a comment of the properties file.
   * @throws IOException
   */
  public static void storeProperties(ZipOutputStream outZip,
      ZipEntry cadmiumPropertiesEntry, Properties cadmiumProps,
      List<String> newWarNames) throws IOException {
    ZipEntry newCadmiumEntry = new ZipEntry(cadmiumPropertiesEntry.getName());
    outZip.putNextEntry(newCadmiumEntry);
    cadmiumProps.store(outZip,
        "Initial git properties for " + newWarNames.get(0));
    outZip.closeEntry();
  }

  /**
   * <p>Loads a properties file from a zip file and updates 2 properties in that properties file.</p> 
   * <p>The properties file in the zip is not written back to the zip file with the updates.</p>
   * @param inZip The zip file to load the properties file from.
   * @param cadmiumPropertiesEntry The entry of a properties file in the zip to load.
   * @param repoUri The value to set the "com.meltmedia.cadmium.git.uri" property with.
   * @param branch The value to set the "com.meltmedia.cadmium.branch" property with.
   * @param configRepoUri The value to set the "com.meltmedia.cadmium.config.git.uri" property with.
   * @param configBranch The value to set the "com.meltmedia.cadmium.config.branch" property with.
   * @return The updated properties object that was loaded from the zip file.
   * @throws IOException
   */
  public static Properties updateProperties(ZipFile inZip,
      ZipEntry cadmiumPropertiesEntry, String repoUri, String branch, 
      String configRepoUri, String configBranch)
      throws IOException {
    Properties cadmiumProps = new Properties();
    cadmiumProps.load(inZip.getInputStream(cadmiumPropertiesEntry));

    if (repoUri != null) {
      cadmiumProps.setProperty("com.meltmedia.cadmium.git.uri", repoUri);
    }
    if (branch != null) {
      cadmiumProps.setProperty("com.meltmedia.cadmium.branch", branch);
    }

    if (!StringUtils.isEmptyOrNull(configRepoUri) && !configRepoUri.equals(repoUri)) {
      cadmiumProps.setProperty("com.meltmedia.cadmium.config.git.uri", configRepoUri);
    } 
    if (configBranch != null) {
      cadmiumProps.setProperty("com.meltmedia.cadmium.config.branch", configBranch);
    }
    return cadmiumProps;
  }
  
  /**
   * Reads a Cadmium war file or war directory and returns a {@link WarInfo} 
   * Instance with all of the details about the war.
   * @param war The compressed war file or exploded war directory to get the info about.
   * @return A {@link WarInfo} Instance with the gathered information.
   * @throws Exception
   */
  public static WarInfo getWarInfo(File war) throws Exception {
    WarInfo info = new WarInfo();
    info.setWarName(war.getName());
    CadmiumWar warHelper = null;
    try {
      warHelper = new CadmiumWar(war);
      getDeploymentInfo(info, warHelper);
      getCadmiumInfo(info, warHelper);
      getArtifactInfo(info, warHelper);
    } finally {
      IOUtils.closeQuietly(warHelper);
    }
    return info;
  }

  private static void getArtifactInfo(WarInfo info, CadmiumWar warHelper)
      throws Exception {
    List<String> artifactProps = warHelper.listFilesWithName("META-INF/maven", "pom.properties");
    
    for(String artifactProp : artifactProps) {
      String artProps = warHelper.fileToString(artifactProp);
      Properties props = new Properties();
      props.load(new StringReader(artProps));
      MavenVector mvn = new MavenVector();
      mvn.setGroupId(props.getProperty("groupId"));
      mvn.setArtifactId(props.getProperty("artifactId"));
      mvn.setVersion(props.getProperty("version"));
      info.getArtifacts().add(mvn);
    }
  }

  private static void getCadmiumInfo(WarInfo info, CadmiumWar warHelper)
      throws Exception {
    if(warHelper.fileExists("WEB-INF/cadmium.properties")) {
      String cadmiumProps = warHelper.fileToString("WEB-INF/cadmium.properties");
      Properties props = new Properties();
      props.load(new StringReader(cadmiumProps));
      info.setRepo(props.getProperty("com.meltmedia.cadmium.git.uri"));
      info.setConfigRepo(props.getProperty("com.meltmedia.cadmium.config.git.uri", info.getRepo()));
      info.setContentBranch(props.getProperty("com.meltmedia.cadmium.branch"));
      info.setConfigBranch(props.getProperty("com.meltmedia.cadmium.config.branch"));
    }
  }

  private static void getDeploymentInfo(WarInfo info, CadmiumWar warHelper)
      throws Exception {
    if(warHelper.fileExists("WEB-INF/jboss-web.xml")) {
      String jbossWeb = warHelper.fileToString("WEB-INF/jboss-web.xml");
      Jerry jbossWebJerry = Jerry.jerry(jbossWeb);
      info.setDomain(jbossWebJerry.$("jboss-web > virtual-host").text());
      info.setContext(jbossWebJerry.$("jboss-web > context-root").text());
    }
  }
  
  /**
   * A class used to read the contents of a zipped or exploded war.
   * 
   * @author John McEntire
   *
   */
  public static class CadmiumWar implements Closeable {
    private File warFile;
    private ZipFile zippedWar = null;
    
    /**
     * Creates an instance if the given warFile exists and is a directory or a zip file.
     * @param warFile
     * @throws Exception
     */
    public CadmiumWar(File warFile) throws Exception {
      this.warFile = warFile;
      if(warFile.exists() && !warFile.isDirectory()) {
        zippedWar = new ZipFile(warFile);
      } else if(!warFile.isDirectory()) {
        throw new Exception(warFile + " does not exist.");
      }
    }
    
    /**
     * Tests if the war file that backs this helper class contains a file.
     * 
     * @param fileName
     * @return
     */
    public boolean fileExists(String fileName) {
      if(zippedWar != null) {
        ZipEntry fileEntry = zippedWar.getEntry(fileName);
        return fileEntry != null;
      }
      return new File(warFile, fileName).exists();
    }
    
    /**
     * Lists all files with the given fileName under a parent directory within 
     * the war that backs this instance.  Files will be listed recursively.
     * @param parent
     * @param fileName
     * @return
     */
    public List<String> listFilesWithName(String parent, String fileName) {
      List<String> files = new ArrayList<String>();
      if(zippedWar != null) {
        Enumeration<?> entries = zippedWar.entries();
        while(entries.hasMoreElements()) {
          ZipEntry entry = (ZipEntry) entries.nextElement();
          if(parent == null || entry.getName().startsWith(parent)) {
            files.add(entry.getName());
          }
        }
      } else if((parent == null ? warFile : new File(warFile, parent)).exists()){
        Collection<File> filesUnderParent = FileUtils.listFiles(parent == null ? warFile : new File(warFile, parent), null, true);
        for(File file : filesUnderParent) {
          files.add(file.getAbsoluteFile().getAbsolutePath().replaceFirst(Pattern.quote(warFile.getAbsoluteFile().getAbsolutePath()), ""));
        }
      }
      Iterator<String> itr = files.iterator();
      while(itr.hasNext()) {
        String theFileName = new File(itr.next()).getName();
        if(!theFileName.equals(fileName)) {
          itr.remove();
        }
      }
      return files;
    }
    
    /**
     * Returns the string contents of a files contained within the war that backs this instance.
     * @param file
     * @return
     * @throws Exception
     */
    public String fileToString(String file) throws Exception {
      if(zippedWar != null) {
        ZipEntry entry = zippedWar.getEntry(file);
        if(entry != null) {
          InputStream in = null;
          try {
            in = zippedWar.getInputStream(entry);
            return IOUtils.toString(in);
          } finally {
            IOUtils.closeQuietly(in);
          }
        }
      } else {
        return FileUtils.readFileToString(new File(warFile, file));
      }
      return null;
    }

    @Override
    public void close() throws IOException {
      if(zippedWar != null) {
        zippedWar.close();
      }
    }
  }
}
