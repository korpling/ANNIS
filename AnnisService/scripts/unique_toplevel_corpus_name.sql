CREATE OR REPLACE FUNCTION unique_toplevel_corpus_name() RETURNS TRIGGER AS $$
DECLARE
	count INTEGER := 0;
BEGIN
	IF NEW.top_level = 'y' THEN
		PERFORM * FROM corpus WHERE corpus.name = NEW.name AND corpus.top_level = 'y';
		GET DIAGNOSTICS count = ROW_COUNT;
		IF count != 0 THEN
			RAISE EXCEPTION 'conflicting top-level corpus found: %', NEW.name;
		END IF;
	END IF;
	RETURN NEW;
END;
$$ language plpgsql;