#!/bin/bash

/home/rds/app/bin/backup_db

cd /home/rds/app/data/tables

cp -p \
   adminLog.sql \
   adminPermissions.sql \
   admin.sql \
   dashboardDataColumns.sql \
   dashboardDataDetailButtons.sql \
   dashboardDataDetailFields.sql \
   dashboardDataDetailSelectors.sql \
   dashboardDataDetails.sql \
   dashboardDataDetailTables.sql \
   dashboardDataSources.sql \
   dashboardDataTableSelectors.sql \
   dashboardDataTables.sql \
   dashboardLinks.sql \
   dashboard.sql \
   reportItems.sql \
   reports.sql \
   webObjects.sql \
/home/rds/src/dashboard/sql

