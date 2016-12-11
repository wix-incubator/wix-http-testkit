[![Build Status](https://travis-ci.org/wix/wix-http-testkit.svg?branch=master)](https://travis-ci.org/wix/wix-http-testkit)

# HTTP Testkit

Overview
========

Wix Http Testkit is a library that will address many of the End-to-end testing concerns you might encounter.

Wix HTTP Testkit aims to be:
* __Simple__ Testing REST services or starting mock/stub servers is very simple and requires very few lines of code.
* __Fast__ Leveraging [Akka-Http](https://github.com/akka/akka-http) infrastructure, starting servers takes milliseconds.
* __Integrated__: Other than providing a set of DSLs to support composing and executing REST HTTP calls and creating and configuring web servers, it also contains out of the box matcher libraries for [Specs<sup>2</sup>](http://wix.github.io/accord/specs2.html) to easily validate each aspect of the tested flow.


Getting Started
===============

HTTP-testkit version 0.1 is available on Maven Central Repository. Scala versions 2.11.1+ and 2.12.0+ are supported. The next milestone is 0.2-SNAPSHOT and is available from the Sonatype snapshots repository.

###SBT
Simply add the *wix-http-testkit* module to your build settings:

```sbt
libraryDependencies += "com.wix" %% "http-testkit" % "0.1"
```
###Maven

```xml
<dependencies>
  <dependency>
    <groupId>com.wix</groupId>
    <artifactId>http-testkit_${scala.tools.version}</artifactId>
    <version>0.1</version>
  </dependency>
</dependencies>

```

# Documentation 

* __Rest Client__: a declarative REST client [Documentation](./HTTP_CLIENT.md).  
* __Simplicator Web Servers__: Easily configurable web servers [Documentation](./WEBSERVER.md).
* __Specs<sup>2</sup> Matchers Suite__: Comprahensive matcher suites [Response Matchers](./HTTP_CLIENT_MATCHERS.md) and [Request Matchers](./WEBSERVER_MATCHERS.md).    

# Contribute

Ideas and feature requests welcome! Report an [issue](https://github.com/wix/wix-http-testkit/issues/) or contact the [maintainer](https://github.com/noam-almog) directly. 
