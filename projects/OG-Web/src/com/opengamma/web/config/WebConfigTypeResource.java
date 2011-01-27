/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgWriter;
import org.fudgemsg.xml.FudgeXMLStreamWriter;
import org.joda.beans.impl.flexi.FlexiBean;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * RESTful resource for a configuration document.
 * 
 * @param <T>  the config element type
 */
@Path("/configs/{type}/{configId}")
public class WebConfigTypeResource<T> extends AbstractWebConfigTypeResource<T> {

  /**
   * The Fudge context.
   */
  protected static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebConfigTypeResource(final AbstractWebConfigTypeResource<T> parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("configs/configtype.ftl", out);
  }

  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response put(
      @FormParam("name") String name,
      @FormParam("configxml") String xml) {
    if (data().getConfig().isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(get()).build();
    }
    
    name = StringUtils.trimToNull(name);
    xml = StringUtils.trimToNull(xml);
    if (name == null || xml == null) {
      FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (xml == null) {
        out.put("err_xmlMissing", true);
      }
      String html = getFreemarker().build("configs/configtype-update.ftl", out);
      return Response.ok(html).build();
    }
    
    // System.out.println(xml);  // cannot parse to Fudge yet
    
    ConfigDocument<T> oldDoc = data().getConfig();
    ConfigDocument<T> doc = new ConfigDocument<T>();
    doc.setUniqueId(oldDoc.getUniqueId());
    doc.setName(name);
    doc.setValue(oldDoc.getValue());
    doc = data().getConfigTypeMaster().update(doc);
    data().setConfig(doc);
    URI uri = WebConfigTypeResource.uri(data());
    return Response.seeOther(uri).build();
  }

  @DELETE
  public Response delete() {
    ConfigDocument<?> doc = data().getConfig();
    if (doc.isLatest() == false) {
      return Response.status(Status.FORBIDDEN).entity(get()).build();
    }
    
    data().getConfigTypeMaster().remove(doc.getUniqueId());
    URI uri = WebConfigsResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ConfigDocument<?> doc = data().getConfig();
    out.put("configDoc", doc);
    out.put("config", doc.getValue());
    
    // get xml and pretty print it
    FudgeMsgEnvelope msg = FUDGE_CONTEXT.toFudgeMsg(doc.getValue());
    StringWriter buf = new StringWriter(1024);
    FudgeMsgWriter writer = new FudgeMsgWriter(new FudgeXMLStreamWriter(FUDGE_CONTEXT, buf));
    writer.writeMessageEnvelope(msg);
    try {
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document xmlDoc = db.parse(new InputSource(new StringReader(buf.toString())));
      xmlDoc.getDocumentElement().normalize();
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      DOMSource source = new DOMSource(xmlDoc);
      buf = new StringWriter(1024);
      StreamResult result =  new StreamResult(buf);
      transformer.transform(source, result);
      String str = buf.toString();
      out.put("configXml", str);
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
    
    out.put("deleted", !doc.isLatest());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebConfigTypeVersionsResource<T> findVersions() {
    return new WebConfigTypeVersionsResource<T>(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebConfigData<?> data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideConfigId  the override config id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebConfigData<?> data, final UniqueIdentifier overrideConfigId) {
    String typeStr = data.getTypeMap().inverse().get(data.getType());
    String configId = data.getBestConfigUriId(overrideConfigId);
    return data.getUriInfo().getBaseUriBuilder().path(WebConfigTypeResource.class).build(typeStr, configId);
  }

}
