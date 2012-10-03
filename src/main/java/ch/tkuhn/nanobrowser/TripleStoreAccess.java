package ch.tkuhn.nanobrowser;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

public class TripleStoreAccess {
	
	// No instances allowed:
	private TripleStoreAccess() {}
	
	private static String endpointURL = NanobrowserApplication.getProperty("sparql-endpoint-url");
	private static SPARQLRepository repo = new SPARQLRepository(endpointURL);
	private static QueryLanguage lang = QueryLanguage.SPARQL;
	
	public static List<BindingSet> getTuples(String sparqlQuery) {
		List<BindingSet> tuples = new ArrayList<BindingSet>();
		try {
			RepositoryConnection connection = repo.getConnection();
			try {
				TupleQuery tupleQuery = connection.prepareTupleQuery(lang, sparqlQuery);
				TupleQueryResult result = tupleQuery.evaluate();
				try {
					while (result.hasNext()) {
						tuples.add(result.next());
					}
				} finally {
					result.close();
				}
			} finally {
				connection.close();
			}
		} catch (OpenRDFException ex) {
			ex.printStackTrace();
		}
		return tuples;
	}

	public static List<Statement> getGraph(String query) {
		List<Statement> triples = new ArrayList<Statement>();
		try {
			RepositoryConnection connection = repo.getConnection();
			try {
				GraphQuery graphQuery = connection.prepareGraphQuery(lang, query);
				GraphQueryResult result = graphQuery.evaluate();
				try {
					while (result.hasNext()) {
						triples.add(result.next());
					}
				} finally {
					result.close();
				}
			} finally {
				connection.close();
			}
		} catch (OpenRDFException ex) {
			ex.printStackTrace();
		}
		return triples;
	}
	
	public static void runUpdateQuery(String query) {
		// SPARQLRepository does not implement update queries
		try {
			// TODO use post instead of get request
			URL url = new URL(endpointURL + "?query=" + URLEncoder.encode(query, "UTF8"));
			url.openStream().close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static List<Statement> sortTriples(List<Statement> triples) {
		Collections.sort(triples, tripleComparator);
		return triples;
	}
	
	public static Comparator<Statement> tripleComparator = new Comparator<Statement>() {
		
		public int compare(Statement o1, Statement o2) {
			int d = o1.getSubject().stringValue().compareTo(o2.getSubject().stringValue());
			if (d == 0) {
				String p1 = o1.getPredicate().stringValue();
				String p2 = o2.getPredicate().stringValue();
				d = p1.compareTo(p2);
				if (d != 0) {
					if (p1.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) d = -1;
					if (p2.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) d = 1;
				}
			}
			if (d == 0) {
				d = o1.getObject().stringValue().compareTo(o2.getObject().stringValue());
			}
			return d;
		};
		
	};

}
