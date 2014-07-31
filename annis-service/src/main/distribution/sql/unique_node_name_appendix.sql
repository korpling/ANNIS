UPDATE _node AS n SET unique_name_appendix = 
(
    WITH same AS
    (
      SELECT row_number() OVER () AS r, i.id AS id
      FROM _node AS i WHERE  i.name=n.name AND i.corpus_ref = n.corpus_ref
    )
    SELECT
    CASE WHEN (SELECT same.r FROM same WHERE n.id = same.id) > 1 
    THEN '_' ||(SELECT same.r-1 FROM same  WHERE n.id = same.id) 
    ELSE '' 
    END
);