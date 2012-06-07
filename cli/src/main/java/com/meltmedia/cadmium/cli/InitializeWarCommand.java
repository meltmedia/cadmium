package com.meltmedia.cadmium.cli;

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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.meltmedia.cadmium.core.FileSystemManager;

@Parameters(commandDescription="Initializes a new sites war from an existing war.", separators="=")
public class InitializeWarCommand {

  @Parameter(names="--existingWar", description="Path to an existing cadmium war.", required=false)
  private String war;
  
  @Parameter(names="--repo", description="Uri to remote github repo.", required=false)
  private String repoUri;
  
  @Parameter(names={"--branch","--tag"}, description="Initial branch to serve content from.", required=false)
  private String branch;
  
  @Parameter(names="--domain", description="Sets the domain name that this war will bind to.", required=false)
  private String domain;
  
  @Parameter(description="\"new war name\"", required=true, arity=1)
  private List<String> newWarNames;
  
  public void execute() throws Exception {
    if(war == null || FileSystemManager.canRead(war)) {
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
          File tmpZip = File.createTempFile("cadmium-basic-war", "war");
          tmpZip.delete();
          tmpZip.deleteOnExit();
          in = InitializeWarCommand.class.getClassLoader().getResourceAsStream("cadmium-basic-war.war");
          out = new FileOutputStream(tmpZip);
          FileSystemManager.streamCopy(in, out);
          inZip = new ZipFile(tmpZip);
        }
        outZip = new ZipOutputStream(new FileOutputStream(newWarNames.get(0)));
        
        ZipEntry cadmiumPropertiesEntry = null;
        cadmiumPropertiesEntry = inZip.getEntry("WEB-INF/cadmium.properties");
        
        Properties cadmiumProps = updateProperties(inZip,
            cadmiumPropertiesEntry);
        
        ZipEntry jbossWeb = null;
        jbossWeb = inZip.getEntry("WEB-INF/jboss-web.xml");
        
        Enumeration<? extends ZipEntry> entries = inZip.entries();
        while (entries.hasMoreElements()) {
          ZipEntry e = entries.nextElement();
          if(e.getName().equals(cadmiumPropertiesEntry.getName())) {
            storeProperties(outZip, cadmiumPropertiesEntry, cadmiumProps);
          } else if (domain != null && e.getName().equals(jbossWeb.getName())) {
            updateDomain(inZip, outZip, jbossWeb);
          } else {
            outZip.putNextEntry(e);
            if (!e.isDirectory()) {
                FileSystemManager.streamCopy(inZip.getInputStream(e), outZip, true);
            }
            outZip.closeEntry();
          }
        }
      } finally {
        if(FileSystemManager.exists("tmp_cadmium-basic-war.war")) {
          new File("tmp_cadmium-basic-war.war").delete();
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
    } else {
      System.err.println("ERROR: \""+war+"\" does not exist or cannot be read.");
      System.exit(1);
    }
  }

  private void updateDomain(ZipFile inZip, ZipOutputStream outZip, ZipEntry jbossWeb)
      throws Exception {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(inZip.getInputStream(jbossWeb));
    
    Element rootNode = null;
    NodeList nodes = doc.getElementsByTagName("jboss-web");
    if(nodes.getLength() == 1) {
      rootNode = (Element) nodes.item(0);
    }
    
    Element vHost = doc.createElement("virtual-host");
    vHost.appendChild(doc.createTextNode(domain));
    
    Element cRoot = doc.createElement("context-root");
    cRoot.appendChild(doc.createTextNode("/"));
    
    removeNodesByTagName(rootNode, "context-root");
    removeNodesByTagName(rootNode, "virtual-host");
    
    rootNode.appendChild(cRoot);
    rootNode.appendChild(vHost);
    
    storeXmlDocument(outZip, jbossWeb, doc);
  }

  private void storeXmlDocument(ZipOutputStream outZip, ZipEntry jbossWeb,
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

  private void removeNodesByTagName(Element doc, String tagname) {
    NodeList nodes = doc.getElementsByTagName(tagname);
    for(int i = 0; i < nodes.getLength(); i++) {
      Node n = nodes.item(i);
      doc.removeChild(n);
    }
  }

  private void storeProperties(ZipOutputStream outZip,
      ZipEntry cadmiumPropertiesEntry, Properties cadmiumProps)
      throws IOException {
    ZipEntry newCadmiumEntry = new ZipEntry(cadmiumPropertiesEntry.getName());
    outZip.putNextEntry(newCadmiumEntry);
    cadmiumProps.store(outZip, "Initial git properties for " + newWarNames.get(0));
    outZip.closeEntry();
  }

  private Properties updateProperties(ZipFile inZip,
      ZipEntry cadmiumPropertiesEntry) throws IOException {
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
