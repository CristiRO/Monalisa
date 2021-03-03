package lia.Monitor.modules;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ElasticsearchQueryCreator {
	private static final String QUERY_AGGREGATOR_NAME = "load_time_outlier";
	static final Logger logger = Logger.getLogger(ElasticsearchQueryCreator.class.getCanonicalName());
	
	private final HttpClient client = HttpClient.newHttpClient();
	private final String esEndpoint;
	private final String esPort;
	
	public ElasticsearchQueryCreator(String esEndpoint, String esPort) {
		this.esEndpoint = esEndpoint;
		this.esPort = esPort;
	}
	
	public Map<Double, Double> getPercentilesForIndex(String start, String stop,
														Set<Double> percentiles,
														String index, String field)
														throws IOException, InterruptedException {
		logger.info("Constructing the request");
		final BodyPublisher body = BodyPublishers.ofString(getRequestBody(start, stop, percentiles, field));
		logger.info("Sending the request");
		final HttpRequest request = getRequest(body, index);
		logger.info("Here is the request to be sent " + request.toString());
		final HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		logger.info("Here is the response " + response.body());
		return processResponse(response.body());
	}
	
	private Map<Double, Double> processResponse(String responseBody) {
		logger.log(Level.INFO, responseBody);

		final JSONParser parser = new JSONParser();
		Object parseResult;
		try {
			parseResult = parser.parse(responseBody);
		} catch (final ParseException e) {
			logger.log(Level.WARNING, "Exception parsing content of " + responseBody, e);
			return null;
		}
		final JSONObject root = (JSONObject) parseResult;
		final JSONObject aggregations = (JSONObject) root.get("aggregations");
		final JSONObject values = (JSONObject) ((JSONObject) aggregations.get(QUERY_AGGREGATOR_NAME)).get("values");
		
		HashMap<Double, Double> res = new HashMap<>();
		for (String percentile : (Set<String>) values.keySet()) {
			res.put(Double.parseDouble(percentile), Double.parseDouble((String) values.get(percentile)));
		}
		
		return res;
	}
	
	private HttpRequest getRequest(BodyPublisher body, String indexName) {
		return HttpRequest.newBuilder()
				.method("GET", body)
				.uri(URI.create(esEndpoint + ":" + esPort + "/" + indexName + "/_search"))
				.setHeader("Content-Type", "application/json")
				.build();
	}
	
	private String getRequestBody(String start, String stop, Set<Double> percentiles, String field) {
		final String percentilesBody;
		if (percentiles.isEmpty()) {
			percentilesBody = "{\"field\":\"" + field + "\"}";
		} else {
			percentilesBody = "{\"field\":\"" + field + "\",\"percents\":" + getJsonStringFromPercentiles(percentiles) + "}";
		}
		
		return "{\"size\":0,\"query\":{\"range\":{\"@timestamp\":{\"gte\":" + start +
				",\"lte\":" + stop + "}}},\"aggs\":{\"" + QUERY_AGGREGATOR_NAME +
				"\":{\"percentiles\":" + percentilesBody + "}}}";
	}

	private String getJsonStringFromPercentiles(Set<Double> percentiles) {
		List<String> p = percentiles
				.stream()
				.map(percent -> Double.toString(percent))
				.collect(Collectors.toList());
		StringBuffer res = new StringBuffer();
		res.append("[");
		res.append(String.join(",", p));
		res.append("]");
		return res.toString();
	}
}
