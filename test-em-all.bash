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
: ${PORT=8080}

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
  else
      echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
      echo  "- Failing command: $curlCmd"
      echo  "- Response Body: $RESPONSE"
      exit 1
  fi
}

function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}


function testUrl() {
    url=$@
    if curl $url -ks -f
    then
          echo "Ok"
          return 0
    else
          echo -n "not yet"
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
            sleep 6
            echo -n ", retry #$n "
        fi
    done
}

function recreateComposite() {
    local movieId=$1
    local composite=$2

    assertCurl 200 "curl -X DELETE http://$HOST:$PORT/movie-composite/${movieId} -s"
    curl -X POST http://$HOST:$PORT/movie-composite -H "Content-Type: application/json" --data "$composite"
}

function setupTestdata() {

    body=\
'{"movieId":1,"title":"Movie 1","releaseDate":"2021-08-12","country":"Country 1","budget":0,"gross":0,"runtime":0, "trivia":[
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
    recreateComposite 1 "$body"

    body=\
'{"movieId":113,"title":"Movie 113","releaseDate":"2021-08-12","country":"Country 113","budget":0,"gross":0,"runtime":0, "reviews":[
	{"reviewId":1,"publishDate":"2021-08-12","title":"title 1","content":"content 1","rating":0},
    {"reviewId":2,"publishDate":"2021-08-12","title":"title 2","content":"content 2","rating":0},
    {"reviewId":3,"publishDate":"2021-08-12","title":"title 3","content":"content 3","rating":0}
]}'
    recreateComposite 113 "$body"

    body=\
'{"movieId":213,"title":"Movie 213","releaseDate":"2021-08-12","country":"Country 213","budget":0,"gross":0,"runtime":0, "trivia":[
		{"triviaId":1,"publishDate":"2021-08-12","content":"content 1","spoiler":false},
        {"triviaId":2,"publishDate":"2021-08-12","content":"content 2","spoiler":false},
        {"triviaId":3,"publishDate":"2021-08-12","content":"content 3","spoiler":false}
	], "crazyCredits":[
        {"crazyCreditId":1,"content":"content 1","spoiler":false},
        {"crazyCreditId":2,"content":"content 2","spoiler":false},
        {"crazyCreditId":3,"content":"content 3","spoiler":false}
	]
}'
    recreateComposite 213 "$body"

}

set -e

echo "Start:" `date`

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
    echo "Restarting the test environment..."
    echo "$ docker-compose down"
    docker-compose down
    echo "$ docker-compose up -d"
    docker-compose up -d
fi

waitForService curl -X DELETE http://$HOST:$PORT/movie-composite/13

setupTestdata

# Verify that a normal request works, expect three trivia, three reviews and three crazy credits
assertCurl 200 "curl http://$HOST:$PORT/movie-composite/1 -s"
assertEqual 1 $(echo $RESPONSE | jq .movieId)
assertEqual 3 $(echo $RESPONSE | jq ".trivia | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 3 $(echo $RESPONSE | jq ".crazyCredits | length")

# Verify that a 404 (Not Found) error is returned for a non existing movieId (13)
assertCurl 404 "curl http://$HOST:$PORT/movie-composite/13 -s"

# Verify that no trivia are returned for movieId 113
assertCurl 200 "curl http://$HOST:$PORT/movie-composite/113 -s"
assertEqual 113 $(echo $RESPONSE | jq .movieId)
assertEqual 0 $(echo $RESPONSE | jq ".trivia | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 3 $(echo $RESPONSE | jq ".crazyCredits | length")

# Verify that no reviews and no crazy credits are returned for movieId 213
assertCurl 200 "curl http://$HOST:$PORT/movie-composite/213 -s"
assertEqual 213 $(echo $RESPONSE | jq .movieId)
assertEqual 3 $(echo $RESPONSE | jq ".trivia | length")
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")
assertEqual 0 $(echo $RESPONSE | jq ".crazyCredits | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a movieId that is out of range (-1)
assertCurl 422 "curl http://$HOST:$PORT/movie-composite/-1 -s"
assertEqual "\"Invalid movieId: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a movieId that is not a number, i.e. invalid format
assertCurl 400 "curl http://$HOST:$PORT/movie-composite/invalidMovieId -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

if [[ $@ == *"stop"* ]]
then
    echo "We are done, stopping the test environment..."
    echo "$ docker-compose down"
    docker-compose down
fi

echo "End:" `date`