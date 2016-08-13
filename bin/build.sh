#!/bin/bash

app_path=$(cd `dirname $0`; pwd)
app_path=$(cd `dirname $app_path`; pwd)
app_target=$app_path/target

jar_name=(`sed -n 's/.*>\(.*\)<\/artifactId>/\1/p' ${app_path}/pom.xml`)
jar_version=(`sed -n 's/.*>\(.*\)<\/version>/\1/p' ${app_path}/pom.xml`)

build_xml=(`sed -n 's/.*>\(.*\)<\/descriptor>/\1/p' ${app_path}/pom.xml`)
build_name=(`sed -n 's/.*>\(.*\)<\/id>/\1/p' ${app_path}/$build_xml`)
build_fmt=(`sed -n 's/.*>\(.*\)<\/format>/\1/p' ${app_path}/$build_xml`)
zip_name=$jar_name-$jar_version.$build_fmt

#echo $zip_name

cd $app_path && mvn clean install
echo unpackage $zip_name
cd $app_target && tar -zxvf $app_target/$zip_name
chmod -R 777  $app_target/$jar_name-$jar_version/bin
