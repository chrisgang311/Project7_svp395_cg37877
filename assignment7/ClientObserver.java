/* Client Observer <ClientObserver.java>
 * EE422C Project 7 submission by
 * <Samuel Patterson>
 * <svp395>
 * <16455>
 * <Christopher Gang>
 * <cg37877>
 * <16450>
 * Slip days used: <1>
 * Fall 2016
 */

package assignment7;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ClientObserver extends PrintWriter implements Observer {

	public ClientObserver(OutputStream out) {
		super(out);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		this.println(arg);
		this.flush();
	}

}
