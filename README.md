# Repose Hello World Custom Filter Bundle

This project is intended to assist a developer in learning what is needed to create a custom filter for Repose. 

## Clone this project
`git clone git@github.com:rackerlabs/repose-hello-world.git`

## Build the cloned project
 - `cd repose-hello-world`
 - `mvn clean install`

## Copy the artifacts to a Repose node
 - `scp ./custom-bundle/target/custom-bundle-1.0-SNAPSHOT.ear                                       root@<SERVER_HOSTING_REPOSE>:/usr/share/repose/filters/`
 - `scp ./hello-world-groovy/src/main/resources/META-INF/schema/examples/hello-world-groovy.cfg.xml root@<SERVER_HOSTING_REPOSE>:/etc/repose/`
 - `scp ./hello-world-java/src/main/resources/META-INF/schema/examples/hello-world-java.cfg.xml     root@<SERVER_HOSTING_REPOSE>:/etc/repose/`
 - `scp ./hello-world-scala/src/main/resources/META-INF/schema/examples/hello-world-scala.cfg.xml   root@<SERVER_HOSTING_REPOSE>:/etc/repose/`

## Add one or more of the hello-world filters to the system-model.cfg.xml
 - `ssh root@<SERVER_HOSTING_REPOSE>`
 - `vi /etc/repose/system-model.cfg.xml`

# Creating your own custom filter bundle

## Create a directories to hold the new filter
 - `mkdir -p ./hello-world-new/src/main/new/org/openrepose/filters/custom/helloworldnew/`
 - `mkdir -p ./hello-world-new/src/test/new/org/openrepose/filters/custom/helloworldnew/`

# Create the files for the new filter.
 - `touch ./hello-world-new/src/main/new/org/openrepose/filters/custom/helloworldnew/HelloWorldNewFilter.new`
 - `touch ./hello-world-new/src/test/new/org/openrepose/filters/custom/helloworldnew/HelloWorldNewFilterTest.new`

# Add the new module to the top level POM.
 - `vi ./pom.xml`

```
    ...
    <modules>
        <module>custom-bundle</module>
        <module>hello-world-java</module>
        <module>hello-world-scala</module>
        <module>hello-world-groovy</module>
        <module>hello-world-new</module>
    </modules>
    ...
```
 
# Add the new module to the custom bundle POM dependency.
 - `vi ./custom-bundle/pom.xml`

```
    ...
    <dependencies>
    ...
        <dependency>
            <groupId>org.openrepose.filters.custom</groupId>
            <artifactId>hello-world-new</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
    ...
```

# Add the new filter info to the bundle.
 - `vi ./custom-bundle/src/main/application/WEB-INF/web-fragment.xml`

```
    ...
    <filter>
        <filter-name>hello-world-new</filter-name>
        <filter-class>org.openrepose.filters.custom.helloworldnew.HelloWorldNewFilter</filter-class>
    </filter>
    ...
```