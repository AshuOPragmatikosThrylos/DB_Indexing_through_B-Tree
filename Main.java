import ui.*;
import java.io.*;

public class Main {

	public static void main(String[] args) throws Exception {

		//basedir takes either the supplied path to create data and indices directories or it creates them in the current directory
		DataManager.basedir = args.length !=0 ? args[0] : new File(".").getAbsolutePath();
		new App();
	}
}