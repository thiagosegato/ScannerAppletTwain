package libs;
import com.sun.jna.win32.StdCallLibrary;
public interface Biblioteca extends StdCallLibrary {
	//public void Teste(byte[] msg, byte[] title);
	public void selectSource();
	public boolean aquire(String imgTitle);
}