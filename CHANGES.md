### Changes

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
