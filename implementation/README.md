# MapRepair


### Prerequisites
  
 * Java 1.8 or higher
 * Maven 2 or higher (if needed, dependencies can also be build manually)

### Installation

The following command need to be runned under the top directory :
```
mvn install
```

Tests can be skipped during installation with :
```
mvn install -DskipTests
```

The JAR will be built and placed in "safe-rewriting/target/" directory.
The JAR "safe-rewriting-1.0.0-Beta-jar-with-dependencies.jar" is packaged with all the needed dependencies, thus will suit most of the users needs.


### Usage example

The system can be runned with the following command line :
```
java -jar safe-rewriting-1.0.0-Beta-jar-with-dependencies.jar -v <policy views file> -m <mapping to rewrite file> -o <output file> -n <value N in srepair algorithm> -p <chosen preference function Max, Avg or Learn (cf. paper)> -t <(if learning is used) training set file>
```

For example, using files in "example/" directory, the command line to rewrite 'PolicyViews.xml' using the Max preference function will be :
```
java -jar safe-rewriting-1.0.0-Beta-jar-with-dependencies.jar -v example/PolicyViews.xml -m example/MappingToRewrite.xml -o test.xml -n 10 -p Max
```

Analogously, the command line using the learning of preference function will be the following :
```
java -jar safe-rewriting-1.0.0-Beta-jar-with-dependencies.jar -v example/PolicyViews.xml -m example/MappingToRewrite.xml -o test.xml -n 10 -p Learn -t example/trainingData.txt
``` 
