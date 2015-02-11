build:
	javac -d bin/ -cp src \
		src/apidemo/util/*.java \
		src/apidemo/*.java \
		src/org/apache/commons/cli/*.java \
		src/com/csvreader/*.java \
		src/com/ib/client/*.java \
		src/com/ib/contracts/*.java \
		src/com/ib/controller/*.java \
		src/historydownloader/*.java
		
run:
	java -cp bin historydownloader.Main   
	