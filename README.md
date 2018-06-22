# aismessages-aws-lambda

## Intro
This repository contains the Java code to run an AWS Lambda function, which can parse NMEA armoured AIS messages.

The input to the function is NMEA strings posted in JSON format via HTTP - and the response is a JSON structure with the decoded AIS messages.

The AWS Lambda function is based entirely on [AISmessages](https://github.com/tbsalling/aismessages) - a Java-based light-weight, zero-dependency, and ultra-efficient message decoder for maritime navigation and safety messages compliant with ITU 1371 (NMEA armoured AIS messages)

For the moment the AWS Lambda function is experimental, and may be stopped, unstable or changed without notice. 
It is and only to be used for experimental and non-commcercial purposes.

## Live demo
The lambda is alive - try it for yourself: POST this this HTTP body

``` json
[
	"!AIVDM,1,1,,A,18UG;P0012G?Uq4EdHa=c;7@051@,0*53",
	"!AIVDM,2,1,0,B,539S:k40000000c3G04PPh63<00000000080000o1PVG2uGD:00000000000,0*34",
	"!AIVDM,2,2,0,B,00000000000,2*27"
]
```

with this header:

``` 
Content-Type:application/json
``` 

to https://bck16nvoa7.execute-api.eu-central-1.amazonaws.com/Production/aisdecoder.

Then you will get a response like this:

```[
    {
        "nmeaMessages": [
            {
                "rawMessage": "!AIVDM,1,1,,A,18UG;P0012G?Uq4EdHa=c;7@051@,0*53",
                "valid": true,
                "messageType": "AIVDM",
                "encodedPayload": "18UG;P0012G?Uq4EdHa=c;7@051@",
                "fillBits": 0,
                "checksum": 83,
                "fragmentNumber": 1,
                "numberOfFragments": 1,
                "radioChannelCode": "A"
            }
        ],
        "metadata": {
            "source": "SRC",
            "received": {
                "nano": 448000000,
                "epochSecond": 1529699908
            },
            "category": "AIS",
            "decoderVersion": "2.2.2"
        },
        "repeatIndicator": 0,
        "sourceMmsi": {
            "MMSI": 576048000
        },
        "navigationStatus": "UnderwayUsingEngine",
        "rateOfTurn": 0,
        "speedOverGround": 6.6,
        "positionAccuracy": false,
        "latitude": 37.912167,
        "longitude": -122.42299,
        "courseOverGround": 350,
        "trueHeading": 355,
        "second": 40,
        "specialManeuverIndicator": "NotAvailable",
        "raimFlag": false,
        "communicationState": {
            "syncState": "UTCDirect",
            "slotTimeout": 1,
            "utcHour": 8,
            "utcMinute": 20
        },
        "messageType": "PositionReportClassAScheduled",
        "transponderClass": "A",
        "valid": true
    },
    {
        "nmeaMessages": [
            {
                "rawMessage": "!AIVDM,2,1,0,B,539S:k40000000c3G04PPh63<00000000080000o1PVG2uGD:00000000000,0*34",
                "valid": true,
                "messageType": "AIVDM",
                "encodedPayload": "539S:k40000000c3G04PPh63<00000000080000o1PVG2uGD:00000000000",
                "fillBits": 0,
                "checksum": 52,
                "fragmentNumber": 1,
                "sequenceNumber": 0,
                "numberOfFragments": 2,
                "radioChannelCode": "B"
            },
            {
                "rawMessage": "!AIVDM,2,2,0,B,00000000000,2*27",
                "valid": true,
                "messageType": "AIVDM",
                "encodedPayload": "00000000000",
                "fillBits": 2,
                "checksum": 39,
                "fragmentNumber": 2,
                "sequenceNumber": 0,
                "numberOfFragments": 2,
                "radioChannelCode": "B"
            }
        ],
        "metadata": {
            "source": "SRC",
            "received": {
                "nano": 471000000,
                "epochSecond": 1529699908
            },
            "category": "AIS",
            "decoderVersion": "2.2.2"
        },
        "repeatIndicator": 0,
        "sourceMmsi": {
            "MMSI": 211339980
        },
        "imo": {
            "IMO": 0
        },
        "callsign": "J050A",
        "shipName": "HHLA 3         B",
        "shipType": "LawEnforcement",
        "toBow": 12,
        "toStern": 38,
        "toStarboard": 2,
        "toPort": 23,
        "eta": "14-05 20:10",
        "draught": 0,
        "destination": "",
        "dataTerminalReady": false,
        "messageType": "ShipAndVoyageRelatedData",
        "transponderClass": "A",
        "valid": true
    }
]
```

## TODO

Remaining work
- externalise AWS Lambda and API Gateway settings
- document, test and refine further
