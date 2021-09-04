#!/usr/bin/env bash
#
# ./gradlew clean build
# docker-compose build
# docker-compose up -d
#
# Sample usage:
#
#   HOST=localhost PORT=7000 ./test-em-all.bash
#
: ${HOST=localhost}
: ${PORT=8443}
: ${MOV_ID_REVS_TRI_CRA=2}
: ${MOV_ID_NOT_FOUND=14}
: ${MOV_ID_NO_TRI_NO_CRA=114}
: ${MOV_ID_NO_REVS_NO_CRA=214}

function assertCurl() {

    local expectedHttpCode=$1
    local curlCmd="$2 -w \"%{http_code}\""
    local result=$(eval $curlCmd)
    local httpCode="${result:(-3)}"
    RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

	if [ "$httpCode" = "$expectedHttpCode" ]
    then
        if [ "$httpCode" = "200" ]
        then
            echo "Test OK (HTTP Code: $httpCode)"
        else
            echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
        fi
        return 0
    else
        echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
        echo  "- Failing command: $curlCmd"
        echo  "- Response Body: $RESPONSE"
        return 1
    fi
}

function assertEqual() {

    local expected=$1
    local actual=$2

	if [ "$actual" = "$expected" ]
    then
        echo "Test OK (actual value: $actual)"
        return 0
    else
        echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
        return 1
    fi
}


function testUrl() {
    url=$@
	if $url -ks -f
    then
          return 0
    else
          return 1
    fi;
}

function waitForService() {
    url=$@
    echo -n "Wait for: $url... "
    n=0
    until testUrl $url
    do
        n=$((n + 1))
        if [[ $n == 100 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 3
            echo -n ", retry #$n "
        fi
    done
    echo "DONE, continues..."
}

function testCompositeCreated() {

    # Expect that the Movie Composite for movieId $MOV_ID_REVS_TRI_CRA has been created with three reviews, three trivia and three crazy credits
    if ! assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_TRI_CRA -s"
    then
        echo -n "FAIL"
        return 1
    fi

    set +e
    assertEqual "$MOV_ID_REVS_TRI_CRA" $(echo $RESPONSE | jq .movieId)
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".trivia | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
    if [ "$?" -eq "1" ] ; then return 1; fi
	
	assertEqual 3 $(echo $RESPONSE | jq ".crazyCredits | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    set -e
}

function waitForMessageProcessing() {
    echo "Wait for messages to be processed... "

    # Give background processing some time to complete...
    sleep 5

    n=0
    until testCompositeCreated
    do
        n=$((n + 1))
        if [[ $n == 40 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 3
            echo -n ", retry #$n "
        fi
    done
    echo "All messages are now processed!"
}

function recreateComposite() {
    local movieId=$1
    local composite=$2

    assertCurl 200 "curl $AUTH -X DELETE -k https://$HOST:$PORT/movie-composite/${movieId} -s"
    curl -X POST -k https://$HOST:$PORT/movie-composite -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" --data "$composite"
}

function setupTestdata() {
 body="{\"movieId\":$MOV_ID_NO_TRI_NO_CRA"
    body+=\
',"title":"Movie 114","releaseDate":"2021-08-12","country":"Country 114","budget":0,"gross":0,"runtime":0, "reviews":[
	{"reviewId":1,"publishDate":"2021-08-12","title":"title 1","content":"content 1","rating":0},
    {"reviewId":2,"publishDate":"2021-08-12","title":"title 2","content":"content 2","rating":0},
    {"reviewId":3,"publishDate":"2021-08-12","title":"title 3","content":"content 3","rating":0}
]}'
    recreateComposite "$MOV_ID_NO_TRI_NO_CRA" "$body"

body="{\"movieId\":$MOV_ID_NO_REVS_NO_CRA"
    body+=\
',"title":"Movie 214","releaseDate":"2021-08-12","country":"Country 214","budget":0,"gross":0,"runtime":0, "trivia":[
		{"triviaId":1,"publishDate":"2021-08-12","content":"content 1","spoiler":false},
        {"triviaId":2,"publishDate":"2021-08-12","content":"content 2","spoiler":false},
        {"triviaId":3,"publishDate":"2021-08-12","content":"content 3","spoiler":false}
	]
}'
    recreateComposite "$MOV_ID_NO_REVS_NO_CRA" "$body"
	
body="{\"movieId\":$MOV_ID_REVS_TRI_CRA"
    body+=\
',"title":"Movie 214","releaseDate":"2021-08-12","country":"Country 214","budget":0,"gross":0,"runtime":0, "trivia":[
		{"triviaId":1,"publishDate":"2021-08-12","content":"content 1","spoiler":false},
        {"triviaId":2,"publishDate":"2021-08-12","content":"content 2","spoiler":false},
        {"triviaId":3,"publishDate":"2021-08-12","content":"content 3","spoiler":false}
	], "reviews":[
        {"reviewId":1,"publishDate":"2021-08-12","title":"title 1","content":"content 1","rating":0},
        {"reviewId":2,"publishDate":"2021-08-12","title":"title 2","content":"content 2","rating":0},
        {"reviewId":3,"publishDate":"2021-08-12","title":"title 3","content":"content 3","rating":0}
    ], "crazyCredits":[
        {"crazyCreditId":1,"content":"content 1","spoiler":false},
        {"crazyCreditId":2,"content":"content 2","spoiler":false},
        {"crazyCreditId":3,"content":"content 3","spoiler":false}
    ]}'
    recreateComposite "$MOV_ID_REVS_TRI_CRA" "$body"

}

