package immibis.bon;

public class ClassFormatException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public ClassFormatException(String message) {
		super(message);
	}
	
	public ClassFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
