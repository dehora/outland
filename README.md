
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
  - [Project Status](#project-status)
- [Quickstart](#quickstart)
  - [Server](#server)
    - [Start a Server with Docker](#start-a-server-with-docker)
    - [Create an App and some Features via the API](#create-an-app-and-some-features-via-the-api)
    - [Enable a Feature](#enable-a-feature)
  - [Client](#client)
    - [Add the client library](#add-the-client-library)
    - [Evaluate a Feature](#evaluate-a-feature)
    - [Enable a Feature](#enable-a-feature-1)
- [Feature Flags and Modern Software Development](#feature-flags-and-modern-software-development)
  - [Why a Service?](#why-a-service)
- [Outland Feature Flag Model](#outland-feature-flag-model)
  - [Features](#features)
  - [Feature Flags and Feature Options](#feature-flags-and-feature-options)
  - [Apps](#apps)
  - [App Grants](#app-grants)
- [Installation](#installation)
  - [Server](#server-1)
    - [Docker](#docker)
    - [Creating Tables in DynamoDB](#creating-tables-in-dynamodb)
    - [Creating a sample App and Features](#creating-a-sample-app-and-features)
    - [Configuring the Server](#configuring-the-server)
    - [Server API Authentication](#server-api-authentication)
  - [Client Installation](#client-installation)
    - [Maven](#maven)
    - [Gradle](#gradle)
    - [SBT](#sbt)
- [Build and Development](#build-and-development)
- [Contributing](#contributing)
- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


# Welcome to Outland

Outland is distributed feature flag and event messaging system.

The reason Outland exists is the notion that feature flags are a first class engineering and product development activity that let you work smaller, better, faster, and with less risk. Feature flagging offers significant leverage and shouldn't just be a technology bolt-on or an afterthought, as is often the case today.

Outland consists of an API server and a Java client, with the ambition to support an admin UI, clients in other languages, a decentralised cluster mode, a container sidecar, and more advanced evaluation and tracking options. 

## Project Status

Outland is not production ready.

The client and server are pre 1.0.0, with the aim of getting to a usable state soon (April 2017). 
The admin UI and cluster mode are next in line. See also:

- [Trello Roadmap](http://bit.ly/2nje8ou) is where the project direction is written down.
- [Open Issues](http://bit.ly/2nLZNUT) section has a  list of bugs and things to get done. 
- [Help Wanted](http://bit.ly/2ngXkxP) has a list of things that would be nice to have.


# Quickstart

Clone the project from github:
 
```bash
git clone git@github.com:dehora/outland.git
```

## Server

### Start a Server with Docker

You can use the docker setup in [examples/quickstart](https://github.com/dehora/outland/tree/master/outland-feature-docker/examples/quickstart) 
to get an outland feature server running.

Go to the `examples/quickstart` directory, 

```bash
cd outland/outland-feature-docker/examples/quickstart
```

run the `start_outland` script:

```bash
./start_outland
```

This will:

 - Add the redis and dynamodb-local images and start them.
 - Start an Outland container on port 8180.
 - Create the dynamodb tables used by the server.
 - Seed the server with an app called `testapp`.
 - Add two feature flags to the app, `test-flag-1` and `test-option-1`. 
 
 
You can see the app's list of features via the API:

```bash
curl -v http://localhost:8180/features/testapp -u testconsole/service:letmein
```

As well as the App itself and its grants:

```bash
curl -v http://localhost:8180/namespaces/testapp -u testconsole/service:letmein
```

Dummy credentials are setup as a convenience in the folder's `.env` file, this is how the 
`testconsole` service is given access (all API calls require authentication). 

### Create an App and some Features via the API

An _app_ is used to group features together and store information about which services are 
granted accessed the app's features. Grants can be given to _services_ or _members_ (such as an 
individual or team account). Every app also has one or more _owners_.

The example below creates an app called testapp-1 with two grants and an owner. The grants are 
given to  a service called testservice-1 and one to a user called testuser-1. The owner is 
also testuser-1 - owners are just owners, and are not granted access to an app's features by 
default. 

```bash
curl -v -XPOST http://localhost:8180/namespaces   \
-H "Content-type: application/json" \
-u testconsole/service:letmein  -d'
{
  "key": "testapp-1"
  ,"name": "Test App One"
  ,"owners": {
    "items":[{
      "name": "Test User One"
      ,"username": "testuser-1"
      ,"email": "testuser-1@example.org"
      }
     ]
   }
  ,"granted" : {
    "services":[{
      "key": "testservice-1"
      ,"name": "Test Service One"
    }]
    ,"members":[{
      "username": "testuser-1"
      ,"name": "Test User One"
    }]
  }
}
'
```

Let's add a _feature_ to the app by posting the feature JSON to the server associating it with 
the app's `key`. This one is a simple on/off flag:

```bash
curl -v http://localhost:8180/features \
-H "Content-type: application/json" \
-u testconsole/service:letmein -d'
{
  "key": "testfeature-1"
  ,"description": "A test feature flag"
  ,"appkey": "testapp-1"
  ,"options": {
    "option": "flag"
  },
  "owner": {
    "name": "Test User One"
    ,"username": "testuser-1"
  }
}
'
```

We can also add a feature _option_. These are features that give each possible result a weight, 
allowing you to fire a feature to just a percentage of requests. This is useful for canary deployments. 

This example creates a `bool` feature which has just two options, true and false. 
The options are weighted so that the feature is false 95% of the time.

```bash
curl -v http://localhost:8180/features \
-H "Content-type: application/json" \
-u testconsole/service:letmein -d'
{
  "key": "testfeature-2"
  ,"description": "A test feature flag"
  ,"appkey": "testapp-1"
  ,"options": {
    "option": "bool",
    "items":[
      {
        "name":"false",
        "value":"false",
        "weight": 9500
      },
      {
        "name": "true",
        "value": "true",
        "weight": 500
      }
    ]
  },
  "owner": {
    "name": "Test User One"
    ,"username": "testuser-1"
  }
}
'
```

### Enable a Feature

Features are off by default. Let's enable the first feature `testfeature-1` by setting its state 
to on:

```bash
curl -v -XPOST  http://localhost:8180/features/testapp-1/testfeature-1 \
-H "Content-type: application/json" \
-u testconsole/service:letmein -d'
{
  "key": "testfeature-1"
  ,"appkey": "testapp-1"
  ,"state": "on"
}  
'
```

## Client

### Add the client library

The client is available via JCenter, see the [Client](#client) section for details. 

Once the client is setup up as a dependency you can configure it as follows:

```java
  final String nsKey = "testapp";
  ServerConfiguration conf = new ServerConfiguration()
      .baseURI("http://localhost:8180")
      .nsKey(nsKey);

  FeatureClient client = FeatureClient.newBuilder()
      .serverConfiguration(conf)
      .authorizationProvider(
          (p, s) -> Optional.of(new Authorization(Authorization.REALM_BASIC,
              new String(Base64.getEncoder().encode(("testconsole/service:letmein").getBytes())))))
      .build();

  Runtime.getRuntime().addShutdownHook(new Thread(client::close));      
```

In a real world setting the `authorizationProvider` would not be hardcoded with credentials, but 
this will do to connect to the local server. 

### Evaluate a Feature

Now you can check one the features created by the seed script:

```java
  if (client.enabled("test-flag-1")) {
    System.out.println(featureKey+": enabled");
  } else {
    System.out.println(featureKey+": disabled");
  }
```

### Enable a Feature

The feature will be off by default. You can enable it via the client or the API.

From the client:

```java
  FeatureResource features = client.resources().features();

  Feature feature = Feature.newBuilder()
      .setAppkey("testapp")
      .setKey("test-flag-1")
      .setState(Feature.State.on)
      .build();

  Feature updated = features.update(feature);
  System.out.println("test-flag-1 state: " + updated.getState());
```

From the API:

```sh
curl -i http://localhost:8180/features/testapp/test-flag-1 \
-u testconsole/service:letmein  \
-H "Content-type: application/json" -d '
{
  "key": "test-flag-1",
  "appkey": "testapp",
  "state": "on"
}
'
```

# Feature Flags and Modern Software Development

Traditionally feature flags are used to control code execution - if the flag is on new code is executed, if the flag is off, the code is skipped. Flags allow you to:

- Work safely by running new code in isolation, disabling or enabling as needed. Turning a dynamic flag off in production is faster than a rollback (assuming you [can in fact do a rollback](https://blog.skyliner.io/you-cant-have-a-rollback-button-83e914f420d9)). 

- Ship larger scale functionality faster as a set of small steps. Flags let you iterate and [go round the loop faster](http://www.startuplessonslearned.com/2008/11/principles-of-lean-startups.html). 

- Avoid long lived feature branches with their continuous merge and n-way integration test overhead. 

Flags reinforce the benefits you get from shipping small changes and continuous delivery,  providing a flywheel effect for those practices. They but don't tend to get the attention the others practices do. Tooling and library support for flags isn't 
close to the level of continuous build and delivery systems and there's far more 
online literature around lean engineering practices than flag based development. 


## Why a Service?

We can identify four reasons to make feature flagging a service:

- **Everyone solves this differently**: Flag systems are built for local needs with what's to hand. ZooKeeper, Etcd, Consul, Databases, Redis/Memcached, discovery services, config files, Puppet/Chef, even DNS, all can and probably have at this point been hammered into a flag service. Highly localised approaches don't compose across team/service boundaries resulting in wasted engineering effort as each team puts something together. 

- **Support product development**: As described above, feature flags have a way of becoming instrinsic to product and service development processes over time. Having a first class service makes this evolution easier and simplifies coordination delivery across services and teams. Finally, the path to quantified product impact and measurable online experiments is less likely to get cut off. 

- **Observability**: In a microservices world (and also for monolithic or monocentric systems) there's value in making flag state observable via a service. As a simple example you can correlate a state change with other metrics. Features can be made available to more than code - a service accessible flag can be used control/toggle infrastructure such as load balancers. Service access 
to feature flags allows human operators to more easily intervene and control systems change.

- **Incident management**: A point of flags is to allow features to be turned off quickly, 
faster than any rollback/rollforward mechanism you might have. If the flag system is a 
internal bolt-on to whatever the team happens to run, then  oncall is going to have 
to delve into that to turn it off. A service interface that offers the least power needed to 
change the state is a safer option. 

# Outland Feature Flag Model

In summary:

- A Feature is identified by a key, and can be in an on or off state. Every feature has an owner
and a version. 

- As well as a state, Features may also have a set of options. These are evaluated in the on 
state to return a particular option. Each option has a weight that affects the chance if it 
being returned.

- An App is a collection of features and identified by a key. Every App at least one owner. A 
Feature always belongs to one App.

- Apps also hold a list of Grants to services and team members. A grant allows them to access 
the App's features from the server. 

## Features

Features have a state, which can be `on` or `off` indicating whether the feature is enabled or 
disabled. As well as its state, a feature has a key acting as an identifier that allows it to be 
checked via the client, for example -

```java
String featureKey = "my-new-feature";
if (features.enabled(featureKey)) {
  System.out.println("New feature!");
} else {
  System.out.println("Old feature!");
}
```
 
A feature has also a description, an owner and a version. Features have an owner 
because we've found it helpful to be able to get in touch with someone about the feature, even 
where there is a description for it. Versions are useful for tracking changes and in Outland version identifiers are also orderable.

## Feature Flags and Feature Options

Features have two forms - _flags_ and _options_. Features which are `on` are _evaluated_ and 
features which are `off` are considered disabled and short circuited. What happens during 
evaluation depends on the kind of feature.
  
The `Flag` is the simplest kind of feature and probably the one you're most familiar with. When a 
flag is on it evaluates to true and you're good to go. 
  
An `Option` is more involved and has two stages. First, the `on` or `off` state is checked, and 
if the state is `off` it's skipped just like a Flag. Second, if the state `on`, the feature's 
available options are evaluated and one of them is selected. Each option has a _weight_ and the 
probability of an option being selected is a function of its weight.

Let's take a boolean feature option as an example. A boolean feature will have two options, "true" 
and "false". Now suppose "false" had a weight of 90%, and "true" had a weight of 10%. 
Then the feature is processed roughly like this:

- If the state is `off`, the feature is skipped.
- If the state is `on`, the "true" and "false" options are evaluated.
- 9 times out of 10 the evaluation will return "false".
- 1 time out of 10 the evaluation will return "true".

This makes the boolean option ideal for scenarios like canary rollouts where a controlled 
percentage of traffic is sent to the new code. As we see things going well, we can increase 
the weight of the "true" option allowing more requests to hit it. If it's not working out we 
can increase the "false" weight, biasing traffic away from the new feature. Worst case if it's a 
bust we can back out by setting the feature's state to `off` and disabling the feature altogether.

A Flag can be considered a degenerate form of Option, where the weight of a Flag is wholly allocated 
to its state. But Flags are such a common case we work with them using their state directly 
and use the Option form for more advanced scenarios.   

A boolean feature can only have true and false options, and this is the only option type available
right now, but options are planned to have types other than boolean. For example a "String" feature 
could have 3 options, "red", "green" and "blue", each with a weight, 10%, 20%, and 70%  which 
biases the evaluation. One time in ten the "red" option will be returned, two times out of ten 
it'll be "green", and seven times out of ten it'll be "blue". This gives us a path beyond on/off 
toggles to things like A/B testing and multi-armed bandits.

## Apps

An App is a collection of features and every feature belongs to just one App. An App can be 
anything - it doesn't have to correspond to a construct in your system like a specific service 
or a product. The main goal of an App is to allow features to be observed by multiple systems. 
For example you might use an App to group features together for an epic project such that it allows 
those features to span multiple microservices owned by a few different teams. Or you may simply 
want to make some features available to your backend server and your single page webapp.  

Our experience is that the thing you want to develop often has multiple 
parts and often will span multiple systems. In fact we think it's inevitable some ambitious thing 
you want to build will cut across whatever boundaries you have in place, however well-considered, 
be they social or technical. Apps allow you to express that need and deal with requirements that 
are naturally divergent. 

Every App has one or more owners that can administrate the App and act as point of contact. Every 
feature belonging to an App must have a unique key within the App.
 
Finally, the App construct enables multi-tenancy, allowing multiple teams to share the 
same Outland service. There's nothing to stop you running multiple Outland servers but it 
can be nice to leverage shared infrastructure and reduce heavy lifting.

## App Grants

As well as grouping features, an App can _grant_ access to one or more services 
(typically running systems), or to one or members (typically individual or teams). We'll just 
refer to both kinds as services for now but they are handled separetely. 

Once the service requesting access to a feature or app is authenticated it is checked to see 
if it's in the grant list for the App. If it is it has access to the feature state, otherwise it 
won't be authorised. 

Grants allow the App owner to declare which services can see the App's features. Owners and grants 
are distinct - owners are not automatically given grants and are not looked up during authentication. 


# Installation

## Server

### Docker

The server is available on [docker hub](https://hub.docker.com/r/dehora/outland-feature-server/).

The [examples/all-in-one](https://github.com/dehora/outland/tree/master/outland-feature-docker/examples/all-in-one) 
project has a simple `docker-compose` file you can use to get started, which includes the 
DynamoDB and Redis dependencies along with some dummy credentials (stored in the docker compose `.env` file).

### Creating Tables in DynamoDB

Once the server is up and running, for local development you can create the DynamoDB tables 
used to store feature data via the Dropwizard admin port. 

The `create_tables` script in the [examples/all-in-one](https://github.com/dehora/outland/tree/master/outland-feature-docker/examples/all-in-one) directory will create the DynamoDB tables used by the server (if you run this a second time, the tables will not be recreated and server will return 500 responses).

For online or production use, you can create the tables via the AWS Console and choose to change their names as described [here](https://github.com/dehora/outland/blob/master/outland-feature-docker/README.md).

### Creating a sample App and Features

The `create_seed_app` will create an App called `test-acme-app` with two example features. The script contains plain curl requests which you might find useful for seeing how to call the API. You can see the App's list of features via the API:

```sh
curl -v http://localhost:8180/features/test-acme-app -u testconsole/service:letmein
```

As well as the App itself and its grants:

```sh
curl -v http://localhost:8180/app/test-acme-app -u testconsole/service:letmein
```

### Configuring the Server

The docker image embeds its own Dropwizard configuration file to avoid requiring a mount. The 
settings can be overridden by passing them to the environment. The full list of configuration 
settings is available  [here](https://github.com/dehora/outland/blob/master/outland-feature-docker/README.md) - at minimum you'll want to set up authorization options for real world use as discussed there.

### Server API Authentication

There's two options for authentication: 

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

## Client Installation

### Maven

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

### Gradle

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

### SBT

Add jcenter to `resolvers` in `build.sbt`:

```scala
resolvers += "jcenter" at "http://jcenter.bintray.com"
```

and add the project to `libraryDependencies` in `build.sbt`:

```scala
libraryDependencies += "net.dehora.outland" % "outland-feature-client" % "0.0.4"
```


# Build and Development

The project is built with [Gradle](http://gradle.org/) and uses the 
[Netflix Nebula](https://nebula-plugins.github.io/) plugins. The `./gradlew` 
wrapper script will bootstrap the right Gradle version if it's not already 
installed. 

The client and server jar files are build using the wonderful 
[Shadow](https://github.com/johnrengelman/shadow) plugin.

# Contributing

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

# License

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


