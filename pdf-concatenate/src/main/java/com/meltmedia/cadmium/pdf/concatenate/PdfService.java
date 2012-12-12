package com.meltmedia.cadmium.pdf.concatenate;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.meltmedia.cadmium.core.CadmiumApiEndpoint;

@CadmiumApiEndpoint
@Path("/concatenate")
public class PdfService {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private Pattern ownerPasswordPattern;
	
	@Path("{fileName}")
	public Response pdfService(@PathParam("fileName") String fileName, @QueryParam("srcUrl") String[] srcUrls,@Context HttpServletResponse resp) {
		if (srcUrls != null && srcUrls.length > 0) {

      String currentUrl = "";
      List<PdfFile> fileList = new LinkedList<PdfFile>();
      try {
        for (String url : srcUrls) {
          currentUrl = url;

          String password = null;
          Matcher m = getOwnerPasswordPattern().matcher(url);
          if (m.find()) {
            password = m.group(3);

            // this will fix file URLs like file:///password@host/...
            url = url.replaceAll("///.*@", "///");

            // this will fix the remaining URLs
            url = url.replaceAll("//.*@", "//");

            fileList.add(new PdfFile(new URL(url), password));
          } else {
            fileList.add(new PdfFile(new URL(url), null));
          }
        } 

       concatenate(fileList, resp.getOutputStream());
      }catch(Exception e) {
      	
      }
		}
		return Response.ok().build();
	}

	private Pattern getOwnerPasswordPattern() {
    if (ownerPasswordPattern == null) {
      ownerPasswordPattern = Pattern.compile("^(https?|ftp|file):///?((.*)@)");
    }
    return ownerPasswordPattern;
  }
		
	public void concatenate(List<PdfFile> pdfFiles, OutputStream os) throws IOException, DocumentException {

    Document document = new Document();

    PdfCopy copy = new PdfCopy(document, os);

    document.open();

    PdfReader reader = null;
    int pageCount;
    String path = null;

    try {
      for (PdfFile f : pdfFiles) {
        path = f.getUrl().toString();

        if (f.getPassword() != null) {
          reader = new PdfReader(f.getUrl(), f.getPassword().getBytes());
        } else {
          reader = new PdfReader(f.getUrl());
        }

        pageCount = reader.getNumberOfPages();
        for (int page = 1; page <= pageCount; page++) {
          copy.addPage(copy.getImportedPage(reader, page));
        }
        reader.close();
        reader = null;
      }
      document.close();
      document = null;
    } catch (IOException ioe) {
      log.error("Error reading pdf: " + path, ioe);
      throw ioe;
    } catch (DocumentException de) {
    	log.error("Error processing pdf: " + path, de);
      throw de;
    } catch (RuntimeException re) {
    	log.error("Runtime Error processing pdf: " + path, re);
      throw re;
    } finally {
      if (reader != null) reader.close();
      if (document != null) document.close();
    }
  }

}
