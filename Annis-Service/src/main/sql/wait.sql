-- UPDATE _rank SET level = 0 WHERE parent IS NULL;
-- UPDATE _rank SET LEVEL = 1 WHERE parent IN (SELECT pre FROM _rank WHERE level = 0);

CREATE OR REPLACE FUNCTION wait(seconds INTEGER) RETURNS INTEGER
AS $$
	import time
	time.sleep(seconds)
	return seconds
$$ language plpythonu;
