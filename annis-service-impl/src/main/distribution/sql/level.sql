-- setup computation of level
ALTER TABLE _rank ADD level integer;

UPDATE _rank c SET "level"=
(
  WITH RECURSIVE levelcalc AS
  (
    SELECT pre, parent FROM _rank WHERE c.pre = _rank.pre
    UNION ALL
    SELECT a.pre, a.parent FROM _rank a, levelcalc l WHERE l.parent = a.pre
  )
  SELECT count(*) - 1 as "level" FROM levelcalc
);