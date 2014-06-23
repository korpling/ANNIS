CREATE OR REPLACE FUNCTION splitanno(qannotext varchar)
  RETURNS text[] AS 
$$
SELECT	CASE WHEN $1 IS NULL THEN NULL
	ELSE (regexp_matches($1, '^([^:]*):([^:]*):(.*)'))
	END
$$
  LANGUAGE sql IMMUTABLE;