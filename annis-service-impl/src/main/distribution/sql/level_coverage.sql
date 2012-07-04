-- setup computation of level for coverage entries
UPDATE _rank c SET "level"=
(
CASE WHEN c.parent IS NULL THEN
  0
ELSE
  1
END
)
FROM _component
WHERE
  c.component_ref = _component.id AND
  _component.type IN ('c')
;