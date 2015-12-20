package ejbproxy.deploy.impl.util;

import ejbproxy.deploy.ExtraJavaEEModule;
import ejbproxy.deploy.JavaEEModuleDescriptor;
import ejbproxy.deploy.JavaEEModuleDescriptor.Type;
import ejbproxy.deploy.WebJavaEEModuleDescriptor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class JavaEEDDUtils {
	/*
	 * Closes the stream.
	 */
	public static List<JavaEEModuleDescriptor> getSpecifiedEjbAndWebModules(
			InputStream dd) throws SAXException, IOException,
			ParserConfigurationException, DOMException, URISyntaxException {
		List<JavaEEModuleDescriptor> ddModules = new ArrayList<JavaEEModuleDescriptor>();

		Document ddDoc = createDOMDocument(dd);
		IOUtils.safeClose(dd);

		NodeList ejbModuleElems = ddDoc.getElementsByTagName("ejb");
		for (int i = 0; i < ejbModuleElems.getLength(); i++) {
			ddModules.add(new JavaEEModuleDescriptor(Type.EJB, new URI(
					ejbModuleElems.item(i).getTextContent())));
		}

		NodeList webURIModuleElems = ddDoc.getElementsByTagName("web-uri");
		for (int i = 0; i < webURIModuleElems.getLength(); i++) {
			ddModules.add(new JavaEEModuleDescriptor(Type.WEB, new URI(
					webURIModuleElems.item(i).getTextContent())));
		}

		return ddModules;
	}

	/*
	 * Closes both provided streams.
	 */
	public static void addExtraModules(InputStream ddIn, OutputStream ddOut,
			List<ExtraJavaEEModule> modules) throws TransformerException,
			SAXException, IOException, ParserConfigurationException {
		Document ddDoc = createDOMDocument(ddIn);
		IOUtils.safeClose(ddIn);
		Element applicationElement = ddDoc.getDocumentElement();

		for (ExtraJavaEEModule eModule : modules) {
			Element moduleElem = ddDoc.createElement("module");
			switch (eModule.getDescriptor().getType()) {
			case EJB:
				Element ejbElem = ddDoc.createElement("ejb");
				ejbElem.setTextContent(eModule.getDescriptor().getURI()
						.getPath());
				moduleElem.appendChild(ejbElem);
				break;
			case WEB:
				Element webElem = ddDoc.createElement("web");
				Element webUriElem = ddDoc.createElement("web-uri");
				webUriElem.setTextContent(eModule.getDescriptor().getURI()
						.getPath());
				webElem.appendChild(webUriElem);
				Element ctxRootElem = ddDoc.createElement("context-root");
				ctxRootElem.setTextContent(((WebJavaEEModuleDescriptor) eModule
						.getDescriptor()).getContextRoot());
				webElem.appendChild(ctxRootElem);
				moduleElem.appendChild(webElem);
				break;
			case JAVA:
				Element javaElem = ddDoc.createElement("java");
				javaElem.setTextContent(eModule.getDescriptor().getURI()
						.getPath());
				moduleElem.appendChild(javaElem);
				break;
			case CONNECTOR:
				Element connectorElem = ddDoc.createElement("connector");
				connectorElem.setTextContent(eModule.getDescriptor().getURI()
						.getPath());
				moduleElem.appendChild(connectorElem);
				break;
			default:
				throw new AssertionError();
			}
			applicationElement.appendChild(moduleElem);
		}

		storeDOMDocument(ddDoc, ddOut);
		IOUtils.safeClose(ddOut);
	}

	/*
	 * Closes the stream.
	 */
	public static final String DD_STUB = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<!DOCTYPE application PUBLIC "
			+ "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN "
			+ "http://java.sun.com/dtd/application_1_3.dtd\">\n"
			+ "<application></application>";

	public static void writeDDStub(OutputStream out) throws IOException {
		out.write(DD_STUB.getBytes("UTF-8"));
		IOUtils.safeClose(out);
	}

	// --- Utilities ---

	private static Document createDOMDocument(InputStream dd)
			throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		domFactory.setCoalescing(true);
		domFactory.setIgnoringComments(true);

		DocumentBuilder docBuilder = domFactory.newDocumentBuilder();
		docBuilder.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(String publicId, String systemId) {
				return new InputSource(new StringReader(""));
			}
		});

		return docBuilder.parse(dd);
	}

	private static void storeDOMDocument(Document ddDoc, OutputStream ddOut)
			throws TransformerException {
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer aTransformer = tranFactory.newTransformer();
		aTransformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
				"-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN");
		aTransformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
				"http://java.sun.com/dtd/application_1_3.dtd");
		Source src = new DOMSource(ddDoc);
		Result dest = new StreamResult(ddOut);
		aTransformer.transform(src, dest);
	}
}
