
**Status**

- Build: [![CircleCI](https://circleci.com/gh/dehora/outland.svg?style=svg)](https://circleci.com/gh/dehora/outland)
- Client Download: [ ![Download](https://api.bintray.com/packages/dehora/maven/outland-feature-java/images/download.svg) ](https://bintray.com/dehora/maven/outland-feature-java/_latestVersion)
- Server Download: [ ![Download](https://api.bintray.com/packages/dehora/maven/outland-feature-server/images/download.svg) ](https://bintray.com/dehora/maven/outland-feature-server/_latestVersion)
- Source Release: [0.0.4](https://github.com/dehora/outland/releases/tag/0.0.4)
- Contact: [maintainers](https://github.com/dehora/outland/blob/master/MAINTAINERS)

# Outland

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Welcome to Outland](#welcome-to-outland)
  - [About](#about)
  - [Background](#background)
  - [Status](#status)
- [Understanding Feature Flags and Options](#understanding-feature-flags-and-options)
- [Requirements and Getting Started](#requirements-and-getting-started)
  - [Client Usage](#client-usage)
  - [Server API](#server-api)
- [Installation](#installation)
  - [Server](#server)
    - [Docker](#docker)
    - [Configuring the Server](#configuring-the-server)
    - [Creating Tables in DynamoDB](#creating-tables-in-dynamodb)
  - [Client](#client)
    - [Maven](#maven)
    - [Gradle](#gradle)
    - [SBT](#sbt)
- [Build and Development](#build-and-development)
- [Contributing](#contributing)
- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Welcome to Outland
 

### About

Outland is distributed feature flag and event messaging system.

### Background


### Status

The client and server are pre 1.0.0, with the aim of getting to 1.0.0 soon.

See also:

- [Trello Roadmap](http://bit.ly/2nje8ou) is where the project direction is written down.
- [Open Issues](http://bit.ly/2nLZNUT) section has a  list of bugs and things to get done. 
- [Help Wanted](http://bit.ly/2ngXkxP) has a list of things that would be nice to have.


## Understanding Feature Flags and Options



## Requirements and Getting Started

### Client Usage


### Server API


## Installation

### Server

#### Docker

The server is available on [docker hub](https://hub.docker.com/r/dehora/outland-feature-server/).

The [examples/all-in-one](https://github.com/dehora/outland/tree/master/outland-feature-docker/examples/all-in-one) 
project has a simple `docker-compose` file you can use to get started, which includes the 
DynamoDB and Redis dependencies.

#### Configuring the Server

The docker image embeds its own Dropwizard configuration file to avoid requiring a mount and 
the settings can be overridden by passing them to the environment. The full list of configuration 
settings is available  [here](https://github.com/dehora/outland/blob/master/outland-feature-docker/README.md)

#### Creating Tables in DynamoDB

Once the server is up and running, for local development you can create the DynamoDB tables 
used to store feature data via the Dropwizard admin port. Again, 
the [examples/all-in-one](https://github.com/dehora/outland/tree/master/outland-feature-docker/examples/all-in-one)  
has a script that will create the tables.

For online or production use, you can create the tables via the AWS Console.

### Client

#### Maven

Add jcenter to the repositories element in `pom.xml` or `settings.xml`:

```xml
<repositories>
  <repository>
    <id>jcenter</id>
    <url>http://jcenter.bintray.com</url>
  </repository>
</repositories>
```  

and add the project declaration to `pom.xml`:

```xml
<dependency>
  <groupId>net.dehora.outland</groupId>
  <artifactId>outland-feature-java</artifactId>
  <version>0.0.4</version>
</dependency>
```
#### Gradle

Add jcenter to the `repositories` block:

```groovy
repositories {
 jcenter()
}
```

and add the project to the `dependencies` block in `build.gradle`:

```groovy
dependencies {
  compile 'net.dehora.outland:outland-feature-java:0.0.4'
}  
```

#### SBT

Add jcenter to `resolvers` in `build.sbt`:

```scala
resolvers += "jcenter" at "http://jcenter.bintray.com"
```

and add the project to `libraryDependencies` in `build.sbt`:

```scala
libraryDependencies += "net.dehora.outland" % "outland-feature-client" % "0.0.4"
```


## Build and Development

The project is built with [Gradle](http://gradle.org/) and uses the 
[Netflix Nebula](https://nebula-plugins.github.io/) plugins. The `./gradlew` 
wrapper script will bootstrap the right Gradle version if it's not already 
installed. 

The client and server jar files are build using the wonderful 
[Shadow](https://github.com/johnrengelman/shadow) plugin.

## Contributing

Please see the [issue tracker](http://bit.ly/2nLZNUT) 
for things to work on. The [help-wanted](http://bit.ly/2ngXkxP) label  has a list of things 
that would be nice to have.

Before making a contribution, please let us know by posting a comment to the 
relevant issue. If you would like to propose a new feature, create a new issue 
first explaining the feature you’d like to contribute or bug you want to fix. Significant 
features will end up being added to the [Trello Roadmap](http://bit.ly/2nje8ou). 

The codebase follows [Square's code style](https://github.com/square/java-code-styles) 
for Java and Android projects.

----

## License

Apache License Version 2.0

Copyright 2017 Bill de hÓra

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


