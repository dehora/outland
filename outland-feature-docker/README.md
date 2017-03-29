# Outland Docker

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Docker Server Configuration](#docker-server-configuration)
  - [Configuration Settings](#configuration-settings)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Docker Server Configuration

The table below shows the list of server settings that can be set via the environment.

The notable and somewhat essential settings are as follows:

**local/docker**:

- `OUTLAND_FEATURE_AUTH_BASIC_ENABLED`: This can be handy for local dev. There's no way by design to disable auth.

- `OUTLAND_FEATURE_AUTH_BASIC_API_KEYS`: A list of known "API Keys" the server knows about; if 
you leave basic on, you'll want something in this.

- `OUTLAND_FEATURE_MULTIPLE_APP_ACCESS_GRANT_LIST`: If you want to enable a whitelisted service 
such as a UI that is granted access for any App's features. The service still has to authenticate.


**production/online**

- `OUTLAND_FEATURE_ENV`: Set to "production" to trigger IAM.

- `OUTLAND_FEATURE_BASE_URI`: Set to your domain.

- `OUTLAND_FEATURE_DYNAMODB_REGION`: Set to your AWS region.

- `OUTLAND_FEATURE_DYNAMODB_URL`: As defined by your AWS region.

- `OUTLAND_FEATURE_AUTH_BASIC_ENABLED`: Ideally the API key option is disabled in the real world 
unless you have a locked down network and a key rotation story (and even then it's a thing).

- `OUTLAND_FEATURE_AUTH_OAUTH_ENABLED`: ideally the OAuth token server option is enabled in 
the real world.

- `OUTLAND_FEATURE_MULTIPLE_APP_ACCESS_GRANT_LIST`: If you want to enable a whitelisted service 
such as a UI that is granted access for any App's features. The service still has to authenticate.

- `OUTLAND_FEATURE_REDIS_CACHE_HOST`, `OUTLAND_FEATURE_REDIS_IDEM_HOST`: If you want to control 
the redis host names. Low volume services can keep these two the same, busy servers might want 
a dedicated node for feature lookups.

- `JAVA_OPTS`: The  settings in `.env` are not insane but you'll probably want to 
increase the `Xmx` and `Xms`.

### Configuration Settings

| Name                                                 | Description                                                                                                                 | Default                              | Importance |
| ---------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- | ------------------------------------ | ---------- |
| OUTLAND_FEATURE_ENV                                  | The working environment. Setting this to "production" tells AWS to use IAM to source credentials                            | "docker"                             | medium     |
| OUTLAND_FEATURE_BASE_URI                             | The URI used by the server to generate links. This affects Location header results in the API.                              | http://localhost:8180                | medium     |
| OUTLAND_FEATURE_API_IDLE_TO                          | How long the server will wait on idle connections. Note this is a literal string, eg "30 seconds".                          | "10 seconds"                         | low        |
| OUTLAND_FEATURE_AWS_KEY                              | The access key used to source non-IAM credentials. If the env is "production" IAM is used instead.                          | "AWS_KEY"                            | low        |
| OUTLAND_FEATURE_AWS_SECRET                           | The access key used to source non-IAM credentials. If the env is "production" IAM is used instead.                          | "AWS_SECRET"                         | low        |
| OUTLAND_FEATURE_DYNAMODB_URL                         | The DynamoDB URL used by the AWS client. The default relies on docker-compose running ddb local.                            | "http://dynamodb:8000"               | high       |
| OUTLAND_FEATURE_DYNAMODB_REGION                      | The signing region used by the AWS client.                                                                                  | "eu-central-1"                       | high       |
| OUTLAND_FEATURE_TABLE_FEATURES                       | The name of the DynamoDB features table. This table stores feature data.                                                    | "dev_outland_features"               | medium     |
| OUTLAND_FEATURE_TABLE_GRAPHS                         | The name of the DynamoDB graphs table. This table stores associations between apps, owners and grants.                      | "dev_outland_app_graph"              | medium     |
| OUTLAND_FEATURE_TABLE_APPS                           | The name of the DynamoDB apps table. This table stores app data.                                                            | "dev_outland_apps"                   | medium     |
| OUTLAND_FEATURE_AUTH_BASIC_ENABLED                   | Determines if basic auth API key option is enabled. This is useful for local work without an OAuth server.                  | true (enabled)                       | high       |
| OUTLAND_FEATURE_AUTH_BASIC_AUTH_POLICY               | Determines the basic auth policy. This is currently fixed to use shared secrets (API keys).                                 | "basic_app_username_password_keys"   | low        |
| OUTLAND_FEATURE_AUTH_BASIC_API_KEYS                  | The list of shared API keys accepted by the server for basic auth. Leaving this empty causes basic auth to reject requests. | empty list                           | high       |
| OUTLAND_FEATURE_AUTH_BASIC_SCOPE_POLICY              | Whether scope checks are performed using basic auth. There's no scope support yet, enabling this breaks basic auth.         | "basic_disable_scope_check"          | low        |
| OUTLAND_FEATURE_AUTH_BASIC_CACHE_CREDENTIALS_SECONDS | How long to cache authenticated credentials received via basic auth.                                                        | 30                                   | low        |
| OUTLAND_FEATURE_AUTH_OAUTH_ENABLED                   | Determines if OAuth authentication is enabled. This is recommended for production/online environments over basic auth.      | false (disabled)                     | high       |
| OUTLAND_FEATURE_AUTH_OAUTH_AUTH_POLICY               | The oauth policy. Currently only a remote call to an oauth server is supported.                                             | "oauth_bearer_check" (remote check)  | low        |
| OUTLAND_FEATURE_AUTH_OAUTH_SCOPE_POLICY              | Determines whether scopes are checked after the bearer is authenticated. Disabled by default.                               | "oauth_disable_scope_check"          | medium     |
| OUTLAND_FEATURE_AUTH_OAUTH_CACHE_TOKEN_SECONDS       | How long to cache authenticated credentials received via Oauth.                                                             | 30                                   | low        |
| OUTLAND_FEATURE_REMOTE_OAUTH_SERVER                  | The remote OAuth server url used to confirm bearer tokens and return information about the principal.                       | "https://localhost/oauth2/tokeninfo" | high       |
| OUTLAND_FEATURE_AUTH_OAUTH_CONNECT_TO_MILLIS         | How long to wait for a connection to the OAuth server.                                                                      | 3000 (millis)                        | low        |
| OUTLAND_FEATURE_AUTH_OAUTH_READ_TO_MILLIS            | How long to wait for a read from the connected OAuth server.                                                                | 3000 (millis)                        | low        |
| OUTLAND_FEATURE_MULTIPLE_APP_ACCESS_GRANT_LIST       | A list of services or members that are granted access to all apps. Typically used for a console or UI client.               | empty                                | medium     |
| JAVA_OPTS                                            | JVM options.                                                                                                                | See the docker .env file             | medium     |
| OUTLAND_FEATURE_REDIS_MAXCONN                        | The maximum number of redis cache connections used by Jedis internally.                                                     | 8                                    | low        |                                                                                                                | See the docker .env file             | medium     |
| OUTLAND_FEATURE_REDIS_CACHE_HOST                     | Redis feature cache hostname.                                                                                               | "redis"                              | medium     |
| OUTLAND_FEATURE_REDIS_CACHE_PORT                     | Redis feature cache post.                                                                                                   | 6379                                 | medium     |
| OUTLAND_FEATURE_REDIS_IDEM_HOST                      | Redis idempotency key hostname.                                                                                             | "redis"                              | medium     |
| OUTLAND_FEATURE_REDIS_MAXCONN                        | Redis idempotency key post.                                                                                                 | 6379                                 | medium    |


