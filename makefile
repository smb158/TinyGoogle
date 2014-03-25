default:
	javac nameserver.java
	javac client.java
	javac -J-Xmx1024M tinyGoogle.java
	javac -J-Xmx1024M worker.java

clean:
	rm *.class
	make