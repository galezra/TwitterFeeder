package il.ac.colman.cs;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import util.AmazonServices;

import java.io.IOException;
import java.sql.SQLException;
import com.amazonaws.services.sqs.AmazonSQS;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;

public class TwitterFeeder {

	public static void main(String[] args) throws SQLException, IOException {
		
		// Create our twitter configuration
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(System.getProperty("config.twitter.consumer.key"))
				.setOAuthConsumerSecret(System.getProperty("config.twitter.consumer.secret"))
				.setOAuthAccessToken(System.getProperty("config.twitter.access.token"))
				.setOAuthAccessTokenSecret(System.getProperty("config.twitter.access.secret"));

		// Create our Twitter stream
		TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
		TwitterStream twitterStream = tf.getInstance();

		StatusListener statusListener = new StatusListener() {
			AmazonSQS sqs = AmazonServices.getSqs();
			AmazonCloudWatch cw = AmazonServices.getCw();

			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

			}

			public void onStatus(Status status) {
				if (status.getText().contains("http")) {
					URLEntity[] urls = status.getURLEntities();
					for (URLEntity urlEntity : urls) {
						String url = urlEntity.getURL();
						sqs.sendMessage(System.getProperty("config.sqs.url"), url);
						AmazonServices.CloudWatchTraffic(cw, 1.00, "TwitterFeeder", System.getProperty("config.twitter.track"));
					}
				}
			}

			public void onStallWarning(StallWarning warning) {

			}

			public void onScrubGeo(long userId, long upToStatusId) {

			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

			}
		};

		twitterStream.addListener(statusListener);
		twitterStream.sample();
		twitterStream.filter(System.getProperty("config.twitter.track"));

	}

}
