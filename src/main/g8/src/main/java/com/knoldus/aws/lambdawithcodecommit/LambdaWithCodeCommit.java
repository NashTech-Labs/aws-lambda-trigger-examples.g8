package com.knoldus.aws.lambdawithcodecommit;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.CodeCommitEvent;
import com.knoldus.aws.utils.confighelper.ConfigReader;
import com.knoldus.aws.utils.loggerhelper.LoggerFactory;
import com.knoldus.aws.utils.loggerhelper.LoggerService;
import com.knoldus.aws.utils.mailhelper.MailerHelper;

import java.util.List;
import java.util.stream.Collectors;

public class LambdaWithCodeCommit implements RequestHandler<CodeCommitEvent, Void> {

    private static final LoggerService LOGGER = LoggerFactory.getLogService(LambdaWithCodeCommit.class.getName());
    private static final ConfigReader configReader = ConfigReader.getConfigReader("mail");

	/**
	 * Handler function for handling CodeCommit events generated by repository linked to this lambda function.
	 * This lambda function needs CloudWatch permissions as it takes the records through
	 * <code>{@link CodeCommitEvent}</code> triggering this lambda, constructs a {@code String}
	 * and logs it on CloudWatch.
	 *
	 * @param codeCommitEvent Event that triggered lambda function.
	 * @param context Used to access lambda environment information.
	 * @return Void
	 */
	@Override
	public Void handleRequest(CodeCommitEvent codeCommitEvent, Context context) {
		List<CodeCommitEvent.Record> records = codeCommitEvent.getRecords();
		String finalRecord = records.stream().map(CodeCommitEvent.Record::toString).collect(Collectors.joining("\n"));

		String to = configReader.getProperty("to");
        String subject = "Lambda triggered due to code commit";

        String body = String.join(
                System.getProperty("line.separator"),
                "<h2>Your lambda was triggered due to Code Commit</h2>",
                "<p>The record for code commit is -",
                "<p>" + finalRecord + "</p>"
        );

		try {
            MailerHelper.sendMail(to, subject, body);
            LOGGER.info("Commit --> " + finalRecord);
        } catch (Exception ex) {
            LOGGER.info(ex.getMessage());
        }
		return null;
	}
}