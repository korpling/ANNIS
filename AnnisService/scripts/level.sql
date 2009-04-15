ALTER TABLE _rank ADD level integer;

-- create indexes for better performance
CREATE INDEX _idx_rank__parent ON _rank (parent);
CREATE INDEX _idx_rank__level_pre ON _rank (level, pre);

-- rank.level is computed recursively in a stored procedure
SELECT compute_rank_level();

-- drop those indexes
DROP INDEX _idx_rank__parent;
DROP INDEX _idx_rank__level_pre;
