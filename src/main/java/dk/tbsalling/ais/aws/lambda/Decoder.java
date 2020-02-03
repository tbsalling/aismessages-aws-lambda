package dk.tbsalling.ais.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import dk.tbsalling.aismessages.AISInputStreamReader;
import dk.tbsalling.aismessages.ais.messages.AISMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Decoder implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            LambdaLogger logger = context.getLogger();
            logger.log("Received event: " + event.toString());

            var contentType = getContentType(event);
            var body = event.getBody();

            List<String> nmeaSentences = parseBody(body, contentType);
            logger.log("Detected " + nmeaSentences.size() + " NMEA sentences.");

            List<AISMessage> aisMessageList = convertNmeaStringsToAisMessages(nmeaSentences);
            logger.log("Produced " + aisMessageList.size() + " AIS messages.");

            return createResponseEvent(aisMessageList);
        } catch (RuntimeException e) {
            return createErrorResponseEvent(e);
        }
    }

    private APIGatewayProxyResponseEvent createErrorResponseEvent(RuntimeException e) {
        var responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setIsBase64Encoded(false);
        responseEvent.setHeaders(Map.of("Content-Type", "application/json"));
        responseEvent.setStatusCode(e instanceof ParseError ? ((ParseError) e).statusCode : 500);

        Map<String, String> error;
        if (e.getCause() == null) {
            error = Map.of(
                    "message", e.getMessage(),
                    "statusCode", responseEvent.getStatusCode().toString()
            );
        } else {
            error = Map.of(
                    "message", e.getMessage(),
                    "statusCode", responseEvent.getStatusCode().toString(),
                    "cause", e.getCause().getClass().getName(),
                    "causeMessage", e.getCause().getMessage()
            );
        }
        responseEvent.setBody(JSONObject.toJSONString(error));

        return responseEvent;
    }

    private APIGatewayProxyResponseEvent createResponseEvent(List<AISMessage> aisMessageList) {
        var responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setStatusCode(200);
        responseEvent.setIsBase64Encoded(false);
        responseEvent.setHeaders(Map.of("Content-Type", "application/json"));
        responseEvent.setBody(JSONArray.toJSONString(aisMessageList));
        return responseEvent;
    }

    private List<AISMessage> convertNmeaStringsToAisMessages(List<String> nmeaSentences) {
        List<AISMessage> aisMessageList = new LinkedList<>();
        AISInputStreamReader aisInputStreamReader = new AISInputStreamReader(nmeaSentences, aisMessage -> aisMessageList.add(aisMessage));
        aisInputStreamReader.run();
        return aisMessageList;
    }

    private List<String> parseBody(String body, String contentType) {
        if (contentType.equalsIgnoreCase("application/json")) {
            try {
                var parser = new JSONParser();
                JSONArray nmeaSentenceJsonArray = (JSONArray) parser.parse(body);
                return new LinkedList<>(nmeaSentenceJsonArray);
            } catch (ParseException e) {
                throw new ParseError(e.getMessage(), 500, e);
            }
        } else if (contentType.equalsIgnoreCase("text/plain")) {
            var lines = body.split("\n");
            return List.of(lines);
        } else {
            throw new ParseError("Unsupported 'Content-Type': '" + contentType + "'", 415);
        }
    }

    private String getContentType(APIGatewayProxyRequestEvent event) {
        var contentType = event.getHeaders().getOrDefault("Content-Type", "application/json");
        contentType = contentType.strip();
        return contentType;
    }

    private class ParseError extends RuntimeException {
        public ParseError(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public ParseError(String message, int statusCode, Throwable cause) {
            super(message, cause);
            this.statusCode = statusCode;
        }

        private final int statusCode;
    }
}
