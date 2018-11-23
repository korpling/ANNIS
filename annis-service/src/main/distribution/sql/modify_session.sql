--- Session
-- SET enable_seqscan TO off;
-- SET enable_nestloop TO off;
-- SET enable_mergejoin TO off;
-- SET enable_hashjoin TO off;
-- SET enable_bitmapscan TO off;
-- SET enable_material TO off;
SET geqo_effort=10;
SET geqo_pool_size=2000;
SET geqo_seed=0.2;  