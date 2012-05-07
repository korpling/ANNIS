-- setup computation of level
ALTER TABLE _rank ADD level integer;

UPDATE _rank c SET "level"=
(
  WITH RECURSIVE levelcalc AS
  (
    SELECT pre, parent, component_ref FROM _rank WHERE c.pre = _rank.pre AND c.component_ref = _rank.component_ref
    UNION ALL
    SELECT a.pre, a.parent, a.component_ref FROM _rank a, levelcalc l WHERE l.parent = a.pre AND l.component_ref = a.component_ref
  )
  SELECT count(*) - 1 as "level" FROM levelcalc
);