#!/bin/bash

echo "======================================"
echo "CDC 테스트 명령어"
echo "======================================"
echo ""

echo "1. Port-forward 실행 (별도 터미널):"
echo "   kubectl port-forward svc/deuknet-app 8080:8080"
echo ""

echo "2. 게시글 생성:"
echo '   curl -X POST http://localhost:8080/api/posts \
     -H "Content-Type: application/json" \
     -d '"'"'{
       "title": "CDC 테스트 게시글",
       "content": "PostgreSQL → Debezium → Kafka 테스트",
       "categoryIds": ["123e4567-e89b-12d3-a456-426614174000"]
     }'"'"
echo ""
echo "   → 반환된 POST_ID 복사하기"
echo ""

echo "3. Elasticsearch에서 조회:"
echo '   ES_POD=$(kubectl get pod -l app=elasticsearch -o jsonpath='"'"'{.items[0].metadata.name}'"'"')'
echo '   kubectl exec $ES_POD -- curl -s "http://localhost:9200/posts-detail/_doc/POST_ID여기에넣기"'
echo ""

echo "4. PostgreSQL Outbox 확인:"
echo '   PG_POD=$(kubectl get pod -l app=postgres -o jsonpath='"'"'{.items[0].metadata.name}'"'"')'
echo '   kubectl exec $PG_POD -- psql -U deuknet_user -d deuknet -c "SELECT * FROM outbox_events ORDER BY occurred_on DESC LIMIT 5;"'
echo ""

echo "5. Kafka 토픽 확인:"
echo '   kubectl exec deployment/kafka -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list'
echo ""

echo "6. Kafka 메시지 확인 (PostDetail 토픽):"
echo '   kubectl exec -it deployment/kafka -- /opt/kafka/bin/kafka-console-consumer.sh \
     --bootstrap-server localhost:9092 \
     --topic outbox.events.PostDetail \
     --from-beginning'
echo ""
