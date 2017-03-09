### Changes

### 0.0.1

**Server**:

- Version features with a hybrid logical clock-alike
- Fix server shadow jar's main class so that it starts up.


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
