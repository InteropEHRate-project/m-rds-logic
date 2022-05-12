# InteropEHRate Mobile Research Data Sharing (RDS) Protocol's Library

## Installation Guide
The process of integrating the `m-rds-logic` library is quite straightforward, as it is provided as a `jar` file, and is hosted in the project's Nexus repository. 

In case a gradle project is created, the following line needs to be inserted in the dependencies section of the build.gradle file:
```
implementation(group:'eu.interoperhate', name:'rds-simple', version: '0.1.3')
```

If the development team importing the library, is using Maven instead of Gradle, the same dependency must be expressed with the following Maven syntax:
```
<dependency>
	<groupId>eu.interopehrate</groupId>
	<artifactId>rds-simple</artifactId>
	<version>0.1.3</version>
</dependency>
```
