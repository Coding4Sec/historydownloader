# Command Line Tool for Downloading Historical Stock Data from Interactive Brokers API

This tool connects to a locally running TWS or IB Gateway to download historical data from their API and write it into a csv file.

## TODO
- Understand and solve any rate limiting problems when downloading a lot of data
- Remove unnecessary code (the reference client provided by IB). Or have contributors install all of it separate instead of bloating this repo?

## To Build:
    make build
    
## To Run:
1. Login to TWS or IB Gateway, and make sure it is configured to allow connections on port 7496.
2. Run a simple download with a lot of default parameters:

    java -cp bin historydownloader.Main -s MSFT

3. You may have to click "Accept" on the TWS or IB Gateway application.
4. Inspect the output:

    tail output.csv

5. Play around with the other parameters:

    java -cp bin historydownloader.Main -help
