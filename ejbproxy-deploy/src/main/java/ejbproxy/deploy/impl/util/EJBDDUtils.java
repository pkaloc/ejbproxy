package ejbproxy.deploy.impl.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

public class EJBDDUtils {
	/*
	 * Closes both provided streams.
	 */
	public static void addPrimaryDefaultInterceptor(InputStream ejbDDIn,
			OutputStream ejbDDOut, String interceptor)
			throws TransformerException, SAXException, IOException,
			ParserConfigurationException {
		Document ddDoc = createDOMDocument(ejbDDIn);
		IOUtils.safeClose(ejbDDIn);
		Element ejbJarElement = ddDoc.getDocumentElement();

		// Create or get the 'assembly-descriptor' element
		Element aDesc = (Element) ejbJarElement.getElementsByTagName(
				"assembly-descriptor").item(0);
		if (aDesc == null) {
			aDesc = ddDoc.createElement("assembly-descriptor");

			// The 'assembly-descriptor' element must be specified before the
			// 'ejb-client-jar' element (if exists) to keep the dd valid.
			NodeList eClJarNL = ejbJarElement
					.getElementsByTagName("ejb-client-jar");
			if (eClJarNL.getLength() > 0) {
				ejbJarElement.insertBefore(aDesc, eClJarNL.item(0));
			} else {
				ejbJarElement.appendChild(aDesc);
			}
		}

		// Create and append new first 'interceptor-binding' element
		Element firstIntBinding = (Element) aDesc.getElementsByTagName(
				"interceptor-binding").item(0);
		if (firstIntBinding == null) {
			firstIntBinding = ddDoc.createElement("interceptor-binding");
			// The 'interceptor-binding' element must be specified before the
			// 'message-destination', 'exclude-list' and 'application-exception'
			// elements (if exist) to keep the dd valid.
			Node nextSibling = null;
			if (aDesc.getElementsByTagName("application-exception").getLength() > 0) {
				nextSibling = aDesc.getElementsByTagName(
						"application-exception").item(0);
			}
			if (aDesc.getElementsByTagName("exclude-list").getLength() > 0) {
				nextSibling = aDesc.getElementsByTagName("exclude-list")
						.item(0);
			}
			if (aDesc.getElementsByTagName("message-destination").getLength() > 0) {
				nextSibling = aDesc.getElementsByTagName("message-destination")
						.item(0);
			}
			if (nextSibling == null) {
				aDesc.appendChild(firstIntBinding);
			} else {
				aDesc.insertBefore(firstIntBinding, nextSibling);
			}
		} else {
			Element ongoingFirstIntBinding = ddDoc
					.createElement("interceptor-binding");
			aDesc.insertBefore(ongoingFirstIntBinding, firstIntBinding);
			firstIntBinding = ongoingFirstIntBinding;
		}

		// Set the default interceptor
		Element ejbName = ddDoc.createElement("ejb-name");
		ejbName.setTextContent("*");
		firstIntBinding.appendChild(ejbName);
		Element intClass = ddDoc.createElement("interceptor-class");
		intClass.setTextContent(interceptor);
		firstIntBinding.appendChild(intClass);

		storeDOMDocument(ddDoc, ejbDDOut);
		IOUtils.safeClose(ejbDDOut);
	}

	/*
	 * Closes the stream.
	 */
	public static final String DD_STUB = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<ejb-jar xmlns=\"http://java.sun.com/xml/ns/javaee\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "version=\"3.%1$s\" "
			+ "xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_%1$s.xsd\">\n"
			+ "</ejb-jar>";

	public static void writeDDStub(OutputStream out, String minorVersion)
			throws IOException {
		out.write(String.format(DD_STUB, minorVersion).getBytes("UTF-8"));
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
		Source src = new DOMSource(ddDoc);
		Result dest = new StreamResult(ddOut);
		aTransformer.transform(src, dest);
	}
}
