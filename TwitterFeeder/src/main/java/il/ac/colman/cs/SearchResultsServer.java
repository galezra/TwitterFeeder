package il.ac.colman.cs;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.fasterxml.jackson.databind.ObjectMapper;

import util.AmazonServices;
import util.DataStorage;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


public class SearchResultsServer extends AbstractHandler {
	public static void main(String[] args) throws Exception {

		Server server = new Server(8080);

		server.setHandler(new SearchResultsServer());

		server.start();
		server.join();

	}

	private DataStorage storage;

	SearchResultsServer() throws SQLException {
		this.storage = new DataStorage();
	}
	


	public void handle(String s, Request request, HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws IOException, ServletException {
		// Set the content type to JSON
		httpServletResponse.setContentType("text/html;charset=UTF-8");
		httpServletResponse.setStatus(HttpServletResponse.SC_OK);

		// Build data from request
		AmazonCloudWatch cw = AmazonServices.getCw();
		long startTime = System.nanoTime();
		List<ExtractedLink> results = storage.search(httpServletRequest.getParameter("query"));
		long endTime = (System.nanoTime() - startTime) / 1000000;
		AmazonServices.CloudWatchTraffic(cw, endTime, "SearchApi","FetchTime");
		
		String html = HtmlBuilder(results);

		// Notify that this request was handled
		request.setHandled(true);

		// Convert data to JSON string and write to output
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(httpServletResponse.getWriter(), html);
	}

	public String HtmlBuilder(List<ExtractedLink> results) {
		if (results != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("<!DOCTYPE html> <html> <head>"
					+ " <style> div.gallery { margin: 5px; border: 1px solid #ccc; float: left; width: 600px; box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19); } div.gallery:hover { border: 1px solid #777; } div.gallery img { width: 100%; height: auto; } div.desc { padding: 5px; margin-left:15px } </style> "
					+ "</head> <body>");
			for (ExtractedLink extractedLink : results) {
				sb.append("<div class='gallery'><a><img src='" +extractedLink.getScreenshotURL()+"' width='600' height='400'></a>"
						+ "<div class='desc'><b>Link:</b>"+ extractedLink.getUrl()+"</div>"
						+ "<div class='desc'><b>Title:</b>"+ extractedLink.getTitle()+"</div>"
						+ "<div class='desc'><b>Description:</b>"+ extractedLink.getDescription()+"</div>"
						+ "<div class='desc'><b>Content:</b>"+ extractedLink.getContent()+"</div></div>");
			}
			sb.append("</body></html>");
			return sb.toString();
		}
		return null;
	}
	
}
