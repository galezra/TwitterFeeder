
package util;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.PutMetricDataResult;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

public class AmazonServices {
	private static AWSCredentialsProvider awsCredentialsProvider;

	public static AmazonSQS getSqs() {
		awsCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(
				System.getProperty("config.sqs.accesskey"), System.getProperty("config.sqs.secretkey")));
		return  AmazonSQSClientBuilder.standard().withRegion("eu-west-1").withCredentials(awsCredentialsProvider)
				.build();
	}

	public static AmazonS3 getS3() {
		awsCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(
				System.getProperty("config.sqs.accesskey"), System.getProperty("config.sqs.secretkey")));
		return AmazonS3ClientBuilder.standard().withRegion("eu-west-1").withCredentials(awsCredentialsProvider)
				.build();
	}

	public static AmazonCloudWatch getCw() {
		awsCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(
				System.getProperty("config.sqs.accesskey"), System.getProperty("config.sqs.secretkey")));
		return AmazonCloudWatchClientBuilder.standard().withRegion("eu-west-1")
				.withCredentials(awsCredentialsProvider).build();
	}

	public static void CloudWatchTraffic(AmazonCloudWatch cw, double time, String dimensionName, String matricName) {
		Dimension dimension = new Dimension().withName(dimensionName).withValue("URLS");

		MetricDatum datum = new MetricDatum().withMetricName(matricName).withUnit(StandardUnit.None).withValue(time)
				.withDimensions(dimension);

		PutMetricDataRequest request = new PutMetricDataRequest().withNamespace("Custom Traffic").withMetricData(datum);
		PutMetricDataResult response = cw.putMetricData(request);
	}
	


}
