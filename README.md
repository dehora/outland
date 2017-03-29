
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
  - [Server API Authentication](#server-api-authentication)
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


## Understanding Feature Flags

### Features

As well as its state, a feature has a key, a description and an owner. Keys are how clients call 
to see if a feature is enabled. Features have an owner because we've found it helpful to be 
able to get in touch with someone about the feature, even where there is a description for it. All
features have a state, which can be `on` or `off` indicating whether the feature is enabled or 
disabled.

Features have two forms - _flags_ and _options_. Features which are `on` are _evaluated_ and 
features which are `off` are considered disabled and short circuited. What happens during 
evaluation depends on the kind of feature.
  
The `Flag` is the simplest kind of feature and probably the one you're most familiar with. When a 
flag is on it evaluates to true and you're good to go. 
  
An `Option` has two stages. First it's `on` or `off` state is checked. If the state is `off` it's 
skipped just like a Flag. If the state `on`, the feature's available options are evaluated and one 
of them is selected. Each option has a _weight_ and the probability of an option being 
 selected is a function of its weight.
 
A Flag can be considered a base form of Option, where the weight of a flag is 100% set to its 
state. But flags are such a common case we just deal with them using their state directly 
and reserve the option form for more advanced scenarios. 

Let's take a boolean feature option as an example. A boolean feature will have two options, "true" 
and "false" - let's say  "false" had a weight of 90%, and "true" option had a weight of 10%. 
Then:

- If the state is `off`, it's skipped.
- If the state is `on`, the "true" and "false" options are evaluated
- 9 times out of 10 the evaluation will return "false"
- 1 time out of 10 the evaluation will return "true"

This makes the boolean option ideal for scenarios like canary rollouts where a controlled 
percentage of traffic is sent to the new code. As we see things going well we can increase 
the weight of the "true" option allowing more requests to hit it. If it's not working out we 
can increase the "false" weight. Worst case if it's a bust we can back out completely by setting 
the feature's state to `off` and disabling it altogether.
  
A boolean feature can only have true and false options, and this is the only option type available
right now, but options are planned to have types other than boolean. For example a string feature 
could have 3 options, "red", "green" and "blue", each with a weight, 10%, 20%, and 70%  which 
biases the evaluation. One time in ten the "red" option will be returned, two times out of ten 
it'll be "green", and seven times out of ten it'll be "blue". This gives us a path beyond on/off 
toggles to for things like A/B testing and multi-armed bandit evaluation.

### Apps

An App is a collection of features. An App can be anything - it doesn't have to correspond to a 
construct in your system like a specific service or a product. The main goal of an App is to allow 
features to be observed by multiple systems. For example you might use an App to group features 
together for an epic project such that it allows those features to span multiple microservices 
owned by a few different teams. Or you may simply want to make some features available to your 
backend servers and your single page webapp.  

Our experience is that the thing you want to develop often has multiple parts and often will span 
multiple systems. In fact we think it's inevitable some ambitious thing you want to build will cut 
across whatever well-considered boundaries you have in place, be they social or technical. Apps 
allow you to express that need. 

Every App has one or more owners that can administrate the App and act as point of contact. Every 
feature belonging to an App must have a unique key.
 
Finally, the App construct enables multi-tenancy, allowing multiple teams to share the 
same Outland service. There's nothing to stop you running multiple Outland servers but it 
can be nice to leverage shared infrastructure and reduce heavy lifting.

### Grants

As well as grouping features, an App can _grant_ access to one or more services 
(typically running systems), or to one or members (typically individual or teams). We'll just 
refer to both kinds as services for now but they are handled separetely. 

Once the service requesting access to a feature or app is authenticated it is checked to see 
if it's in the grant list for the App. If it is it has access to the feature state, otherwise it 
won't be authorised. 

Grants allow the App owner to declare which services can see the App's features. Owners and grants 
are distinct - owners are not automatically given grants and are not looked up during authentication. 


  




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

The docker image embeds its own Dropwizard configuration file to avoid requiring a mount. The 
settings can be overridden by passing them to the environment. The full list of configuration 
settings is available  [here](https://github.com/dehora/outland/blob/master/outland-feature-docker/README.md) 
at minimum you'll want to set up authorization options.

#### Creating Tables in DynamoDB

Once the server is up and running, for local development you can create the DynamoDB tables 
used to store feature data via the Dropwizard admin port. Again, 
the [examples/all-in-one](https://github.com/dehora/outland/tree/master/outland-feature-docker/examples/all-in-one)  
has a script that will create the tables.

For online or production use, you can create the tables via the AWS Console.

### Server API Authentication

This is somewhere between not-great and terrible right now. There's two options for authentication: 

- Basic Authentication: An "API Key" style model where callers have well known credentials sent 
in the password part along with their identity. This is enabled by default but no keys are configured.

- OAuth Bearer Authentication: Callers submit bearer tokens and those tokens are verified by a 
remote OAuth server and exchanged for an OAuth token object that contains principal identity 
and scopes. This is disabled by default.

The options aren't mutually exclusive, you can enable both. 

There's no way to turn authentication off, which is deliberate. Also, an unconfigured 
Outland server will fail to authenticate requests, ie, it won't work out of the box unless you 
either supply API Keys for the Basic option or enable access to an OAuth server to verify Bearer 
tokens.

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


