package util;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;


import il.ac.colman.cs.ExtractedLink;

/**
 * Extract content from links
 */
public class LinkExtractor {
	private Document document;

	public ExtractedLink extractContent(String url) throws IOException {
		/*
		 * Use JSoup to extract the text, title and description from the URL.
		 * 
		 * Extract the page's content, without the HTML tags. Extract the title from
		 * title tag or meta tags, prefer the meta title tags. Extract the description
		 * the same as you would the title.
		 * 
		 * For title and description tags, if there are multiple (which is usually the
		 * case) take the first.
		 */	
		
	    AmazonCloudWatch cw = AmazonServices.getCw();
		long startTime = System.nanoTime();
		// Get url
		this.document = Jsoup.connect(url).get();
		
		// Get real url
		url = getMetaTag("title");
		if(url == null) {
			url = document.title();
		}
		
		this.document = Jsoup.connect(url).get();
		// Get content
		String content = getContent();
		
		// Get title
		String title = getMetaTag("title");
		if(title == null) {
			title = document.title();
		}
		
		
		// Get description
		String description = getMetaTag("description");
		
		// Get screenshotURL
		ScreenshotGenerator sg = new ScreenshotGenerator();
		long endTime = (System.nanoTime() - startTime) / 1000000;
		AmazonServices.CloudWatchTraffic(cw, endTime, "FetchUrlContent", "FetchTime");
		
		String screenshotURL = sg.takeScreenshot(url);
		ExtractedLink el = new ExtractedLink(url, content, title, description, screenshotURL);
		return el;
	}
	
	String getMetaTag(String attr) {
	    Elements elements = document.select("meta[name=" + attr + "]");
	    for (Element element : elements) {
	        final String s = element.attr("content");
	        if (s != null) return s;
	    }
	    elements = document.select("meta[property=" + attr + "]");
	    for (Element element : elements) {
	        final String s = element.attr("content");
	        if (s != null) return s;
	    }
	    elements = document.select("meta[name=twitter:" + attr + "]");
	    for (Element element : elements) {
	        final String s = element.attr("content");
	        if (s != null) return s;
	    }
		return null;
	}
	
	public String getContent() {
		String text = null;
		 Elements spanTags = document.getElementsByTag("body");
		    for (Element spanTag : spanTags) {
		         text = spanTag.wholeText();
		    }
	    			if(text == null || text.length() < 100) {
	    				return null;
	    			}
		    		String res = text.replaceAll("([^A-Za-z0-9/\\-?:\\(\\)\\.,’'\\+#=! \\” %& \\* <>; \\{@\\r\\n])", "")
		    		.replaceAll("\\n","")
		    		.replaceAll("\\r", "")
		    		.replaceAll("\\'", "")
		    		.replaceAll("\\’","");
		    		
		    		if(res.length() > 99) {
		    			res = res.substring(0, 99);
		    		}
		    
		return res;
	}
	
}
