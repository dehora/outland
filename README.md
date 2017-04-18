
**Status**

- Build: [![CircleCI](https://circleci.com/gh/dehora/outland.svg?style=svg)](https://circleci.com/gh/dehora/outland)
- Client Download: [ ![Download](https://api.bintray.com/packages/dehora/maven/outland-feature-java/images/download.svg) ](https://bintray.com/dehora/maven/outland-feature-java/_latestVersion)
- Server Download: [ ![Download](https://api.bintray.com/packages/dehora/maven/outland-feature-server/images/download.svg) ](https://bintray.com/dehora/maven/outland-feature-server/_latestVersion)
- Source Release: [0.0.10](https://github.com/dehora/outland/releases/tag/0.0.10)
- Contact: [maintainers](https://github.com/dehora/outland/blob/master/MAINTAINERS)

# Outland

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Welcome to Outland](#welcome-to-outland)
  - [Why Feature Flags?](#why-feature-flags)
  - [Why a Service?](#why-a-service)
  - [Project Status](#project-status)
- [Quickstart](#quickstart)
  - [Server](#server)
    - [Start a Server with Docker](#start-a-server-with-docker)
    - [Create a Group and some Features via the API](#create-a-group-and-some-features-via-the-api)
    - [Enable a Feature](#enable-a-feature)
    - [Add a Feature Namespace](#add-a-feature-namespace)
  - [Client](#client)
    - [Add the client library](#add-the-client-library)
    - [Evaluate a Feature](#evaluate-a-feature)
    - [Client Feature API](#client-feature-api)
- [Outland Feature Flag Model](#outland-feature-flag-model)
  - [Summary: Features, Groups and Namespaces](#summary-features-groups-and-namespaces)
  - [Features](#features)
  - [Feature Flags and Feature Options](#feature-flags-and-feature-options)
    - [Option Selection](#option-selection)
    - [Boolean Options](#boolean-options)
    - [String Options](#string-options)
    - [Option Controls](#option-controls)
  - [Groups](#groups)
    - [Group Access Control](#group-access-control)
  - [Namespaces](#namespaces)
    - [Pattern: using namespaces for environments](#pattern-using-namespaces-for-environments)
- [Installation](#installation)
  - [Server](#server-1)
    - [Docker](#docker)
    - [Creating Tables in DynamoDB](#creating-tables-in-dynamodb)
    - [Creating a sample Group and Features](#creating-a-sample-group-and-features)
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

The reason Outland exists is the notion that feature flags are a first class engineering and 
product development activity that let you work smaller, better, faster, and with less risk. 

Outland consists of an API server and a Java client, with the ambition to support an admin UI, 
clients in other languages, a decentralised cluster mode, a container sidecar, and more advanced 
evaluation and tracking options. 

## Why Feature Flags?

Feature flagging offers significant leverage and shouldn't just be a technology bolt-on or an 
afterthought, as is often the case today.

Feature flags allow you to:

- Work safely. Turning a dynamic flag off in production is faster than a rollback. 

- Ship larger scale functionality faster as a set of small steps.  

- Avoid long lived feature branches with their merge and integration test overhead. 

Flags reinforce the benefits you get from shipping small changes and continuous delivery,  
providing a flywheel effect for those practices.

You can read more about the concepts in the post ["Feature Flags: Smaller, Better, Faster Software Development"](https://medium.com/@dehora/feature-flags-smaller-better-faster-software-development-f2eab58df0f9)

## Why a Service?

We can identify four reasons to make feature flagging a service:

- **Everyone solves this differently**: Flag systems are typically built with whatever's to hand 
instead of a system that designed from the ground up to support flag based engineering and development.  

- **Support product development**: Feature flags have a way of becoming important to product and 
service development processes over time. Having a first class service makes this easier 
and simplifies coordination delivery across services and teams. 

- **Observability**: In a microservices world (and also for monolithic or monocentric systems) 
there's value in making flag state observable via a service. Service access 
to feature flags allows human operators to more easily intervene and control systems change.

- **Incident management**: A point of flags is to allow features to be turned off quickly. If the 
flag system is a internal bolt-on to whatever the team happens to run is far more risky than a 
service interface that offers the least power needed to change the state. 

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
 - Seed the server with a group called `testgroup`.
 - Add three feature flags to the group, `test-flag-1`, `test-option-1` and `test-flag-namespace-1`. 
 
 
You can see the group's list of features via the API:

```bash
curl -v http://localhost:8180/features/testgroup -u testconsole/service:letmein
```

As well as the Group itself and its grants:

```bash
curl -v http://localhost:8180/groups/testgroup -u testconsole/service:letmein
```

Dummy credentials are setup as a convenience in the folder's `.env` file, this is how the 
`testconsole` service is given access (all API calls require authentication). 

### Create a Group and some Features via the API

A _group_ contains one or more features and describe which services or teams are 
granted access to those features. Every group also has one or more _owners_.

The example below creates a group called testgroup-1 that can be accessed by a service called 
`testservice-1` and a member called `testuser-1` and whose owner is 
also `testuser-1`:
```bash
curl -v -XPOST http://localhost:8180/groups   \
-H "Content-type: application/json" \
-u testconsole/service:letmein  -d'
{
  "key": "testgroup-1"
  ,"name": "Test Group One"
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

Owners have permissions to edit the group, but owners are not granted access to the group's 
features by default - only services and members are allowed to access features. 


Let's add a _feature_ to the group by posting the feature JSON to the server associating it with 
the group's `key`. This one is a simple on/off flag:

```bash
curl -v http://localhost:8180/features \
-H "Content-type: application/json" \
-u testconsole/service:letmein -d'
{
  "key": "testfeature-1"
  ,"description": "A test feature flag"
  ,"group": "testgroup-1"
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
  ,"group": "testgroup-1"
  ,"options": {
    "option": "bool",
    "items":[
      {
        "key":"false",
        "value":"false",
        "weight": 9500
      },
      {
        "key": "true",
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
curl -v -XPOST  http://localhost:8180/features/testgroup-1/testfeature-1 \
-H "Content-type: application/json" \
-u testconsole/service:letmein -d'
{
  "key": "testfeature-1"
  ,"group": "testgroup-1"
  ,"state": "on"
}  
'
```

### Add a Feature Namespace

Features can have multiple namespaces allowing you define a variation of the feature for a 
particular context, such as an environment. Let's add a "production" namespace to the first 
feature `testfeature-1`: 

```bash
curl -v -XPOST  http://localhost:8180/features/testgroup-1/testfeature-1/namespaces \
-H "Content-type: application/json" \
-u testconsole/service:letmein -d'
{
  "namespace": "production",
  "feature": {
    "key": "testfeature-1"
  }
}
'
```

## Client

### Add the client library

The client is available via JCenter, see the [Client](#client) section for details. 

Once the client is setup up as a dependency you can configure it as follows:

```java
  ServerConfiguration conf = new ServerConfiguration()
      .baseURI("http://localhost:8180")
      .defaultGroup("testgroup-1");

  FeatureClient client = FeatureClient.newBuilder()
      .serverConfiguration(conf)
      .authorizationProvider(
          (group, scope) -> Optional.of(new Authorization(Authorization.REALM_BASIC,
              new String(Base64.getEncoder().encode(("testconsole/service:letmein").getBytes())))))
      .build();   
```

In a real world setting the `authorizationProvider` would not be hardcoded with credentials, but 
this will do to connect to the local server. 

### Evaluate a Feature

Now you can check one the features created by the seed script:

```java
  if (client.enabled("testfeature-1")) {
    System.out.println(featureKey+": enabled");
  } else {
    System.out.println(featureKey+": disabled");
  }
```

To access a feature in a particular group you can use the extended form of `enabled`:

```java
  if (client.enabled("testgroup-1", "testfeature-1")) {
    System.out.println(featureKey+": enabled");
  } else {
    System.out.println(featureKey+": disabled");
  }
```

It's common to work with just one group so the short form exists as a handy convenience. The 
 short form takes its group from the `defaultGroup` set via `ServerConfiguration`.


### Client Feature API

You can manage features via the client via `FeatureResource`:

```java
  FeatureResource features = client.resources().features();

  Feature feature = Feature.newBuilder()
      .setGroup("testgroup-1")
      .setKey("testfeature-1")
      .setState(Feature.State.on)
      .build();

  Feature updated = features.update(feature);
  System.out.println("test-flag-1 state: " + updated.getState());
```


# Outland Feature Flag Model

## Summary: Features, Groups and Namespaces

A _Feature_ is identified by a _key_, and can be in an _on_ or _off_ state. Every feature has an owner
and a version.  As well as a state, Features may also have a set of _options_. These are evaluated 
in the on state and one option is returned. Each option can have a weight that affects the chance 
if it being returned.

A _Group_ is a collection of features and also identified by a key. Every Group at 
least one owner. A Feature always belongs to one Group. Groups also hold a list of services 
and team members that are allowed access its features. 

A feature can have optionally have one or more _Namespaces_ that carry a custom variation of
the feature's state. For example a feature can be disabled by default but enabled for a 
namespace called `staging`.

## Features

Features have a state, which can be `on` or `off` indicating whether the feature is enabled or 
disabled. As well as its state, a feature has a _key_ acting as an identifier that allows it to be 
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
where there is a description for it. Versions are useful for tracking changes and in Outland 
version identifiers are also orderable.

## Feature Flags and Feature Options

Features have two forms - _flags_ and _options_. Features which are `on` are _evaluated_ and 
features which are `off` are considered disabled and short circuited. What happens during 
evaluation depends on the kind of feature.
  
The `Flag` is the simplest kind of feature and probably the one you're most familiar with. When a 
flag is on it evaluates to true and you're good to go. 
  
An `Option` is more involved and has two stages:

- First, the `on` or `off` state is checked to see if it's enabled, and if the state is `off` it's skipped just like a Flag. 

- Second, if the state `on`, the feature's available options are evaluated and one of them is selected. 

A Flag can be considered a reductive form of Option, where the weight of a Flag is wholly allocated 
to its state. But Flags are such a common case we work with them using their state directly 
and use the Option form for more advanced scenarios.   

### Option Selection

Each option has a _weight_ and the probability of an option being selected is proportional to its weight. 

Let's take a boolean feature option as an example. A boolean feature will have two options, "true" 
and "false". Now suppose "false" had a weight of 90%, and "true" had a weight of 10%. 
Then the feature is processed roughly like this:

- If the state is `off`, the selection is skipped and the control value is returned.
- If the state is `on`, the "true" and "false" options are evaluated.
- 9 times out of 10 the evaluation will select "false".
- 1 time out of 10 the evaluation will select "true".

This is sometimes called ["roulette wheel selection"](https://en.wikipedia.org/wiki/Fitness_proportionate_selection) and is how the client decides which option to return.

The process of returning an option is called "selection". Note that this is different to 
determining if a feature is enabled. In the client the distinction is made by using 
`enabled` calls to check feature state and `select` calls to return an option value:

```java
// select returns one of the option values
String selection = client.select("colors");

// but enabled just checks to see if the feature is on:
boolean on = client.enabled("colors");
```

### Boolean Options

A _boolean_ feature  option can only have true and false options, each of which can be given a weight. This makes the boolean option ideal for scenarios like canary rollouts where a 
controlled percentage of traffic is sent to the new code. As we see things going well, we can increase the weight of the "true" option allowing more requests to hit it. If it's not working 
out we can increase the "false" weight, biasing traffic away from the new feature. Worst case 
if it's a bust we can back out by setting the feature's state to `off` and disabling the 
feature altogether. 

Weights can be entirely allocated to one of the boolean options. For example you can give 
the "true" option all the weight by setting the "false" option to 0, as shown in this 
JSON fragment:

```json
{
  "key": "bool-weighted-all-true",
  "group": "group1",
  "state": "on",
  "description": "A test feature option",
  "options": {
    "option": "bool",
    "maxweight": 10000,
    "items": [
      {
        "key": "true",
        "value": "true",
        "weight": 10000
      },
      {
        "key": "false",
        "value": "false",
        "weight": 0
      }
    ],
    "control": "false"
  }
}
```

When these weights are evaluated the true option will always be returned. 

### String Options

A string feature can have multiple options, again each of which can be given a weight. For 
example a "color" feature could have 3 options, "red", "green" and "blue", each with a weight, biasing their selection to 10%, 20%, and 70% such that one time in ten the "red" option will be returned, two times out of ten it'll be "green", and seven times out of ten it'll be "blue".  
This JSON fragment shows what that would look like:

```json
{
  "key": "colors",
  "group": "group1",
  "state": "on",
  "description": "A test feature string",
  "options": {
    "option": "string",
    "maxweight": 10000,
    "items": [
      {
        "key": "option-blue",
        "value": "blue",
        "weight": 7000
      },
      {
        "key": "option-green",
        "value": "green",
        "weight": 2000
      },
      {
        "key": "option-red",
        "value": "red",
        "weight": 1000
      }
    ],
    "control": "option-green"
  }
}
```

This gives us a path beyond on/off toggles to things like A/B testing and multi-armed bandits.

Like boolean options string features can be given weights and one string can be allocated all 
the weight. Again as with booleans a string feature can have a `"control"` field indicating 
which option should be selected if the feature is off. In fact, the boolean option can 
be considered a special case of a string option that is understood by the server and client.

### Option Controls

Note the last example has a field called `"control"` that is set to the `"option-green"`.  A  control defines which option should be returned if the feature is set to `off` - when the 
feature is off the selection algorithm will return the option named by the control. If 
the control is not declared the return value will default to an empty string in the 
client. 

Controls are useful as fallbacks and for test experiment scenarios such as A/B tests 
can act as the value shown to the group outside the experiment.

Sometimes you want the control option to be different from the selection candidates. The 
easiest way to do that is to define an option whose weight is 0. This means it will never 
be selected when the feature is enabled, but is available for use as the control option when 
the feature is disabled.

## Groups

A Group is a collection of features and every feature belongs to just one Group. Every 
feature in a Group must have a unique key within the Group. 

The main goal of a Group is to allow features to be observed by multiple systems. For example 
you might use a Group to group features together for an epic project such that it allows 
those features to span multiple microservices owned by a few different teams. Or you may simply 
want to make some features available to your backend server and your single page webapp.  

Our experience is that the thing you want to develop often has multiple parts and often will 
span multiple systems. It's inevitable some thing you want to build will cut across whatever 
service boundaries you have in place, however well-considered they are. Groups allow you to 
handle that and deal with requirements that are naturally divergent. 

Every Group has one or more owners that can administrate the Group and act as point of 
contact. 
 
Finally, the Group construct enables multi-tenancy, allowing multiple teams to share the 
same Outland service. There's nothing to stop you running multiple Outland servers but it 
can be nice to leverage shared infrastructure and reduce heavy lifting.

### Group Access Control

As well as grouping features, an Group can _grant_ access to one or more services 
(typically running systems), or to one or members (typically individual or teams). Grants allow 
the Group owner to declare which services can see the Group's features.  We'll just refer 
to both kinds as services for now but they are handled separately. 

Once the service requesting access to a feature or group is authenticated it is checked to see 
if it's in the grant list for the Group. If it is it has access to the feature state, 
otherwise it won't be authorised. 

Owners and grants are distinct - owners are not automatically given grants and are not looked 
up during authentication. 

## Namespaces

Each feature can have one or more namespaces that contain variations of the feature state. The 
API returns the namespace variations as part of the feature's response data.

The client can be configured to use a particular namespace via its `ServerConfiguration`, 
for example: 

```java
  ServerConfiguration conf = new ServerConfiguration()
      .baseURI("http://localhost:8180")
      .namespace("staging");

  FeatureClient client = ...;
```

If a feature it's evaluating doesn't have that namespace the client will fall back to using 
the feature's default state.

You can define feature namespaces when creating a feature, but they are easy to add to a feature after its been created by sending one to the features `namespaces` resource. Here's an example:

```bash
curl -v http://localhost:8180/features/testgroup-1/testfeature-2/namespaces \
-H "Content-type: application/json" \
-u testconsole/service:letmein -d'
{
  "namespace": "staging",
  "feature": {
    "key": "testfeature-2",
    "state": "off",
    "options": {
      "option": "bool",
      "items": [
        {
          "key": "false",
          "value": "false",
          "weight": 9900
        },
        {
          "key": "true",
          "value": "true",
          "weight": 100
        }
      ]
    }
  }
}
'
```

### Pattern: using namespaces for environments

A common use of namespaces is to define per-environment settings. For example a "production" 
namespace can have a bool option with a 99% false weight and a 1% true weight, whereas the 
default could be 90% false and 10% true. Using namespaces like this means you don't have to 
define a Group per environment you're working with.


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

### Creating a sample Group and Features

The `create_seed_group` will create a Group called `test-acme-group` with two example features. The script contains plain curl requests which you might find useful for seeing how to call the API. You can see the Group's list of features via the API:

```sh
curl -v http://localhost:8180/features/test-acme-group -u testconsole/service:letmein
```

As well as the Group itself and its grants:

```sh
curl -v http://localhost:8180/groups/test-acme-group -u testconsole/service:letmein
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
  <version>0.0.10</version>
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
  compile 'net.dehora.outland:outland-feature-java:0.0.10'
}  
```

### SBT

Add jcenter to `resolvers` in `build.sbt`:

```scala
resolvers += "jcenter" at "http://jcenter.bintray.com"
```

and add the project to `libraryDependencies` in `build.sbt`:

```scala
libraryDependencies += "net.dehora.outland" % "outland-feature-client" % "0.0.10"
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


