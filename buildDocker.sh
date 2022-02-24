#!/bin/bash

tag=$1

serverIp=`cat server.txt`
echo "$serverIp"

docker build -f Dockerfile.order . -t wechat-shop-order
docker build -f Dockerfile.main . -t wechat-shop-main

docker tag wechat-shop-order $serverIp:5000/wechat-shop-order:$tag
docker tag wechat-shop-main $serverIp:5000/wechat-shop-main:$tag

docker push $serverIp:5000/wechat-shop-order:$tag
docker push $serverIp:5000/wechat-shop-main:$tag
