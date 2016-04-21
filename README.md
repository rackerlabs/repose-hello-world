# Repose Hello World Custom Filter Bundle

This project is intended to assist a developer in learning what is needed to create a custom filter for Repose.
This project contains four custom filters (in various languages) and a module to package up the filters into an
artifact Repose can use (custom-bundle).

For more information on working with Repose, check out [Getting Started with Repose](https://repose.atlassian.net/wiki/display/REPOSE/Getting+Started+with+Repose).

# Deploy example custom filters

## Clone this project
```
git clone git@github.com:rackerlabs/repose-hello-world.git
```

## Build the cloned project
```
cd repose-hello-world
gradle clean
gradle build
```

## Copy the artifacts to a Repose node
```
scp ./custom-bundle/build/libs/custom-bundle-1.0-SNAPSHOT.ear                                    root@<SERVER_HOSTING_REPOSE>:/usr/share/repose/filters/
scp ./hello-world-groovy/src/main/resources/META-INF/schema/examples/hello-world-groovy.cfg.xml  root@<SERVER_HOSTING_REPOSE>:/etc/repose/
scp ./hello-world-java/src/main/resources/META-INF/schema/examples/hello-world-java.cfg.xml      root@<SERVER_HOSTING_REPOSE>:/etc/repose/
scp ./hello-world-scala/src/main/resources/META-INF/schema/examples/hello-world-scala.cfg.xml    root@<SERVER_HOSTING_REPOSE>:/etc/repose/
scp ./hello-world-kotlin/src/main/resources/META-INF/schema/examples/hello-world-kotlin.cfg.xml  root@<SERVER_HOSTING_REPOSE>:/etc/repose/
```

## Add one or more of the hello-world filters to the system-model.cfg.xml
```
ssh root@<SERVER_HOSTING_REPOSE>
vi /etc/repose/system-model.cfg.xml
```

Partial file contents:
```
...
<filters>
    <filter name="hello-world-java"/>
</filters>
...
```

# Creating your own custom filter bundle

## Create the new source code directories
```
mkdir -p ./hello-world-new/src/main/new/org/openrepose/filters/custom/helloworldnew/
mkdir -p ./hello-world-new/src/test/new/org/openrepose/filters/custom/helloworldnew/
```

## Create the files for the new filter
```
touch ./hello-world-new/build.gradle   # only if you need additional dependencies
touch ./hello-world-new/src/main/new/org/openrepose/filters/custom/helloworldnew/HelloWorldNewFilter.java
touch ./hello-world-new/src/test/new/org/openrepose/filters/custom/helloworldnew/HelloWorldNewFilterTest.java
```

## Add the new module to the top level build
```
vi ./settings.gradle
```

File contents:
```
include 'custom-bundle',
        'hello-world-groovy',
        'hello-world-java',
        'hello-world-kotlin',
        'hello-world-scala',
        'hello-world-new'
```
 
## Add the new module to the custom bundle build
```
vi ./custom-bundle/build.gradle
```

File contents:
```
apply plugin: 'ear'

dependencies {
    earlib project(":hello-world-groovy")
    earlib project(":hello-world-java")
    earlib project(":hello-world-kotlin")
    earlib project(":hello-world-scala")
    earlib project(":hello-world-new")
}
```

## Add the new filter info to the bundle
```
vi ./custom-bundle/src/main/application/WEB-INF/web-fragment.xml
```

Partial file contents:
```
    ...
    <filter>
        <filter-name>hello-world-new</filter-name>
        <filter-class>org.openrepose.filters.custom.helloworldnew.HelloWorldNewFilter</filter-class>
    </filter>
    ...
```
