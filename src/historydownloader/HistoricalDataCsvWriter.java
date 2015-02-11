package historydownloader;

import java.io.IOException;

import com.csvreader.CsvWriter;
import com.ib.controller.ApiController.IHistoricalDataHandler;
import com.ib.controller.Bar;
import com.ib.controller.Formats;

public class HistoricalDataCsvWriter implements IHistoricalDataHandler {

	private Runnable m_doneDownloading;
	private CsvWriter m_csvWriter;
	
	public HistoricalDataCsvWriter(String filePath, Runnable doneDownloading) {
		this.m_csvWriter = new CsvWriter(filePath);
		
		// TODO: Write headers?
		
		this.m_doneDownloading = doneDownloading;
	}
	
	@Override
	public void historicalData(Bar bar, boolean hasGaps) {
		// System.out.println("Bar = " + bar.toString());
		
		String[] values = {
				Formats.fmtDate(bar.time() * 1000),
				String.valueOf(bar.high()),
				String.valueOf(bar.low()),
				String.valueOf(bar.open()),
				String.valueOf(bar.close()),
				String.valueOf(bar.wap()),
				String.valueOf(bar.volume()),
				String.valueOf(bar.count())
		};
		
		try {
			this.m_csvWriter.writeRecord(values);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void historicalDataEnd() {
		System.out.println("Historical Data ended.");
		this.m_csvWriter.close();
		
		// Notify main thread that we're done and the application can end.
		this.m_doneDownloading.run();
	}

}
