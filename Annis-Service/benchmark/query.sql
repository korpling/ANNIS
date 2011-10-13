BEGIN;
SELECT 	facts1.id AS id1, facts1.text_ref AS text_ref1, facts1.left_token AS left_token1, facts1.right_token AS right_token1 FROM 	facts AS facts1 WHERE 	facts1.is_token IS TRUE AND 	facts1.toplevel_corpus IN (7) AND 	(facts1.sample & B'10000') = B'10000'  ;
END;

BEGIN;
SELECT 	facts1.id AS id1, facts1.text_ref AS text_ref1, facts1.left_token AS left_token1, facts1.right_token AS right_token1 FROM 	facts AS facts1 WHERE 	facts1.is_token IS TRUE AND 	facts1.toplevel_corpus IN (6) AND 	(facts1.sample & B'10000') = B'10000'  ;
END;

BEGIN;
SELECT DISTINCT 	facts1.id AS id1, facts1.text_ref AS text_ref1, facts1.left_token AS left_token1, facts1.right_token AS right_token1, 	facts2.id AS id2, facts2.text_ref AS text_ref2, facts2.left_token AS left_token2, facts2.right_token AS right_token2 FROM 	facts AS facts1, 	facts AS facts2 WHERE 	facts2.edge_type = 'd' AND 	facts2.edge_name IS NULL AND 	facts1.pre = facts2.parent AND 	facts1.toplevel_corpus IN (7) AND 	(facts1.sample & B'00100') = B'00100' AND 	facts2.toplevel_corpus IN (7) AND 	(facts2.sample & B'00100') = B'00100' AND 	facts1.corpus_ref = facts2.corpus_ref  ;
END;

