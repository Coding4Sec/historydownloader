package historydownloader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.NewContract;
import com.ib.controller.Types;
import com.ib.controller.Types.BarSize;
import com.ib.controller.Types.DurationUnit;
import com.ib.controller.Types.WhatToShow;

public class Main implements IConnectionHandler {

	private static final ConsoleLogger m_inLogger = new ConsoleLogger("log-in: ");
	private static final ConsoleLogger m_outLogger = new ConsoleLogger("log-out: ");
	
	private static ApiController m_apiController;
	
	// Command-line options
	private static String hostAddress = "127.0.0.1";
	private static int hostPort = 7496;
	private static int clientId = 1111;
	
	private static String outputPath = "output.csv";
	
	private static String symbol;
	private static String endDateTime; // Defaults to today
	private static int durationAmt = 1;
	private static DurationUnit durationUnit = DurationUnit.DAY;
	private static BarSize barSize = BarSize._1_min;
	private static boolean regularTradingHoursOnly = false;
		
	private static void parseCommandLineOptions(String[] args) {
		// http://commons.apache.org/proper/commons-cli/introduction.html
		Options options = new Options();
		
		// Define arguments
		options.addOption("host", true, "The host address to connect to (e.g. 127.0.0.1)");
		options.addOption("port", true, "The host port to connect to (e.g. 7496)");
		options.addOption("clientId", true, "The clientId to use when connecting to the host.");
		
		options.addOption("outputPath", true, "The .csv file you wish to be written.");
		
		options.addOption("s", true, "The symbol you wish to download historical data about.");
		options.addOption("edt", true, "End date time: The last day of the window you wish to download from. "
				+ "Format: YYYYMMDD hh:mm:ss");
		options.addOption("da", true, "Duration Amount: the length of time you wish to download.");
		options.addOption("du", true, "Duration Unit: The unit of time to download over. Accepted values = SECOND, DAY, WEEK, MONTH, YEAR");
		options.addOption("bs", true, "Bar Size: The granularity of data to return. Accepted values = "
				+ "_1_secs, _5_secs, _10_secs, _15_secs, _30_secs, _1_min, _2_mins, _3_mins, _5_mins, _10_mins, _15_mins, _20_mins, _30_mins, _1_hour, _4_hours, _1_day, _1_week");
		options.addOption("rth", false, "When specified, will download data only from regular trading hours.");
		
		options.addOption("help", false, "Print this help message");
		
		// Parse arguments
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Interrogate values
		if (cmd.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "java -cp bin historydownloader.Main", options );
			System.exit(1);
		}
		
		if (cmd.hasOption("host")) {
			hostAddress = cmd.getOptionValue("host");
		}
		
		if (cmd.hasOption("port")) {
			hostPort = Integer.valueOf(cmd.getOptionValue("port"));
		}
		
		if (cmd.hasOption("clientId")) {
			clientId = Integer.valueOf(cmd.getOptionValue("clientId"));
		}
		
		if (cmd.hasOption("outputPath")) {
			outputPath = cmd.getOptionValue("outputPath");
		}
		
		if (cmd.hasOption("s")) {
			symbol = cmd.getOptionValue("s");
		} else {
			System.err.println("Symbol required.");
			System.exit(1);
		}
		
		if (cmd.hasOption("edt")) {
			endDateTime = cmd.getOptionValue("edt");
		} else {
			// Default to today
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			Date date = new Date();
			endDateTime = dateFormat.format(date);
		}
		
		if (cmd.hasOption("da")) {
			durationAmt = Integer.valueOf(cmd.getOptionValue("da"));
		}
		
		if (cmd.hasOption("du")) {
			// http://stackoverflow.com/questions/604424/java-convert-string-to-enum
			durationUnit = DurationUnit.valueOf(cmd.getOptionValue("du"));
		}
		
		if (cmd.hasOption("bs")) {
			barSize = BarSize.valueOf(cmd.getOptionValue("bs"));
		}
		
		if (cmd.hasOption("rth")) {
			regularTradingHoursOnly = true;
		}
	}
	
	
	public static void main(String[] args) {
		parseCommandLineOptions(args);
		
		// Connect to the API
		m_apiController = new ApiController(new Main(), m_inLogger, m_outLogger);
		m_apiController.connect(hostAddress, hostPort, clientId);
		
		// Create an IHistoricalData handler to receive data and write it
		// to a CSV file.
		HistoricalDataCsvWriter csvWriter = new HistoricalDataCsvWriter(
				outputPath, 
				new Runnable() {
					@Override public void run() {
						m_apiController.disconnect();
					}
			});
		
		// Start querying data from the API and streaming it into the CSV file
		m_apiController.reqHistoricalData(
				getContract(symbol), 
				endDateTime,
				durationAmt,
				durationUnit,
				barSize,
				WhatToShow.TRADES,
				regularTradingHoursOnly,
				csvWriter);
	}

	private static NewContract getContract(String symbol) {
		NewContract contract = new NewContract();
		
		contract.symbol(symbol.toUpperCase());
		
		contract.secType(Types.SecType.STK);
		
		// TODO Make these configurable?
		contract.exchange("SMART");
		contract.primaryExch("ISLAND");
		contract.currency("USD");
		
		// TODO fill in other aspects of the contract?
		
		return contract;
	}
	
	@Override
	public void connected() {
		System.out.println("Connected!");
	}

	@Override
	public void disconnected() {
		System.out.println("Disconnected!");
	}

	@Override
	public void accountList(ArrayList<String> list) {
		System.out.println("Received account list:");
		for(String acct : list) {
			System.out.println("  " + acct);
		}
	}

	@Override
	public void error(Exception e) {
		System.out.println("Connection exception: " + e.toString());
	}

	@Override
	public void message(int id, int errorCode, String errorMsg) {
		System.out.println("Connection message: " + id + " " + errorCode + " " + errorMsg);
	}

	@Override
	public void show(String string) {
		System.out.println("Connection show: " + string);
	}

}
