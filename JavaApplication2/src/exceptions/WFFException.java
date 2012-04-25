package exceptions;
/**
 * Wrong File Format Exception. Describes the wrong file format by carrying the
 * message.
 *
 * @author Egidijus Lukauskas
 */
public class WFFException extends Exception {
	protected String message = "";
	
	public WFFException(String msg) {
		this.message = msg;
	}
	
	public String getMessage(){
	   return "Netinkamas failo formatas! " + this.message;
    }
}
