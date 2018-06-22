package dk.tbsalling.ais.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import dk.tbsalling.aismessages.AISInputStreamReader;
import dk.tbsalling.aismessages.ais.messages.AISMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class Decoder implements RequestHandler<List<String>, List<AISMessage>>{

    @Override
    public List<AISMessage> handleRequest(List<String> nmeaCollection, Context context) {

       if (nmeaCollection == null)
            throw new RuntimeException("nmea is null");

        if (nmeaCollection.isEmpty())
            throw new RuntimeException("nmea is empty");

        LambdaLogger logger = context.getLogger();
        logger.log("AisDecoder has been called: awsRequestId: " + context.getAwsRequestId());
      //  logger.log("Received " + nmeaCollection.getNmea().size() + " NMEA strings");

        StringJoiner stringJoiner = new StringJoiner("\n");
        for (String s : nmeaCollection) {
             stringJoiner.add(s);
             logger.log("Added " + s + " to bundle.");
        }
        String nmeaBundle = stringJoiner.toString();
        logger.log("nmeaBundle: " + nmeaBundle);

        InputStream inputStream = new ByteArrayInputStream(nmeaBundle.getBytes(StandardCharsets.UTF_8));

        List<AISMessage> aisMessageList = new LinkedList<>();

        AISInputStreamReader aisInputStreamReader = new AISInputStreamReader(inputStream, aisMessage -> aisMessageList.add(aisMessage));
        try {
            aisInputStreamReader.run();
        } catch (IOException e) {
            logger.log(e.getMessage());
        }

        logger.log("Lambda done.");

        return aisMessageList;
    }

}
