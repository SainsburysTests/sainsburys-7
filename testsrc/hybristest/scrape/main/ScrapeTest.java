package hybristest.scrape.main;
/**
 *
 */
import static org.junit.Assert.*;
import hybristest.scrape.util.ScrapeUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

public class ScrapeTest {

	private final static Logger LOG = Logger.getLogger(ScrapeTest.class.getName());
	
	/**
	 * This test was written when I was trying to access the original url and investigating different ways of fetching the doc.
	 * 
	 */
	@Test
	public void parseUrlTest(){
		String TESTURL = "http://www.sainsburys.co.uk/webapp/wcs/stores/servlet/CategoryDisplay?"
				+ "listView=true&orderBy=FAVOURITES_FIRST&parent_category_rn=12518&top_category=12518&langId=44&beginIndex=0&"
				+ "pageSize=20&catalogId=10137&categoryId=185749&storeId=10151&hideFilters=true";
				
		String params = TESTURL.substring(TESTURL.indexOf('?')+1);
		String [] paramsArray = params.split("&|=");
		assertEquals(0,paramsArray.length%2);
		Map<String,String> paramsMap = new LinkedHashMap<String,String>();
		for(int i = 0; i < paramsArray.length -1; i += 2){
			paramsMap.put(paramsArray[i], paramsArray[i+1]);
		}
		//Make sure we have a map the correct size, if not there could be duplicates.
		assertEquals(paramsArray.length/2,paramsMap.size());

	}
	/**
	 * When we access the url we want to get a document back
	 */
	@Test
	public void fetchDocumentByUrlTest() {
		LOG.info("in fetchTest");
		Scrape testScrape = new Scrape();
		Document doc = null;
		try {
			doc = testScrape.fetchDocumentByUrl();
		} catch (IOException e) {			
			LOG.error(e.getMessage());
		}
		assertNotNull(doc);

	}
	/**
	 * When we access the url we want to get a document back
	 */
	@Test
	public void fetchDocumentByFileTest() {
		LOG.info("in fetchTest");
		Scrape testScrape = new Scrape();
		Document doc = null;
		try {
			doc = testScrape.fetchDocumentByFile("site.html");
		} catch (IOException e) {			
			LOG.error(e.getMessage());
		}
		assertNotNull(doc);

	}
	/**
	 * Checks we've found the 15 items on the page.
	 */
	@Test
	public void getItemsTest() {
		LOG.info("in getItemsTest");
		Scrape testScrape = new Scrape();
		Document doc = null;
		try {
			doc = testScrape.fetchDocumentByFile("site.html");
			Elements mainListerDiv = testScrape.getProductLister(doc);
			Iterator<Element> iterator = mainListerDiv.iterator();
			while (iterator.hasNext()) {
				Element e = iterator.next();
				Elements listItems = e.select("li h3");
				assertEquals(15,listItems.size());
			}
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}

	}
	@Test
	public void testFormatPageSizeKB(){
		LOG.info("in testFormatPageSizeKB");
		String formattedSize = ScrapeUtils.formatPageSize(1536L);
		assertEquals("1.50Kb",formattedSize);
	}
	@Test
	public void testFormatPageSizeB(){
		LOG.info("in testFormatPageSizeB");
		String formattedSize = ScrapeUtils.formatPageSize((long) (1024L * 0.5));
		assertEquals("0.50Kb",formattedSize);
	}
	@Test
	public void testFormatPageSizeMB(){
		LOG.info("in testFormatPageSizMB");
		String formattedSize = ScrapeUtils.formatPageSize(5767168L);
		assertEquals("5.50Mb",formattedSize);
	}
	@Test
	public void testFormatPageSizeGB(){
		LOG.info("in testFormatPageSizeGB");
		String formattedSize = ScrapeUtils.formatPageSize(1024 * 1024 * 1024 );
		assertEquals("1.00Gb",formattedSize);
	}
	@Test
	public void testFormatPageSizeZero(){
		LOG.info("in testFormatPageSizeZero");
		String formattedSize = ScrapeUtils.formatPageSize(0 );
		assertEquals("0.00Kb",formattedSize);
	}
	@Test
	public void testFormatPageSizeShouldFail(){
		LOG.info("in testFormatPageSizeShouldFail");
		String formattedSize = ScrapeUtils.formatPageSize(0 );
		assertEquals("0.04440Kb",formattedSize);
	}
	
}
