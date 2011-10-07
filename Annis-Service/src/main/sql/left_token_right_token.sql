-- BEGIN;

ALTER TABLE _node ADD left_token integer, ADD right_token integer;

CREATE TEMPORARY TABLE spannendtoken
AS
(
  WITH RECURSIVE leftright AS
  (
    SELECT n.id as id, r.parent as parent, r.pre AS pre, n.token_index AS left_token,
          n.token_index as right_token
    FROM _node as n, _rank as r
    WHERE
      n.token_index is not null
      AND r.node_ref = n.id

    UNION ALL

    SELECT r.node_ref AS id, r.parent AS parent, r.pre AS pre,
          l.left_token AS left_token, l.right_token AS right_token
    FROM _rank as r, _component as c, leftright as l
    WHERE
      l.parent = r.pre AND c.type in ('c', 'd')
      AND r.component_ref = c.id
  )
  SELECT id, min(left_token) AS left_token, max(right_token) as right_token
  FROM leftright
  GROUP BY id
);

CREATE INDEX _idx_spannendtoken_id on spannendtoken(id);

ANALYZE spannendtoken;

UPDATE _node SET
left_token = (SELECT left_token FROM spannendtoken AS s WHERE s.id = _node.id),
right_token = (SELECT right_token FROM spannendtoken AS s WHERE s.id = _node.id)
;

DROP TABLE spannendtoken;
-- COMMIT;