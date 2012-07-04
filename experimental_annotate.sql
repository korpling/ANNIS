WITH matchednode AS
(
  SELECT ARRAY[inn.id1, inn.id2] AS key, row_number() OVER () as n, inn.*    FROM (
    SELECT DISTINCT
      facts1.id AS id1, facts1.text_ref AS text1, facts1.left_token AS left_token1, facts1.right_token AS right_token1, 
      facts1.corpus_ref AS corpus1, facts1.node_name AS name1, 
      facts2.id AS id2, facts2.text_ref AS text2, facts2.left_token AS left_token2, facts2.right_token AS right_token2, 
      facts2.corpus_ref AS corpus2, facts2.node_name AS name2
    FROM
      facts AS facts1,
      facts AS facts2
    WHERE
      -- annotations can always only be inside a subcorpus/document AND
      -- artificial node subview AND
      facts1.is_token IS TRUE AND
      facts1.n_sample IS TRUE AND
      facts1.right_token = facts2.left_token - 1 AND
      facts1.text_ref = facts2.text_ref AND
      facts1.toplevel_corpus IN (3) AND
      facts2.is_token IS TRUE AND
      facts2.n_sample IS TRUE AND
      facts2.toplevel_corpus IN (3)
    ORDER BY id1, id2
    LIMIT 10 OFFSET 0
  ) AS inn
),
coveredseg AS
(
	SELECT m.key, m.n, f.seg_left - 2 AS min, f.seg_right + 2 AS max, f.text_ref AS text_ref
	FROM facts as f, matchednode AS m 
	WHERE 
    f.toplevel_corpus IN (3) AND
		(
      (f.left_token <= m.right_token1 AND f.right_token >= m.left_token1 AND f.text_ref = m.text1)
      OR
      (f.left_token <= m.right_token2 AND f.right_token >= m.left_token2 AND f.text_ref = m.text2)
		) AND
		f.seg_name = 'norm' AND
		f.n_sample IS true
), 
extendedseg AS
(
	SELECT c.key, c.n, f.left_token, f.right_token, f.text_ref AS text_ref
	FROM coveredseg as c, facts f
	WHERE
    f.toplevel_corpus IN (3) AND
		f.seg_name = 'norm' AND
		-- covered
		f.seg_left <= max AND	f.seg_right >= min AND
		f.text_ref = c.text_ref AND
		f.n_sample IS TRUE
)
SELECT e.key, e.n, f.* FROM extendedseg AS e, facts as f
WHERE
  f.toplevel_corpus IN (3) AND
	f.left_token >= e.left_token AND
	f.right_token <= e.right_token AND
	f.text_ref = e.text_ref
ORDER BY e.n, f.component_id, f.pre
;

