package hybristest.scrape.main;

import hybristest.scrape.util.ScrapeUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.granule.json.JSONArray;
import com.granule.json.JSONException;
import com.granule.json.JSONObject;

public class Scrape {

	private static final String POUND = "£";

	private static final String UNIT = "/unit";

	// This is only hardcoded because it is a test application. It seems a
	// rather long url to add to a properties file
	// or to enter from the command line (This is modified from url in PDF to remove
	// duplicate and empty parameters in a failed attempt to access the page programmatically.)
	private static final String TESTURL = "http://www.sainsburys.co.uk/webapp/wcs/stores/servlet/CategoryDisplay?"
			+ "listView=true&orderBy=FAVOURITES_FIRST&parent_category_rn=12518&top_category=12518&langId=44&beginIndex=0&"
			+ "pageSize=20&catalogId=10137&categoryId=185749&storeId=10151&hideFilters=true";

	private final static Logger LOG = Logger.getLogger(Scrape.class.getName());

	/**
	 * Main entry class for Scrape
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Parse the URL
		Scrape scrape = new Scrape();
		try {
		
			Document doc = scrape.fetchDocumentByUrl();
			Elements mainListerDiv = scrape.getProductLister(doc);
			if (CollectionUtils.isNotEmpty(mainListerDiv)) {
				JSONArray jsonArray = scrape.getJsonArray(mainListerDiv);
				JSONObject results = new JSONObject();
				results.put("results", jsonArray);
				results.put("total", scrape.calculateTotal(jsonArray));
				printResults(results);
			} else {
				LOG.error("Unable to recover product list");
			}

		} catch (IOException | JSONException e) {
			LOG.error("Unable to access URL :" + e.getMessage());
		}
	}
	/**
	 * Totals the prices.
	 * 
	 * @param products
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	private BigDecimal calculateTotal(JSONArray products) throws JSONException {
		BigDecimal total = BigDecimal.ZERO;

		Iterator i = products.iterator();
		while (i.hasNext()) {
			JSONObject product = (JSONObject) i.next();
			String price = product.getString("unit_price");
			if (StringUtils.isNotEmpty(price)) {
				total = total.add(BigDecimal.valueOf(Double.valueOf(price)));
			}
		}
		return total.setScale(2);
	}
	/**
	 * Prints the results using the logger
	 * 
	 * @param results
	 * @throws JSONException
	 */
	private static void printResults(JSONObject results) throws JSONException {
		LOG.info(results.toString(4));
	}
	/**
	 * Returns the product list items as a json array
	 * 
	 * @param mainListerDiv
	 * @return JSONArray
	 * @throws JSONException
	 */
	public JSONArray getJsonArray(final Elements mainListerDiv)
			throws JSONException {

		JSONArray productArray = new JSONArray();
		Iterator<Element> iterator = mainListerDiv.iterator();
		while (iterator.hasNext()) {
			Element element = iterator.next();
			Elements products = element.select("li h3");
			for (Element product : products) {
				try {
					productArray.add(createJsonObject(product));
				} catch (IOException e) {

					LOG.warn(e.getMessage());
				}
			}
		}

		return productArray;
	}

