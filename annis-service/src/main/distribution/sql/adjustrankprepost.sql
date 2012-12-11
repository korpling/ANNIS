ALTER TABLE _rank RENAME pre TO id;
ALTER TABLE _rank ADD pre integer;

DROP TABLE IF EXISTS _premin;
CREATE UNLOGGED TABLE _premin (
  component_ref integer PRIMARY KEY,
  minpre integer
);

INSERT INTO _premin(component_ref, minpre)
SELECT component_ref, min(id) as minpre FROM _rank GROUP BY component_ref;

UPDATE _rank AS r SET 
  pre = id - (SELECT minpre FROM _premin AS m WHERE r.component_ref = m.component_ref),
  post = post - (SELECT minpre FROM _premin AS m WHERE r.component_ref = m.component_ref),
  parent = parent - (SELECT minpre FROM _premin AS m WHERE r.component_ref = m.component_ref)
;

DROP TABLE _premin;
