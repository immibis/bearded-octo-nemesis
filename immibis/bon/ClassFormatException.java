package immibis.bon;

public class ClassFormatException extends Exception {
	static final long serialVersionUID = 1L;

	public ClassFormatException(String message) {
		super(message);
	}

	public ClassFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}