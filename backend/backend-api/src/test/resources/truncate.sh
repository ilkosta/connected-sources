#!/bin/bash

docker exec -i backend-pg-1 psql -U cs -d connected_sources -f - <../resources/clean.sql
rm -rf /home/costa/pkg/connected-sources/backend/backend-api/tenants/default
