package de.deutschdiachrondigital.dddquery.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import annisservice.AnnisService;
import annisservice.exceptions.AnnisCorpusAccessException;
import annisservice.exceptions.AnnisQLSemanticsException;
import annisservice.exceptions.AnnisQLSyntaxException;

public class Shell {

	private AnnisService annisService;
//	private AnnisQlTranslator dddQueryMapper;
//	private GraphMatcher graphMatcher;
//	private AnnotationRetriever annotationRetriever;

	public static void main(String[] args) {
		Shell shell = new BeanFactory().getShell();
		boolean done = false;
		while ( ! done ) try {
			done = shell.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean run() throws IOException, SQLException, ClassNotFoundException, InconsistentDataException  {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String last = null;

		new BeanFactory().runService();
		
		while (true) {
			System.out.print("--- DDDquery: ");

			String input = in.readLine();
			if (input == null)
				return true;

			if (".".equals(input))
				input = last;

			last = input;
			//			input = dddQueryMapper.translate(input);
//			
//			List<Match> matches = graphMatcher.matchGraph(input);
//			
//			if ( ! matches.isEmpty() )
//				annotationRetriever.retrieveAnnotations(matches, 1, 1);
			
			
			List<Long> corpusList = Arrays.asList(1L);
			try {
				int count = annisService.getCount(corpusList, input);
				if (count > 0)
					annisService.getResultSet(corpusList, input, 25, 1, 1, 1);
			} catch (AnnisQLSemanticsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AnnisQLSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AnnisCorpusAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
		
//	public GraphMatcher getGraphMatcher() {
//		return graphMatcher;
//	}
//
//	public void setGraphMatcher(GraphMatcher generator) {
//		this.graphMatcher = generator;
//	}
//
//	public AnnotationRetriever getAnnotationRetriever() {
//		return annotationRetriever;
//	}
//
//	public void setAnnotationRetriever(AnnotationRetriever annotationRetriever) {
//		this.annotationRetriever = annotationRetriever;
//	}
//
//	public AnnisQlTranslator getDddQueryMapper() {
//		return dddQueryMapper;
//	}
//
//	public void setDddQueryMapper(AnnisQlTranslator dddQueryMapper) {
//		this.dddQueryMapper = dddQueryMapper;
//	}

	public AnnisService getAnnisService() {
		return annisService;
	}

	public void setAnnisService(AnnisService annisService) {
		this.annisService = annisService;
	}


}
