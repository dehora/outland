### Changes

### 0.0.12

- Upgrades gradle and nebula to 4.0.1.
- Upgrades build to circleci 2.

#### Server

- Removes type fields from formats.
- Cleans up protocol buffers and lib versions.
- Maps malformed json to 422 responses.

#### Client 

- Removes outland-cluster-java. New home: [outland-message](https://github.com/dehora/outland-message)
- Changes the order of arguments for client enabled and select calls.
- Tidies client configuration.


### 0.0.11

#### Server

- Adds support for string based options (OptionType "string").
- Allows an option's collection to have a default/fallback ("control").
- Supports updating namespaces via API.
- Caches group data on feature server.

#### Client

- Introduces client signatures for selecting options based on weights/control values.
- Supports selecting string/bool options using a roulette wheel selection
- Adds a FeatureRecord wrapper for features to memoize some data/calls (eg weights).
- Moves enabled and selection checks into FeatureRecord.
- Adds default group method signatures to FeatureClient.
- Fixes group guard check on ResourceOptions.
- Increases test coverage.
- Moves client generated proto files to dedicated src folder.

### 0.0.10

#### Server

- Allows a feature to have one or more namespaced variants.
- Fixes location headers for groups and features.
- Adds properties to groups.
- Removes ids from version structures.
- Tidies up proto files, types and identifiers.
- Renames namespace to group.
- Returns 422 if the feature/group request content is invalid.

#### Clients

- Allows a client to be configured to use a namespace.
- Evaluates feature namespaces falling back to default values.


### 0.0.9

#### Client

- Replaces "app" concept with namespace in client
- Clients can work with multiple namespaces without declaring a configuration.
- Adds an automatic client shutdown hook.
- Adds JavaDoc for client.


### 0.0.8

#### Server

- Renames App to Namespace.
- Renames grants to access.
- Moves app owners into a collection.
- Fixes updates of features.
- Rejects re-creation of Features with a 422.
- Rejects re-creation of Apps with a 422.
- Extends the readme quickstart with HTTP API examples


### 0.0.7

#### Server

 - Fixes updates sent with no option changes.


### 0.0.6

#### Server

- Wraps feature options in a collection object.
- Validates option ids match on update


### 0.0.5

#### Server

- Defines an initial Open API file for the server.
- Adds a discovery resource for serving the Open API file.
- Updates readme: background, quickstart, docker, describes features, apps and grants.
- Males OAuth remote server check the only option.
- Cleans up basic auth option, remove scope check and redundant options.
- Replaces global API keys with per service/member keys.
- Simplifies AWS client configuration.

#### Client 
 
- Reduces declared client deps to SLF4J, RocksDB, and Metrics.

#### Client

- Declared client dependencies are, SLF4J, RocksDB, and Metrics.


### 0.0.4

**Server**

- Changes App to have service and member grants to access features, replacing owners.
- Renames appId to appkey across client and server to reflect what the field actually is.
- Removes FeatureOwner in proto/api model replacing it with Owner.
- Renames optionType to option in from proto/api model.
- Adds type fields to the proto/api model.
- Allows any authenticated principal to create an App via the API.
- Adds an App validator checking minimum fields needed to create.
- Adds a feature validator checking minimum fields and options for create/update.
- Moves docker support to outland-feature-docker sub-project.
- Publishes image to docker hub under dehora/outland-feature-server
- Allows jvm opts to be passed to docker through the env.
- Embeds configuration directly in image, removes previously needed docker mount.
- Makes dropwizard conf items available to docker through the env.
- Adds an example docker-compose file for dehora/outland-feature-server.

**Client**:

- Updates API to match server.


### 0.0.3

**Server**

- Checks the app exists before allowing a feature creation.
- Adds an implementation of HyParView using gRPC.


### 0.0.2

**Server**:

- App support - apps group features for one or more services and belong to owners.
- Allows apps to have be created via the API with owner/services add/removes.
- Make idempotency key checks for repeated app creation requests.
- Add a dynamodb apps and graph table for holding apps and app/member mappings.
- Add a remote OAuth access token check based on Plan B's tokeninfo.
- Externalise auth mechanism choices (basic, oauth) to envars.
- Allow access to any apps/features to an authenticated service/owner access list.

**Client**:

- Add apps, owners and services messages to protocol buffer (not directly exposed yet).
- Set the client's json protobuf parser to must ignore.


### 0.0.1

**Server**:

- Version features with a hybrid logical clock-alike
- Fix server shadow jar's main class so that it starts up.
- Add a docker setup for local use.
- Add feature flag bool definitions that can carry weights

**Client**:

- Evaluate feature flags according to their weights, if defined.


#### 0.0.0

Initial release. An implementation of a feature server API and client. 

**Server**:
  
- Create, update and read features for one or multiple applications.
- Features are stored in DynamoDB and cached in Redis.
- Creates/updates can be requested with an Idempotency-Key header to avoid duplicates.
- Basic (API key) and Oauth (Token) authentication support.
- API errors returned as problem JSON.
- API JSON can be transformed to Protocol Buffers 3

**Client**:

- Request feature enabled checks, optionally throwing exceptions for missing features.
- Work on behalf of one, or all applications.
- Cache features in memory with periodic update checks.
- Store feature data locally in RocksDB to handle unavailable servers.
- Access the feature API server via OAuth or Basic Auth.
- Support loading self-signed/other certs not available in the local keystore.
- Map API JSON to Protocol Buffers.
- Export API access metrics using Dropwizard metrics.
