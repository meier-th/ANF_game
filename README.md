This is a back end of ANF game. It's implemented with Spring Boot and runs on embedded Tomcat container.

How to run:
1. Clone this repository to your machine
2. Install a relational database (current version uses PostgreSQL, can be changed)
3. If you use a database other than PostgreSQL - edit Maven's pom.xml file:
  3.1. Remove a dependency for PostgreSQL JDBC driver (org.postgresql.postgresql)
  3.2. Add a dependency for your database JDBC driver
4. Set up database parameters in application.properties file (url, driver class, username, password)
5. (Optional) Set a port for the application to run on (31480 is set now)
6. Build a jar file by running 'mvn package'
7. Execute jar file in target/ directory with command 'java -jar ANF*.*.jar' *.* - version, can be set in pom.xml.
