package ejbproxy.runtime.configuration.web;

import ejbproxy.runtime.configuration.web.util.RuntimeConfigurationOperationInvoker;

import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class WebConfigServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private RuntimeConfigurationOperationInvoker opInvoker;

	@Override
	public void init() throws ServletException {
		try {
			opInvoker = new RuntimeConfigurationOperationInvoker(
					new ObjectName(getServletConfig().getInitParameter(
							"runtime-config-objectname")));
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String jndiBinding = req.getParameter("jndiBinding");
		String type = req.getParameter("type");
		String value = refineValue(type, req.getParameter("value"));

		resp.setContentType("text/html;charset=UTF-8");
		PrintWriter writer = resp.getWriter();

		printHeader(writer, jndiBinding);
		printSeparator(writer);
		if (type != null && value != null) {
			try {
				if (jndiBinding != null) {
					setCustomConfigParam(jndiBinding, type, value);
				} else {
					setDefaultConfigParam(req.getParameter("force") != null,
							type, value);
				}
				printConfigSetSuccessMessage(writer);
				printSeparator(writer);
			} catch (Exception e) {
				printConfigSetErrorMessage(writer, e);
				printSeparator(writer);
			}
		}

		try {
			if (jndiBinding != null) {
				printCustomConfigForms(writer,
						getInitParameter("context-path"), jndiBinding, type,
						loadCustomConfigData(jndiBinding, type),
						loadDefaultConfigData(type));
			} else {
				printDefaultConfigForms(writer,
						getInitParameter("context-path"), type,
						loadDefaultConfigData(type),
						req.getParameter("force") != null);
			}
		} catch (Exception e) {
			printConfigGetErrorMessage(writer, e);
		}

		printSeparator(writer);
		printHelp(writer, jndiBinding);

		printEnd(writer);
	}

	// --- Data-loading utilities ---

	private String refineValue(String type, String origValue) {
		if ("ejbproxy-enabled".equals(type) || "logging-enabled".equals(type)) {
			return origValue != null ? "true" : "false";
		} else {
			return origValue;
		}
	}

	private Object[] loadDefaultConfigData(String type) throws Exception {
		// if (type != null) {
		// return new Object[] { getDefaultConfigParam(type) };
		// } else {
		Object[] data = new Object[6];
		data[0] = getDefaultConfigParam("ejbproxy-enabled");
		data[1] = getDefaultConfigParam("endpoints");
		data[2] = getDefaultConfigParam("handler");
		data[3] = getDefaultConfigParam("logging-enabled");
		data[4] = getDefaultConfigParam("logger");
		data[5] = getDefaultConfigParam("logger-name");
		return data;
		// }
	}

	private Object[] loadCustomConfigData(String jndiBinding, String type)
			throws Exception {
		// if (type != null) {
		// return new Object[] { getCustomConfigParam(jndiBinding, type) };
		// } else {
		Object[] data = new Object[6];
		data[0] = getCustomConfigParam(jndiBinding, "ejbproxy-enabled");
		data[1] = getCustomConfigParam(jndiBinding, "endpoints");
		data[2] = getCustomConfigParam(jndiBinding, "handler");
		data[3] = getCustomConfigParam(jndiBinding, "logging-enabled");
		data[4] = getCustomConfigParam(jndiBinding, "logger");
		data[5] = getCustomConfigParam(jndiBinding, "logger-name");
		return data;
		// }
	}

	private Object getCustomConfigParam(String jndiBinding, String type)
			throws Exception {
		if (type.equals("ejbproxy-enabled")) {
			return opInvoker.isEJBProxyEnabled(jndiBinding);
		} else if (type.equals("endpoints")) {
			return opInvoker.getEndpoints(jndiBinding);
		} else if (type.equals("handler")) {
			return opInvoker.getHandler(jndiBinding);
		} else if (type.equals("logging-enabled")) {
			return opInvoker.isLoggingEnabled(jndiBinding);
		} else if (type.equals("logger")) {
			return opInvoker.getLogger(jndiBinding);
		} else if (type.equals("logger-name")) {
			return opInvoker.getLoggerName(jndiBinding);
		} else {
			throw new UnsupportedOperationException("Illegal 'type' specified!");
		}
	}

	private Object getDefaultConfigParam(String type) throws Exception {
		if (type.equals("ejbproxy-enabled")) {
			return opInvoker.isEJBProxyEnabled();
		} else if (type.equals("endpoints")) {
			return opInvoker.getDefaultEndpoints();
		} else if (type.equals("handler")) {
			return opInvoker.getDefaultHandler();
		} else if (type.equals("logging-enabled")) {
			return opInvoker.isLoggingEnabled();
		} else if (type.equals("logger")) {
			return opInvoker.getDefaultLogger();
		} else if (type.equals("logger-name")) {
			return opInvoker.getDefaultLoggerName();
		} else {
			throw new UnsupportedOperationException("Illegal 'type' specified!");
		}
	}

	private void setCustomConfigParam(String jndiBinding, String type,
			String value) throws Exception {
		if (type.equals("ejbproxy-enabled")) {
			opInvoker.setEJBProxyEnabled(jndiBinding, Boolean.valueOf(value));
		} else if (type.equals("endpoints")) {
			opInvoker.setEndpoints(jndiBinding,
					Arrays.asList(value.split("\\s*(,|\\s)\\s*")));
		} else if (type.equals("handler")) {
			opInvoker.setHandler(jndiBinding, value);
		} else if (type.equals("logging-enabled")) {
			opInvoker.setLoggingEnabled(jndiBinding, Boolean.valueOf(value));
		} else if (type.equals("logger")) {
			opInvoker.setLogger(jndiBinding, value);
		} else if (type.equals("logger-name")) {
			opInvoker.setLoggerName(jndiBinding, value);
		} else {
			throw new UnsupportedOperationException("Illegal 'type' specified!");
		}
	}

	private void setDefaultConfigParam(boolean force, String type, String value)
			throws Exception {
		if (type.equals("ejbproxy-enabled")) {
			opInvoker.setEJBProxyEnabled(Boolean.valueOf(value), force);
		} else if (type.equals("endpoints")) {
			opInvoker.setDefaultEndpoints(
					Arrays.asList(value.split("\\s*(,|\\s)\\s*")), force);
		} else if (type.equals("handler")) {
			opInvoker.setDefaultHandler(value, force);
		} else if (type.equals("logging-enabled")) {
			opInvoker.setLoggingEnabled(Boolean.valueOf(value), force);
		} else if (type.equals("logger")) {
			opInvoker.setDefaultLogger(value, force);
		} else if (type.equals("logger-name")) {
			opInvoker.setDefaultLoggerName(value, force);
		} else {
			throw new UnsupportedOperationException("Illegal 'type' specified!");
		}
	}

	// --- Printing utilities ---

	private static void printHeader(PrintWriter writer, String jndiBinding) {
		writer.print("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
		writer.printf(
				"<HTML><HEAD><STYLE TYPE=\"text/css\" MEDIA=\"screen\">body{font-family:monospace;}.error-msg{color:red;}.result-msg{color: green;}.hint{color:gray;font-style:italic;}table,th,tr,td{border-width:0;}.label-cell{width: 170px;}.input-cell{width: 160px;}.submit-cell{width: 50px;}</STYLE><TITLE>EJBProxy settings: - %s</TITLE></HEAD><BODY>",
				jndiBinding == null ? "default" : jndiBinding);
		writer.printf("<H1>EJBProxy %s settings:</H1>",
				jndiBinding == null ? "<EM>default</EM>" : "'<EM>"
						+ jndiBinding + "</EM>' ejb");
	}

	private static void printConfigSetSuccessMessage(PrintWriter writer) {
		writer.print("<H3 CLASS=\"result-msg\">Configuration updated successfully.</H3>");
	}

	private static void printConfigSetErrorMessage(PrintWriter writer,
			Exception e) {
		writer.printf(
				"<H3 CLASS=\"error-msg\">Configuration could not be updated! An error occurred: %s</H3>",
				e.getCause() != null ? e.getCause() : e);
	}

	private static void printConfigGetErrorMessage(PrintWriter writer,
			Exception e) {
		writer.printf(
				"<H3 CLASS=\"error-msg\">Configuration could not be fetched! An error occurred: %s</H3>",
				e.getCause() != null ? e.getCause() : e);
	}

	private static void printSeparator(PrintWriter writer) {
		writer.print("<HR>");
	}

	private static void printDefaultConfigForms(PrintWriter writer,
			String actionURL, String type, Object[] data, boolean force) {
		// if (type == null || type.equals("ejbproxy-enabled")) {
		printDefaultConfigElementForm(writer, actionURL, "ejbproxy-enabled",
				"EJBProxy enabled", "CHECKBOX", data[0], force);
		// }
		// if (type == null || type.equals("endpoints")) {
		printDefaultConfigElementForm(writer, actionURL, "endpoints",
				"Endpoints", "TEXT", /* type == null ? */
				data[1]/* : data[0] */, force);
		// }
		// if (type == null || type.equals("handler")) {
		printDefaultConfigElementForm(writer, actionURL, "handler", "Handler",
				"TEXT", /* type == null ? */data[2]/* : data[0] */, force);
		// }
		// if (type == null || type.equals("logging-enabled")) {
		printDefaultConfigElementForm(writer, actionURL, "logging-enabled",
				"Logging enabled", "CHECKBOX",
				/* type == null ? */data[3]/* : data[0] */, force);
		// }
		// if (type == null || type.equals("logger")) {
		printDefaultConfigElementForm(writer, actionURL, "logger", "Logger",
				"TEXT", /* type == null ? */data[4]/* : data[0] */, force);
		// }
		// if (type == null || type.equals("logger-name")) {
		printDefaultConfigElementForm(writer, actionURL, "logger-name",
				"Logger name", "TEXT", /* type == null ? */
				data[5]/* : data[0] */, force);
		// }
	}

	private static void printCustomConfigForms(PrintWriter writer,
			String actionURL, String jndiBinding, String type, Object[] data,
			Object[] defData) {
		// if (type == null || type.equals("ejbproxy-enabled")) {
		printCustomConfigElementForm(writer, actionURL, jndiBinding,
				"ejbproxy-enabled", "EJBProxy enabled", "CHECKBOX", data[0],
				defData[0]);
		// }
		// if (type == null || type.equals("endpoints")) {
		printCustomConfigElementForm(writer, actionURL, jndiBinding,
				"endpoints", "Endpoints", "TEXT", /* type == null ? */
				data[1]/* : data[0] */,
				/* type == null ? */defData[1]/* : defData[0] */);
		// }
		// if (type == null || type.equals("handler")) {
		printCustomConfigElementForm(writer, actionURL, jndiBinding, "handler",
				"Handler", "TEXT", /* type == null ? */
				data[2]/* : data[0] */,
				/* type == null ? */defData[2]/* : defData[0] */);
		// }
		// if (type == null || type.equals("logging-enabled")) {
		printCustomConfigElementForm(writer, actionURL, jndiBinding,
				"logging-enabled", "Logging enabled", "CHECKBOX",
				/* type == null ? */data[3]/* : data[0] */, /* type == null ? */
				defData[3]/* : defData[0] */);
		// }
		// if (type == null || type.equals("logger")) {
		printCustomConfigElementForm(writer, actionURL, jndiBinding, "logger",
				"Logger", "TEXT", /* type == null ? */data[4]/* : data[0] */,
				/* type == null ? */defData[4]/* : defData[0] */);
		// }
		// if (type == null || type.equals("logger-name")) {
		printCustomConfigElementForm(writer, actionURL, jndiBinding,
				"logger-name", "Logger name", "TEXT", /* type == null ? */
				data[5]/* : data[0] */,
				/* type == null ? */defData[5]/* : defData[0] */);
		// }
	}

	private static void printDefaultConfigElementForm(PrintWriter writer,
			String actionURL, String type, String label, String inputType,
			Object value, boolean force) {
		writer.printf("<FORM METHOD=\"GET\" ACTION=\"%s\"><TABLE><TR>",
				actionURL);

		if (value instanceof Iterable<?>) {
			value = createCommaSeparatedString(((Iterable<?>) value).iterator());
		}

		writer.printf(
				"<TD CLASS=\"label-cell\"><P><LABEL FOR=\"%s\">%s: </LABEL></P></TD>",
				type, label);
		if (inputType.equals("CHECKBOX")) {
			writer.printf(
					"<TD CLASS=\"input-cell\"><INPUT TYPE=\"%s\" ID=\"%s\" NAME=\"value\" %s></TD>",
					inputType, type, (value instanceof Boolean && (Boolean) value) ? "VALUE=\"true\" CHECKED=\"CHECKED\"": "VALUE=\"false\"");
		} else {
			writer.printf(
					"<TD CLASS=\"input-cell\"><INPUT TYPE=\"%s\" ID=\"%s\" NAME=\"value\" VALUE=\"%s\"></TD>",
					inputType, type, value);
		}
		writer.printf(
				"<TD CLASS=\"submit-cell\"><INPUT TYPE=\"HIDDEN\" NAME=\"type\" VALUE=\"%s\"><INPUT TYPE=\"SUBMIT\" VALUE=\"Set\"></TD>",
				type);
		String forceCheckboxID = "force-" + new Random().nextInt(1000);
		writer.printf(
				"<TD CLASS=\"reset-cell\"><INPUT TYPE=\"CHECKBOX\" ID=\"%s\" NAME=\"force\" VALUE=\"true\" %s><LABEL FOR=\"%s\">&nbsp;reset</LABEL></TD>",
				forceCheckboxID, force ? "CHECKED=\"CHECKED\"" : "",
				forceCheckboxID);

		writer.print("</TR></TABLE></FORM>");
	}

	private static void printCustomConfigElementForm(PrintWriter writer,
			String actionURL, String jndiBinding, String type, String label,
			String inputType, Object value, Object defValue) {
		boolean defValueEffective = false;
		if (value == null) {
			value = defValue;
			defValueEffective = true;
		}
		if (value instanceof Iterable<?>) {
			value = createCommaSeparatedString(((Iterable<?>) value).iterator());
		}

		writer.printf("<FORM METHOD=\"GET\" ACTION=\"%s\"><TABLE><TR>",
				actionURL);

		writer.printf(
				"<TD CLASS=\"label-cell\"><P><LABEL FOR=\"%s\">%s: </LABEL></P></TD>",
				type, label);
		if (inputType.equals("CHECKBOX")) {
			writer.printf(
					"<TD CLASS=\"input-cell\"><INPUT TYPE=\"%s\" ID=\"%s\" NAME=\"value\" VALUE=\"true\" %s>%s</TD>",
					inputType,
					type,
					(value != null && (Boolean) value) ? "CHECKED=\"CHECKED\""
							: "",
					defValueEffective ? "<BR><SPAN CLASS=\"hint\">(inherited)</SPAN>"
							: "");
		} else {
			writer.printf(
					"<TD CLASS=\"input-cell\"><INPUT TYPE=\"%s\" ID=\"%s\" NAME=\"value\" VALUE=\"%s\">%s</TD>",
					inputType,
					type,
					value,
					defValueEffective ? "<BR><SPAN CLASS=\"hint\">(inherited)</SPAN>"
							: "");
		}
		writer.printf(
				"<TD CLASS=\"submit-cell\"><INPUT TYPE=\"HIDDEN\" NAME=\"jndiBinding\" VALUE=\"%s\"><INPUT TYPE=\"HIDDEN\" NAME=\"type\" VALUE=\"%s\"><INPUT TYPE=\"SUBMIT\" VALUE=\"Set\"></TD>",
				jndiBinding, type);

		writer.print("</TR></TABLE></FORM>");
	}

	private void printHelp(PrintWriter writer, String jndiBinding) {
		StringBuilder helpText = new StringBuilder();
		List<Object> args = new ArrayList<Object>();

		helpText.append("<DIV CLASS=\"hint\">");
		helpText.append("<H3>Concise help:</H3>");
		helpText.append("<P>Hello. This is the web configuration interface of EJBProxy.</P>");
		helpText.append("<P>You are currently configuring the %s settings.</P>");
		args.add(jndiBinding == null ? "<STRONG>default</STRONG>" : "'<STRONG>"
				+ jndiBinding + "</STRONG>' ejb");

		if (jndiBinding != null) {
			helpText.append("<P>So, any ongoing change will <STRONG>apply to the this ejb only</STRONG>.</P>");
			helpText.append("<P>Hint: To modify the <STRONG>default</STRONG> settings which applies to all ejbs, <STRONG>omit the 'jndiBinding'</STRONG> request parameter, please.</P>");
			helpText.append("<P>The '<STRONG>(inherited)</STRONG>' text mean that the given configuration parameter´s value is inherited from the <STRONG>default</STRONG> settings. So, if you change the value within default settings, the change will also apply to this ejb.</P>");
		} else {
			helpText.append("<P>So, any ongoing change will <STRONG>apply to either all or all-without-custom-value ejbs</STRONG>. All-without-custom-value ejbs are such ejbs which have not overridden the default value by a custom value.</P>");
			helpText.append("<P>Hint: To modify individual ejb´s settings, <STRONG>specify the 'jndiBinding'</STRONG> request parameter, please.</P>");
			helpText.append("<P>If you do the update with the '<STRONG>reset</STRONG>' checkbox checked, the update will apply to ALL ejbs. Even those which currently have their values set to some custom value. That is, have not inherited it from the default settings.</P>");
		}
		helpText.append("<P>Every configuration parameter is updated totally independently from others.</P>");
		helpText.append("<P>Settings updates are <STRONG>synchronous</STRONG>. That is, you should see your new settings <STRONG>immediately on response</STRONG>.</P>");

		helpText.append("<P>Config. parameters explanation:</P>");
		helpText.append("<DL>");
		helpText.append("<DT>EJBProxy enabled</DT>");
		helpText.append("<DD>Specifies whether to allow the EJBProxy at all. That is, whether calls upon the given ejb(s) will be proxied or left untouched by the EJBProxy.</DD>");
		helpText.append("<DT>Endpoints</DT>");
		helpText.append("<DD>Specifies ejb containers which will be made available to the corresponding invocation handlers from subsequent invocations on. Note: endpoints are referenced by their logical names which were given to them by the EJBProxy deployer within the initial configuration. Note2: Individual names must be separated with comma or space.</DD>");
		helpText.append("<DT>Handler</DT>");
		helpText.append("<DD>Specifies the invocation handler which will proxy subsequent invocations upon the given ejb(s). Note: the handler is referenced by its logical name which was given to it by the EJBProxy deployer within the initial configuration.</DD>");
		helpText.append("<DT>Logging enabled</DT>");
		helpText.append("<DD>Marks whether invocations upon the given ejb(s) are logged or not.</DD>");
		helpText.append("<DT>Logger</DT>");
		helpText.append("<DD>Specifies the invocation logger which will log subsequent invocations upon the given ejb(s). Note: the logger is referenced by its logical name which was given to it by the EJBProxy deployer within the initial configuration.</DD>");
		helpText.append("<DT>Logger name</DT>");
		helpText.append("<DD>Specifies the name under which the corresponding invocation logger will log.</DD>");
		helpText.append("</DL>");

		helpText.append("</DIV>");

		writer.printf(helpText.toString(), args.toArray());
	}

	private static void printEnd(PrintWriter writer) {
		writer.print("</BODY></HTML>");
		writer.close();
	}

	// --- Utilities ---

	private static String createCommaSeparatedString(Iterator<?> it) {
		StringBuilder result = new StringBuilder();
		while (it.hasNext()) {
			result.append(it.next());
			if (it.hasNext()) {
				result.append(", ");
			}
		}
		return result.toString();
	}
}
