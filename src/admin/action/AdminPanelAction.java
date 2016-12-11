package admin.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import domain.DropdownOption;

/**
 * This is to demonstrate modification of certain texts in WEB-INF/web.xml <br/>
 * Assumed is application is run extracted, so web.xml is a real file that can be modified. <br/>
 * Only text content is exposed to modification, so XML structure remains safely unmodified. <br/>
 * Any XML special characters will be escaped. <br/>
 */
public class AdminPanelAction extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String DROPDOWN_OPTIONS_ATTRIBUTE_NAME = "dropdownOptions";

	/** Name of the session attribute holding the parsed web.xml as a {@link Document} */
	public static final String DEPLOYMENT_DESCRIPTOR_ATTTRIBUTE_NAME = "deploymentDescriptorDocument";

	/** Name of the servlet context attribute, the presence of which indicates WEB-INF/web.xml is under rewriting by another thread */
	public static final String DEPLOYMENT_DESCRIPTOR_LOCK_ATTRIBUTE_NAME = "deploymentDescriptorLocked";

	private final Collection<DropdownOption> options = new ArrayList<DropdownOption>() {{
		add(new DropdownOption("-- Select here --", "", true, true));
		add(new DropdownOption("Option 1", "valueOne", false, false));
		add(new DropdownOption("Option 2", "valueTwo", false, false));
		add(new DropdownOption("Option 3", "valueThree", false, false));
		add(new DropdownOption("Option 4", "valueFour", false, false));
	}};

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminPanelAction() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.getServletContext().setAttribute("dropdownOptionsAttributeName", DROPDOWN_OPTIONS_ATTRIBUTE_NAME);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try {
			request.setAttribute(DROPDOWN_OPTIONS_ATTRIBUTE_NAME, this.getDropdownOptions(request));
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		request.getRequestDispatcher("/WEB-INF/secure/admin/admin_panel.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String paramOne = request.getParameter("paramOne");
		if(paramOne != null && this.canDeploymentDescriptorBeModified()) {
			this.startDeploymentDescriptorModification();
			try {
				final Document doc = this.getDeploymentDescriptor(request);
				final XPath xPath = XPathFactory.newInstance().newXPath();
				this.setContextParamValue("example-param", paramOne, doc, xPath);
				this.saveDeploymentDescriptor(doc);
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				this.stopDeploymentDescriptorModification();
			}
		}
		doGet(request, response);
	}

	private boolean canDeploymentDescriptorBeModified() {
		return this.getServletContext().getAttribute(DEPLOYMENT_DESCRIPTOR_LOCK_ATTRIBUTE_NAME) == null;
	}

	private void startDeploymentDescriptorModification() {
		this.getServletContext().setAttribute(DEPLOYMENT_DESCRIPTOR_LOCK_ATTRIBUTE_NAME, new Object());
	}

	private void stopDeploymentDescriptorModification() {
		this.getServletContext().removeAttribute(DEPLOYMENT_DESCRIPTOR_LOCK_ATTRIBUTE_NAME);
	}

	private Collection<DropdownOption> getDropdownOptions(final HttpServletRequest request) 
			throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		final Document doc = this.getDeploymentDescriptor(request);
		final XPath xPath = XPathFactory.newInstance().newXPath();
		final String exampleParamValue = this.getContextParamValue("example-param", doc, xPath);
		if(exampleParamValue != null) {
			final List<DropdownOption> exampleParamDropdownOptions = new LinkedList<DropdownOption>();
			for(final DropdownOption option : this.options) {
				if(option.getValue().equals(exampleParamValue)) {
					exampleParamDropdownOptions.add(new DropdownOption(option.getLabel(), option.getValue(), true, option.isDisabled()));
				} else {
					exampleParamDropdownOptions.add(option);
				}
			}
			return exampleParamDropdownOptions;
		}
		return null;
	}
	
	private Document getDeploymentDescriptor(final HttpServletRequest request)
			throws SAXException, IOException, ParserConfigurationException {
		final Object deploymentDescriptorObj = request.getSession().getAttribute(DEPLOYMENT_DESCRIPTOR_ATTTRIBUTE_NAME);
		if(deploymentDescriptorObj != null && Document.class.isAssignableFrom(deploymentDescriptorObj.getClass())) {
			return (Document)deploymentDescriptorObj;
		} else {
			final InputStream is = getServletContext().getResourceAsStream("/WEB-INF/web.xml");
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringComments(false);
			dbf.setIgnoringElementContentWhitespace(false);
			final Document deploymentDescriptorDocument = dbf.newDocumentBuilder().parse(is);
			request.getSession().setAttribute(DEPLOYMENT_DESCRIPTOR_ATTTRIBUTE_NAME, deploymentDescriptorDocument);
			return deploymentDescriptorDocument;
		}
	}
	
	private boolean saveDeploymentDescriptor(final Document doc) 
			throws TransformerConfigurationException, UnsupportedEncodingException, FileNotFoundException, TransformerException, TransformerFactoryConfigurationError {
		final File deploymentDescriptorFile = new File(this.getServletContext().getRealPath("/WEB-INF/web.xml"));
		if(deploymentDescriptorFile.exists() && deploymentDescriptorFile.canWrite()) {
			TransformerFactory.newInstance().newTransformer().transform(
				new DOMSource(doc), 
				new StreamResult(new OutputStreamWriter(new FileOutputStream(deploymentDescriptorFile), "UTF-8")));
			return true;
		}
		return false;
	}
	
	private String getContextParamValue(final String contextParamName, final Document doc, final XPath xPath) 
			throws XPathExpressionException {
		final Element paramValueElement = (Element)xPath.evaluate(
			String.format("/web-app/context-param[normalize-space(param-name)='%s'][1]/param-value[1]", contextParamName), 
			doc.getDocumentElement(),
			XPathConstants.NODE);
		if(paramValueElement != null) {
			return paramValueElement.getTextContent().trim();
		}
		return null;
	}
	
	private void setContextParamValue(final String contextParamName, final String contextParamValue, final Document doc, final XPath xPath) 
			throws XPathExpressionException {
		final Element paramValueElement = (Element)xPath.evaluate(
			String.format("/web-app/context-param[normalize-space(param-name)='%s'][1]/param-value[1]", contextParamName), 
			doc.getDocumentElement(),
			XPathConstants.NODE);
		if(paramValueElement != null) {
			paramValueElement.setTextContent(contextParamValue);
		}
	}
}
