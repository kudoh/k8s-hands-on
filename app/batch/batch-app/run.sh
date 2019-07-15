#!/bin/bash
set -eu

export INPUT_FILE_NAME=github-events-$(date +%s%3N).csv
# call github events
for i in {1..10}; do 
    echo "calling github api... page:${i}"
    curl -sSL -u $GITHUB_API_USER:$GITHUB_API_PASSWORD https://api.github.com/events?page=${i} | \
    jq -r '.[] | [.id,.type,.created_at,.repo.name,.repo.url,.actor.login, if (.org.login) then .org.login else "" end] | @csv' >> orig-${INPUT_FILE_NAME};
done

sort -t, -k1 -u orig-${INPUT_FILE_NAME} > ${INPUT_FILE_NAME}
rm orig-${INPUT_FILE_NAME}

# copy file into mounted NFS volume
cp -p ${INPUT_FILE_NAME} ${FILE_MOUNTPATH}/

echo "running spring batch application with ${INPUT_FILE_NAME}..."

# run spring-batch app
java -Xmx1024M -Xms1024M -jar /usr/local/app/batch-app.jar file.name=${INPUT_FILE_NAME}
