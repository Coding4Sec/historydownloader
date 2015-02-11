package historydownloader;

import com.ib.controller.ApiConnection.ILogger;

public class ConsoleLogger implements ILogger {

	private String prefix;
	
	public ConsoleLogger() {
		this("");
	}
	
	public ConsoleLogger(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public void log(String valueOf) {
		// TODO: Format. Apparently valueOf is like 1 character at a time
		// Also, I'm not sure how valuable this information even is.
		//System.out.println(this.prefix + valueOf);
	}

}
