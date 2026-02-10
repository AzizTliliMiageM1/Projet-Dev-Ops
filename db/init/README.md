Local MySQL initialization scripts

- `schema.sql` : creates `users` and `abonnements` tables.
- `sample_data.sql` : inserts a few sample users and subscriptions for demo.

How it works
- Files placed in `db/init` are automatically executed by the official MySQL Docker image on first container initialization (when the volume is empty).
- To re-run the scripts, remove the `db_data` volume and restart:

```bash
docker-compose down
docker volume rm Projet-Dev-Ops-main_db_data
docker-compose up --build
```

If you prefer to apply scripts to a running container instead of recreating the volume:

```bash
docker-compose exec -T db sh -c 'mysql -u root -p"$MYSQL_ROOT_PASSWORD" abonnementsdb' < db/init/schema.sql
docker-compose exec -T db sh -c 'mysql -u root -p"$MYSQL_ROOT_PASSWORD" abonnementsdb' < db/init/sample_data.sql
```
