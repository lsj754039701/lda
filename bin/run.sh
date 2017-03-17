#!/bin/bssh
cd $(dirname $0)

function run() {
    cd ..
    rm -fr keyword 
    rm -fr typeInfo

    typeInfo="file:///data/resys/lsj/work/lda/typeInfo"
    keyword="file:///data/resys/lsj/work/lda/keyword"
    /data/resys/var/spark-2.0.2-bin-hadoop2.6/bin/spark-submit --class "LDA" \
        --jars /data/resys/lsj/work/task2/mysql-connector-java-5.1.40-bin.jar \
        target/scala-2.11/*.jar \
        "$typeInfo" \
        "$keyword" \
        || return  1
}

$@
