-- compute real roots
-- actually, roots of components that are not actual roots should link parent to their parent node (even though it is in another component)
-- BEGIN;
ALTER TABLE _rank ADD root boolean DEFAULT 'n';

CREATE TABLE _tmp_real_roots AS SELECT node_ref
FROM _rank 
GROUP BY node_ref
HAVING count(distinct _rank.parent) = 0;

UPDATE _rank SET root = 'y' 
FROM _tmp_real_roots roots 
WHERE _rank.node_ref = roots.node_ref;

DROP TABLE _tmp_real_roots;

-- setup computation of level
ALTER TABLE _rank ADD level integer;

-- create indexes for better performance
CREATE INDEX _idx_rank__parent ON _rank (parent);
CREATE INDEX _idx_rank__level_pre ON _rank (level, pre);

-- rank.level is computed recursively in a stored procedure
SELECT compute_rank_level();

-- drop those indexes
DROP INDEX _idx_rank__parent;
DROP INDEX _idx_rank__level_pre;
