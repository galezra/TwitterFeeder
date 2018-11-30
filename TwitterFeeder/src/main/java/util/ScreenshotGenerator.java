package util;

import java.io.File;
import java.io.IOException;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.s3.AmazonS3;

public class ScreenshotGenerator {
	public String takeScreenshot(String url) {
		// Configure our client
				AmazonS3 sqs = AmazonServices.getS3();
				AmazonCloudWatch cw = AmazonServices.getCw();
				//Configuration root folder and nightmare screenshot
				long startTime = System.nanoTime();
				String root = System.getProperty("user.dir");
				String screenshotJS = "node " + root + "\\src\\main\\resources\\screenshot.js";

				try {
					File tempFile = File.createTempFile(root, ".png");

					Process p = Runtime.getRuntime().exec(screenshotJS + " " + url + " " + tempFile.toString());
					p.waitFor();
					 int exitCode = p.waitFor();
				        if (exitCode != 0) {
				            System.out.println("Error Execute when exitCode=1");
				        }else{
				            System.out.println("Fine Execute when exitCode=0");
				        }

					// Upload a file
					sqs.putObject("screenshotsrg", tempFile.getName(), tempFile);
					System.out.println(tempFile);
					tempFile.delete();
					long endTime = (System.nanoTime() - startTime) / 1000000;
					AmazonServices.CloudWatchTraffic(cw, endTime, "FetchScreenShoot", "FetchTime");
					
					// Get the object URL
					String urlS3 = "https://s3-eu-west-1.amazonaws.com/screenshotsrg/"+ tempFile.getName();
					return urlS3;
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
	}
}
