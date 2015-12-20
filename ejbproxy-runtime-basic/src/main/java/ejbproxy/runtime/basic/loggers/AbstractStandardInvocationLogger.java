package ejbproxy.runtime.basic.loggers;

import ejbproxy.runtime.configuration.Configurable;
import ejbproxy.runtime.invocation.AbstractInvocationLogger;
import ejbproxy.runtime.invocation.InvocationOnEJB;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.text.StrBuilder;
import org.slf4j.MDC;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Random;

/**
 * Standard invocation logger which delegates result/exception to string
 * conversion to descendants. All other things (including log messages format)
 * are already implemented by this logger.
 */
public abstract class AbstractStandardInvocationLogger extends
		AbstractInvocationLogger implements Configurable {
	private static final String INIT_MSG_FORMAT = "BEGIN(%s) %s.%s(%s)";
	private static final String RESULT_MSG_FORMAT = "END(%s) %s.%s(%s)[%n%s%n]";
	private static final String EXCEPTION_MSG_FORMAT = "EXCEPTION(%s) %s.%s(%s)[%n%s%n]";

	private static final String LOG_UUID_KEY = "StandardInvocationLogger.UUID_KEY";

	private static final String EMPTY_ARGS_SIGNATURE = "";
	private static final String OMIT_ARGS_SIGNATURE_KEY = "omit_args_sign";
	private static final String OMIT_ARGS_SIGNATURE_VALUE = "--args-omited--";
	private static final String ARGS_ITEM_SEPARATOR = ", ";

	private static final String NULL_VALUE_REPLACEMENT = "<null>";
	private static final String VOID_RETURN_TYPE_REPLACEMENT = "<void>";

	private boolean omitResultExceptionArgumentsSignature;
	private final Random randomNumberGen;

	public AbstractStandardInvocationLogger() {
		omitResultExceptionArgumentsSignature = true;
		randomNumberGen = new Random();
	}

	@Override
	protected String doLogInit(InvocationOnEJB invocation) {
		String logUUID = String.valueOf(Math.abs(randomNumberGen.nextLong()));
		MDC.put(LOG_UUID_KEY, logUUID);

		return String.format(INIT_MSG_FORMAT, logUUID, invocation.getMethod()
				.getDeclaringClass().getName(), invocation.getMethod()
				.getName(), createArgumentsSignatureString(invocation
				.getArguments()));
	}

	@Override
	protected String doLogResult(InvocationOnEJB invocation, Object result) {
		String resultAsString = result == null ? invocation.getMethod()
				.getGenericReturnType() == Void.TYPE ? VOID_RETURN_TYPE_REPLACEMENT
				: NULL_VALUE_REPLACEMENT
				: convertResultToString(result);
		return String
				.format(RESULT_MSG_FORMAT,
						MDC.get(LOG_UUID_KEY),
						invocation.getMethod().getDeclaringClass().getName(),
						invocation.getMethod().getName(),
						omitResultExceptionArgumentsSignature ? OMIT_ARGS_SIGNATURE_VALUE
								: createArgumentsSignatureString(invocation
										.getArguments()), resultAsString);
	}

	@Override
	protected String doLogException(InvocationOnEJB invocation,
			Throwable throwable) {
		return String
				.format(EXCEPTION_MSG_FORMAT,
						MDC.get(LOG_UUID_KEY),
						invocation.getMethod().getDeclaringClass().getName(),
						invocation.getMethod().getName(),
						omitResultExceptionArgumentsSignature ? OMIT_ARGS_SIGNATURE_VALUE
								: createArgumentsSignatureString(invocation
										.getArguments()),
						convertExceptionToString(throwable));
	}

	/**
	 * The <code>throwable</code> is never <code>null</code>.
	 */
	protected String convertExceptionToString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}

	/**
	 * The <code>result</code> is never <code>null</code>.
	 */
	protected abstract String convertResultToString(Object result);

	// --- Configurable API ---

	@Override
	public void configure(Map<String, String> configuration) {
		if (configuration.containsKey(OMIT_ARGS_SIGNATURE_KEY)) {
			omitResultExceptionArgumentsSignature = Boolean
					.valueOf(configuration.get(OMIT_ARGS_SIGNATURE_KEY));
		}
	}

	// --- Utilities ---

	private static String createArgumentsSignatureString(Object[] args) {
		if (args.length == 0) {
			return EMPTY_ARGS_SIGNATURE;
		} else {
			StrBuilder signature = new StrBuilder();
			for (int i = 0; i < args.length; i++) {
				if (args[i] != null && args[i].getClass().isArray()) {
					signature.append(ToStringBuilder.reflectionToString(
							args[i], ToStringStyle.SIMPLE_STYLE));
				} else {
					signature.append(args[i]);
				}

				if (i < args.length - 1) {
					signature.append(ARGS_ITEM_SEPARATOR);
				}
			}
			return signature.toString();
		}
	}
}
