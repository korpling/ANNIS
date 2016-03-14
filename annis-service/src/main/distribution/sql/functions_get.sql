CREATE OR REPLACE FUNCTION splitanno(qannotext varchar)
  RETURNS text[] AS 
$$
SELECT	CASE WHEN $1 IS NULL THEN NULL
	ELSE (regexp_matches($1, '^([^:]*):([^:]*):(.*)'))
	END
$$
  LANGUAGE sql IMMUTABLE;

-- helper to make the SQL for some import statements easier
CREATE OR REPLACE FUNCTION top_corpus_pre(integer) 
RETURNS integer
AS $$ SELECT pre FROM corpus WHERE id=$1 AND top_level IS TRUE LIMIT 1 $$ STABLE LANGUAGE SQL;
CREATE OR REPLACE FUNCTION top_corpus_post(integer) 
RETURNS integer
AS $$ SELECT post FROM corpus WHERE id=$1 AND top_level IS TRUE LIMIT 1 $$ STABLE LANGUAGE SQL;