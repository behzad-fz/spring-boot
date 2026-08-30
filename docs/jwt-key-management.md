# JWT signing key management

## Where the key lives

The HS256 signing key is **not** in the repository. `TokenService` reads it from
the `jwt.secret` configuration property, which the base `application.properties`
maps to the `JWT_SECRET` environment variable:

```
jwt.secret=${JWT_SECRET}
```

The app **fails fast on startup** if `JWT_SECRET` is unset/empty (the
`TokenService` constructor rejects a blank key, and Spring rejects a missing
placeholder before that).

The key is a base64-encoded value of at least 256 bits (32 bytes), as required
by the `jjwt` `hmacShaKeyFor` HS256 path.

## Setting it locally

Export it before starting the app (either in the shell or start script):

```sh
export JWT_SECRET="<base64-of-a-32+-byte-secret>"   # generate with: openssl rand -base64 48
./mvnw spring-boot:run
```

## Test profile

`application-test.properties` carries an obvious, throwaway dummy key solely so
the H2 test profile can boot and round-trip tokens. It is a fixture, never the
real value.

## Rotating the key

1. Generate a new secret (see above).
2. Update `JWT_SECRET` in every environment that runs the app (deploy config /
   secrets store — never in source control).
3. Rolling restart: because verification uses the same key as signing, old
   tokens become invalid as soon as the new value is live. Issue new tokens or
   coordinate a maintenance window; there is no dual-key grace period in this
   codebase.