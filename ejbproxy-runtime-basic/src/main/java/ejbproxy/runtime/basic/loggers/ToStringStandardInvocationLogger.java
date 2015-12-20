package ejbproxy.runtime.basic.loggers;

/**
 * Simple standard logger which uses the <code>toString</code> method to log
 * invocations´ result/exception.
 */
public class ToStringStandardInvocationLogger extends
		AbstractStandardInvocationLogger {
	@Override
	protected String convertResultToString(Object result) {
		return result.toString();
	}
}
