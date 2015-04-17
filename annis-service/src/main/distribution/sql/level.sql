-- setup computation of level for dominance and pointing relations

ALTER TABLE _rank ADD level integer;

UPDATE _rank c SET "level"=
(
  WITH RECURSIVE levelcalc AS
  (
    SELECT pre, parent, component_ref 
    FROM _rank, _component
    WHERE 
      c.pre = _rank.pre AND c.component_ref = _rank.component_ref AND
      _rank.component_ref = _component.id AND
      _component.type IN ('d', 'p')   
    UNION ALL
    
    SELECT a.pre, a.parent, a.component_ref FROM _rank a, levelcalc l WHERE l.parent = a.id AND l.component_ref = a.component_ref
  )
  SELECT count(*) - 1 as "level" FROM levelcalc
)
FROM _component
WHERE
  c.component_ref = _component.id AND
  _component.type IN ('d', 'p')
;