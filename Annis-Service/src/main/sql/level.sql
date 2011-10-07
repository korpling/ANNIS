--set enable_mergejoin = false;

-- compute real roots
-- actually, roots of components that are not actual roots should link parent to their parent node (even though it is in another component)
-- BEGIN;
ALTER TABLE _rank ADD root boolean DEFAULT 'n';

CREATE TEMPORARY TABLE _tmp_real_roots AS SELECT node_ref
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

CREATE TEMPORARY TABLE tmplevels
AS
(
  WITH RECURSIVE levelcalc AS
  (
    SELECT r.pre as pre, 0 as level
    FROM _rank as r
    WHERE
      r.parent is null

    UNION ALL

    SELECT r.pre as pre, l.level+1 as level
    FROM _rank as r, levelcalc as l
    WHERE
      r.parent = l.pre
  )
  SELECT * FROM levelcalc as l
);
CREATE INDEX _idx_tmplevels_pre on tmplevels(pre);

ANALYZE tmplevels;

UPDATE _rank SET
level = (SELECT level FROM tmplevels AS l WHERE l.pre = _rank.pre)
WHERE _rank.pre is not null;

-- drop those indexes
DROP INDEX _idx_rank__parent;
DROP INDEX _idx_tmplevels_pre;

DROP TABLE tmplevels;

--set enable_mergejoin = true;