# Readme file for Sainsbury’s Software Engineering Test

 __NB Having just received the correct URL I have updated the code to read a page
 from file as well as from URL. The intention was to make the app more configurable
  eg. having URL(s) in a config file or passed in via command line. However having originally been given an
 incorrect URL this plan fell by the wayside.__
  
* Overview - The solution is written in Java using the Eclipse IDE (Luna). 
The solution is incomplete inasmuch that I couldn't  download the initial page.
However I found the challenge interesting so the following is as far as I got.

* I have written a console application to consume a specific webpage,  process
 some data and present it. One stumbling block was that the scraping library I 
 chose, jsoup, does not seem to like dynamically generated pages, or that was my
 assumption. I did spend some time looking for a solution but the search was unsuccessful.
 I carried on with the task using the source of the page as saved from Firefox. The 
 saved page source is included in the supplied artifacts. Jsoup didn't have any trouble
 accessing the product page links from the saved page. I have since been sent the correct
 test specification.
 
 The url in the original  pdf contained duplicate and 'empty' parameters.

* I wrote a few unit tests - in ScrapeTest.java. As I had the saved paged as a static file the 
  tests use the same page. The tests are incomplete in that there are not enough edge cases.
  Additional testing would include using a mocking library to check for test cases where the
  page does not meet the specific format, the code as is manages OK with that.

* The Junit test cases can be run in Eclipse.

* I have used log4j for logging and presenting the results.

* I used the Granule Json library, this provides a decent 'pretty print' toString() method.

* Artefacts include : 
	1. This readme.	
	2. An executable jar Scrape.jar which can be run from the command line using
	 `[C:\workspace\sainsburys>]java -jar Scrape.jar`
	3. site.html - the page source used for unittests.
	4. The hybristest Eclipse project. This can be unzipped and imported into
	 Eclipse as a project, use hybristest as the root directory. 
	