set -e

echo "Start Tests:" `date`

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
    echo "Restarting the test environment..."
    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
    echo "$ docker-compose up -d"
    docker-compose up -d
fi

waitForService curl -k https://$HOST:$PORT/actuator/health

ACCESS_TOKEN=$(curl -k https://writer:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
AUTH="-H \"Authorization: Bearer $ACCESS_TOKEN\""

setupTestdata

waitForMessageProcessing

# Verify that a normal request works, expect three trivia, three reviews and three crazy credits
assertCurl 200 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_TRI_CRA $AUTH -s"
assertEqual "$MOV_ID_REVS_TRI_CRA" $(echo $RESPONSE | jq .movieId)
assertEqual 3 $(echo $RESPONSE | jq ".trivia | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 3 $(echo $RESPONSE | jq ".crazyCredits | length")

# Verify that a 404 (Not Found) error is returned for a non existing movieId ($MOV_ID_NOT_FOUND)
assertCurl 404 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_NOT_FOUND $AUTH -s"

# Verify that no trivia and no crazy credits are returned for movieId $MOV_ID_NO_TRI_NO_CRA
assertCurl 200 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_NO_TRI_NO_CRA $AUTH -s"
assertEqual "$MOV_ID_NO_TRI_NO_CRA" $(echo $RESPONSE | jq .movieId)
assertEqual 0 $(echo $RESPONSE | jq ".trivia | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 0 $(echo $RESPONSE | jq ".crazyCredits | length")

# Verify that no reviews and no crazy credits are returned for movieId $MOV_ID_NO_REVS_NO_CRA
assertCurl 200 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_NO_REVS_NO_CRA $AUTH -s"
assertEqual "$MOV_ID_NO_REVS_NO_CRA" $(echo $RESPONSE | jq .movieId)
assertEqual 3 $(echo $RESPONSE | jq ".trivia | length")
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 0 $(echo $RESPONSE | jq ".crazyCredits | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a movieId that is out of range (-1)
assertCurl 422 "curl -k https://$HOST:$PORT/movie-composite/-1 $AUTH -s"
assertEqual "\"Invalid movieId: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a movieId that is not a number, i.e. invalid format
assertCurl 400 "curl -k https://$HOST:$PORT/movie-composite/invalidMovieId $AUTH -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

# Verify that a request without access token fails on 401, Unauthorized
assertCurl 401 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_TRI_CRA -s"

# Verify that the reader - client with only read scope can call the read API but not delete API.
READER_ACCESS_TOKEN=$(curl -k https://reader:secret@$HOST:$PORT/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .access_token -r)
READER_AUTH="-H \"Authorization: Bearer $READER_ACCESS_TOKEN\""

assertCurl 200 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_TRI_CRA $READER_AUTH -s"
assertCurl 403 "curl -k https://$HOST:$PORT/movie-composite/$MOV_ID_REVS_TRI_CRA $READER_AUTH -X DELETE -s"

echo "End, all tests OK:" `date`

if [[ $@ == *"stop"* ]]
then
    echo "Stopping the test environment..."
    echo "$ docker-compose down --remove-orphans"
    docker-compose down --remove-orphans
fi