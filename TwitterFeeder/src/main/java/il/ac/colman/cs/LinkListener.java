package il.ac.colman.cs;

import util.AmazonServices;
import util.DataStorage;
import util.LinkExtractor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

public class LinkListener {
	public static void main(String[] args) throws SQLException, InterruptedException {
		// Connect to the database
		DataStorage dataStorage = new DataStorage();

		// Initiate our link extractor
		LinkExtractor linkExtractor = new LinkExtractor();

		// Listen to SQS for arriving links
		// Configure our client 
		AmazonSQS sqs = AmazonServices.getSqs();

		// Receive a message from the SQS
		while (true) {
			ReceiveMessageResult result = sqs
					.receiveMessage(System.getProperty("config.sqs.url"));
			List<Message> messages = result.getMessages();
			if (messages.size() == 0) {
				Thread.sleep(5000);
			} else {
 				for (Message message : messages) {
					// Do something with message
 					String url = message.getBody();
 					String track = System.getProperty("config.twitter.track");
 					try {
						dataStorage.addLink(linkExtractor.extractContent(url), track);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
 					
				}
			}
		}
	}
}
