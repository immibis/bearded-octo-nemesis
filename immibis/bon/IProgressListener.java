package immibis.bon;

public interface IProgressListener {
	public void start(int max, String text);
	public void set(int value);
}
