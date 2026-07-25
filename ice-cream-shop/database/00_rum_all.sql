-- =====================================================
-- RUN ALL SCRIPTS IN ORDER
-- =====================================================

\echo 'Creating database...'
\i 01_create_database.sql

\c ice_cream_shop

\echo 'Creating tables...'
\i 02_create_tables.sql

\echo 'Inserting data...'
\i 03_insert_data.sql

\echo 'Creating views...'
\i 04_create_views.sql

\echo 'Creating indexes...'
\i 05_create_indexes.sql

\echo '✅ All scripts executed successfully!'
