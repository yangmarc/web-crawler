# web-crawler
Web-crawler

Execute the following command 

Clean and Build 
```
mvn clean && mvn package
```

Run the program 
```
java -cp target/webcrawler-1.0-SNAPSHOT.jar com.webcrawler.app.App [ domain ] [ start Url ] [ maxThreads ] [ maxDepth ]
```
Example
```$xslt
java -cp target/webcrawler-1.0-SNAPSHOT.jar com.webcrawler.app.App https://www.example.com https://www.example.com/ 5 2
```

Default: 
```
maxThreads = 5
maxDepth = 2
```