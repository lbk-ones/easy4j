#!/bin/sh
echo "Nacos auto config started"
accountConfig=$(cat ../config/template-account.properties)
storageConfig=$(cat ../config/template-storage.properties)
orderConfig=$(cat ../config/template-order.properties)
gatewayConfig=$(cat ../config/template-gateway.properties)
gatewayConfigYaml=$(cat ../config/gateway-common.yaml)
groupId="template-service"
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" -d "dataId=template-storage.properties&group=${groupId}&content=${storageConfig}&username=nacos&password=nacos&type=properties"
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" -d "dataId=template-account.properties&group=${groupId}&content=${accountConfig}&username=nacos&password=nacos&type=properties"
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" -d "dataId=template-order.properties&group=${groupId}&content=${orderConfig}&username=nacos&password=nacos&type=properties"
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" -d "dataId=template-gateway.properties&group=${groupId}&content=${gatewayConfig}&username=nacos&password=nacos&type=properties"
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" -d "dataId=gateway-common.yaml&group=${groupId}&content=${gatewayConfigYaml}&username=nacos&password=nacos&type=yaml"
echo "Nacos config pushed successfully finished"