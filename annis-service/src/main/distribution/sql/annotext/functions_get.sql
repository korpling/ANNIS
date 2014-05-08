CREATE OR REPLACE FUNCTION splitanno(qannotext varchar)
  RETURNS text[] AS
$BODY$
SELECT	CASE WHEN qannotext IS NULL THEN NULL
	ELSE (regexp_matches(qannotext, '^([^:]*):([^:]*):(.*)'))
	END
$BODY$
  LANGUAGE sql IMMUTABLE;
