# Project Dunan
Project Dunan is a service catalog for vehicle owners to purchase parts.  This site is heavily inspired by [GM Direct Parts](https://www.gmpartsdirect.com/).

## Milestones
- [ ] Create "Part Explorer" feature
- [ ] Create "Part Finder" feature

## Project Modules
- TBD


### Project Notes
#### Solution #1 Technologies
- [Vue.js](https://vuejs.org/) for frontend application (create-vue)
    - [Nuxt](https://nuxt.com/) for the frontend application (This will happen once Testing capaibilites are stable: https://nuxt.com/docs/getting-started/testing)
- [Primevue](https://primevue.org/) for frontend components
- [FastAPI](https://fastapi.tiangolo.com/) for backend application
- [Qdrant](https://qdrant.tech/) for vector database (similarity search)
- [Dgraph](https://dgraph.io/) for graph database (part explorer)

#### Proposed Technologies (Solution #2)
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