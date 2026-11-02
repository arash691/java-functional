# INC-38 hints: production uses the wrong page size

<details><summary>Level 1</summary>

The yaml has a value for production. The running production app does not use it. So either the profile is not active, or something with higher priority overrides it.
</details>

<details><summary>Level 2</summary>

Ops starts production with `SPRING_PROFILES_ACTIVE=production`. Compare that string with the name used in `shopflow-w11.yml`.
</details>

<details><summary>Level 3</summary>

Start the app with the profile and ask Spring where a property comes from: expose the `env` endpoint locally and call `/actuator/env/shopflow.catalog.page-size`; also look at `activeProfiles` in `/actuator/env`.
</details>

<details><summary>Level 4</summary>

Search: "Spring Boot profile-specific document spring.config.activate.on-profile" and "Spring Boot externalized configuration order".
</details>

<details><summary>Level 5</summary>

The document is activated by `on-profile: prod` but the active profile is `production`. Make the names match (and decide on one name for the whole team).
</details>
