# Project Rune
Collection of projects I have put together to deepen my skills across various frameworks, architectures, methodologies, and technologies.  The repository and project naming scheme follows various names from the Suikoden games (which was my favorite video game series :smiley:).  Below is a breakdown of all the projects in this repository followed by a brief summary.

---

## Project Harmonia
TBD

---

## Project Dunan
Project based on [GM Direct Parts](https://www.gmpartsdirect.com/) vehicle part list page.

Proposed Technologies (Solution #1)
- [Vue.js](https://vuejs.org/) for frontend application (create-vue)
  - [Nuxt](https://nuxt.com/) for the frontend application (This will happen once Testing capaibilites are stable: https://nuxt.com/docs/getting-started/testing)
- [Primevue](https://primevue.org/) for frontend components
- [FastAPI](https://fastapi.tiangolo.com/) for backend application
- [Redis](https://redis.io/docs/stack/) for database

Proposed Technologies (Solution #2)
- [Laminar](https://laminar.dev/) for the frontend application
- [Laminar SAP UI5 Web Components](https://github.com/sherpal/LaminarSAPUI5Bindings) for frontend components
- Cats Effect
  - [http4s](https://http4s.org/) for backend web server
  - [doobie](https://tpolecat.github.io/doobie/index.html) JDBC Layer
- ZIO
  - [ZIO HTTP](https://zio.dev/zio-http/) or [ZIO gRPC](https://zio.dev/ecosystem/community/zio-grpc) for backend web server
  - [ZIO JDBC](https://zio.dev/zio-jdbc/) JDBC Layer
- Database
  - Postgres with [pgvector extensions](https://github.com/pgvector/pgvector) - Database
  - [Supabase](https://supabase.com/) with pgvector extension - Database (Use Supabase REST API or GraphQL endpoint instead of JDBC)

