-- UPDATE _rank SET level = 0 WHERE parent IS NULL;
-- UPDATE _rank SET LEVEL = 1 WHERE parent IN (SELECT pre FROM _rank WHERE level = 0);

CREATE OR REPLACE FUNCTION compute_rank_level() RETURNS INTEGER AS $$
DECLARE
	new_level INTEGER := 0;
	count1 INTEGER := 0;
BEGIN
	RAISE NOTICE 'setting level for root nodes';
	UPDATE _rank SET level = 0 WHERE parent IS NULL;
  
	SELECT COUNT(*) INTO count1 FROM _rank WHERE level IS NULL;
	
	WHILE count1 > 0 LOOP
		new_level := new_level + 1;
		UPDATE _rank child SET level = new_level FROM _rank parent WHERE child.parent = parent.pre AND parent.level = new_level - 1;

		SELECT COUNT(*) INTO count1 FROM _rank WHERE level IS NULL;
		RAISE NOTICE 'set level = %, % nodes to go', new_level, count1;
	
	END LOOP;

    RETURN new_level;
END;
$$ language plpgsql;