	/**
	 * Create a json object from the list element
	 * 
	 * @param product
	 * @param total
	 * @return the product as a jsonObject
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject createJsonObject(final Element product)
			throws IOException, JSONException {
		String productUrl = getProductUrlFromListItemHeader(product);
		final Document page = fetchProductPage(productUrl);
		JSONObject object = new JSONObject();
		object.put("title", getTitle(page));
		object.put("size", getSize(productUrl));
		object.put("unit_price", getUnitPrice(page));
		object.put("description", getDescription(page));

		return object;
	}

	/**
	 * Gets the title or an empty string if no title is found
	 * 
	 * @param a
	 *            product page
	 * @return the title of the page
	 */
	private String getTitle(final Document page) {
		Elements elements = page.select("h1");
		if (CollectionUtils.isNotEmpty(elements)) {
			return elements.first().text();
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Gets the unit price or an empty string if no unit price is found
	 * 
	 * @param page
	 * @param total
	 * @return the unit price element
	 */
	private String getUnitPrice(final Document page) {
		Elements elements = page.select(".pricePerUnit");
		if (CollectionUtils.isNotEmpty(elements)) {
			return parsePrice(elements.first().text());
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Removes the currency and Unit text returns empty string if formatted
	 * price not in £3.50/unit format
	 * 
	 * @param text
	 * @return the amount per unit
	 */
	private String parsePrice(final String formattedPrice) {
		if (StringUtils.isEmpty(formattedPrice)
				|| formattedPrice.indexOf(POUND) == -1
				|| formattedPrice.indexOf(UNIT) == -1) {
			return StringUtils.EMPTY;
		} else {
			return formattedPrice.substring(formattedPrice.indexOf(POUND) + 1,
					formattedPrice.indexOf(UNIT));
		}
	}

	/**
	 * Gets the description or an empty string if no description is found
	 * 
	 * @param page
	 * @return
	 */
	private String getDescription(final Document page) {
		Elements elements = page.select("h3");
		if (CollectionUtils.isNotEmpty(elements)) {
			for (Element element : elements) {
				if (elements.first().hasClass("productDataItemHeader")
						&& "Description".equals(element.text())) {
					return elements.first().nextElementSibling().text();
				}
			}
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Gets the size of the body,
	 * 
	 * @param productUrl
	 * @return
	 * @throws IOException
	 */
	private String getSize(final String productUrl) throws IOException {
		Response response = Jsoup.connect(productUrl).maxBodySize(0)
				.userAgent("Mozilla/5.0").timeout(600000).execute();

		return ScrapeUtils
				.formatPageSize(Long.valueOf(response.bodyAsBytes().length));
	}

	/**
	 * 
	 * @param product
	 * @return
	 */
	private String getProductUrlFromListItemHeader(final Element product) {
		Elements urls = product.select("a[href]");
		return urls.first().attr("href");
	}

	/**
	 * Returns the page as a document Using a file for the input as using the
	 * Jsoup.connect()... call didn't work, probably due to dynamic page
	 * generation. No time to investigate fully. Source was saved to file from
	 * Firefox.
	 * 
	 * @return the page as a document
	 * @throws IOException
	 * 
	 */
	public Document fetchDocumentByUrl() throws IOException {
	    File input = new File("site.html");
		return Jsoup.parse(input, "UTF-8", TESTURL);
		
//		 The following didn't  work. Left in for posterity.
//		 Map<String,String> paramsMap = new LinkedHashMap<String,String>();
//		 String url = parseUrlForParameters(TESTURL,paramsMap);
//		 return
//		 Jsoup.connect(url).maxBodySize(0).data(paramsMap).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
//		 .timeout(0).ignoreContentType(true).get();
	}

	/**
	 * Returns the product page as a document
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Document fetchProductPage(final String url) throws IOException {

		return Jsoup.connect(url).maxBodySize(0).userAgent("Mozilla/5.0")
				.timeout(0).get();
	}
	/**
	 * Returns the product listView elements
	 * 
	 * @param doc
	 * @return Elements as a list
	 */
	public Elements getProductLister(final Document doc) {
		return doc.select(".listView");

	}
	/**
	 * Used to pass parameters as a data map. However this attempt to get the
	 * page dynamically 
	 */
	public String parseUrlForParameters(final String url,
			Map<String, String> paramsMap) {

		String params = url.substring(url.indexOf('?') + 1);
		String[] paramsArray = params.split("&|=");

		for (int i = 0; i < paramsArray.length - 1; i += 2) {
			paramsMap.put(paramsArray[i], paramsArray[i + 1]);
		}
		return url.substring(0, url.indexOf('?'));
	}
}
