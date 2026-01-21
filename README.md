# DeukNet-BE 
주소: https://deuknet.dsmhs.kr/

## 개요
DeukNet-BE는 대덕소프트웨어마이스터고 학생들을 위한 능딸 커뮤니티 서비스로,
학생들이 보다 빠르고 효율적으로 소통하고 정보를 탐색할 수 있도록 설계된 플랫폼입니다.

이 프로젝트의 가장 큰 목표는 빠른 조회와 다양한 검색 경험을 제공하는 것입니다.
단순히 게시글을 등록하고 조회하는 수준을 넘어서,
사용자가 원하는 정보를 정확하고 즉시 찾을 수 있는 커뮤니티 환경을 지향합니다.
 
DeukNet-BE는 다양한 검색 지원 기능을 중심으로 발전하고 있습니다.
예를 들어 게시글 제목이나 내용뿐 아니라,
태그, 작성자, 게시 시점, 인기 여부 등 여러 기준으로 검색이 가능하도록 확장하고 있으며,
이와 함께 추천형 검색, 연관 게시글 탐색, 자동완성 기능 같은
사용자 친화적인 탐색 경험도 목표로 하고 있습니다.

또한 서비스의 기반 구조는
검색 속도와 확장성을 모두 고려한 형태로 설계되어 있어,
많은 사용자 요청이 동시에 발생하더라도
일관되고 빠른 응답을 유지할 수 있도록 최적화되어 있습니다.

## 아키텍쳐


<img width="691" height="428" alt="image" src="https://github.com/user-attachments/assets/c6a885b5-11ae-4ebf-8bf9-6b3da25361d5" />

### Domain Layer

> 시스템의 핵심 규칙과 비즈니스 의미를 표현하는 중심 계층입니다.

비즈니스 엔티티와 도메인 로직이 위치합니다.

외부 환경(DB, 메시지 브로커 등)에 전혀 의존하지 않으며,
순수한 객체 모델 로직으로 구성됩니다.

### Application Layer (Usecase / Port)

> 도메인 로직을 구동시키는 유스케이스(서비스) 계층입니다.

**in port** : presentation 계층으로부터 요청을 받는 인터페이스 (명령 단위, 유즈케이스)

**out port** : domain이 필요로 하는 외부 의존성을 추상화한 인터페이스 (Repository, MessagePublisher 등)

트랜잭션 단위로 비즈니스 로직 실행을 조정하며,
CQRS 구조에서 Command/Query의 분리 로직을 담당합니다.

Command flow에서는 PostgreSQL을 통해 상태 변경을 수행하고,
Query flow에서는 ElasticSearch를 통해 조회를 최적화합니다.

### Infrastructure Layer

> 실제 기술 스택과 외부 시스템 연동을 담당하는 계층입니다.

이 아키텍처에서는 두 가지 세부 영역으로 나뉩니다.

🔹 Data 영역

영속성 관련 어댑터(Repository) 가 위치합니다.

out / repository 포트를 구현하여 도메인 데이터를 저장하거나 조회합니다.

PostgreSQL은 Command 모델(정합성 유지용)로,
ElasticSearch는 Query 모델(조회 성능 최적화용)로 사용합니다.

🔹 External 영역

메시지 브로커(MQ), Redis, 외부 API 등
외부 시스템과의 상호작용을 담당합니다.

도메인 이벤트 발행, 캐싱, 비동기 통신 등을 처리합니다.

역시 out / port를 통해 추상화된 인터페이스를 구현합니다

<img width="712" height="191" alt="image" src="https://github.com/user-attachments/assets/25ba08f2-e7ac-49dd-8e9e-7792bf5e482c" />

> 의존성 방향은 항상 한쪽으로만 흐릅니다.
presentation → usecase → domain
(port는 domain의 일부 개념이지만, 실제 구현은 infrastructure에 존재)
application이 port를 참조 하는 게 아니라 정의하고,
실제 동작은 외부(infrastructure)가 그 포트를 구현합니다.

<img width="874" height="502" alt="image" src="https://github.com/user-attachments/assets/a2f3433c-5eac-40bb-beb9-c764bf981811" />

> 초기 Aggregate 설계

### Aggregate 설계 원칙
1. **작은 Aggregate**: 각 Aggregate는 단일 책임
2. **ID로 참조**: Aggregate 간에는 Entity 객체가 아닌 ID로 참조
3. **일관성 경계**: 각 Aggregate는 독립적인 트랜잭션 경계
4. **명확한 루트**: 각 Aggregate는 명확한 Root Entity 보유 



## CQRS 적용 방법
<img width="747" height="557" alt="image" src="https://github.com/user-attachments/assets/1f358070-c91f-495b-845c-bf9bafe823d2" />

### CQRS 분리의 핵심 이유

CQRS의 핵심은 명령(Command) 과 조회(Query) 를 분리함으로써,
서로 다른 요구사항(Consistency vs Performance)을 독립적으로 최적화하기 위함입니다.


| 구분        | Command Side                  | Query Side             |
| --------- | ----------------------------- | ---------------------- |
| **목적**    | 상태 변경 (write, update, delete) | 데이터 조회                 |
| **모델**    | 도메인 중심 (Aggregate 단위, 불변식 검증) | Projection 중심 (DTO 형태) |
| **DB 설계** | 정규화, 트랜잭션, 일관성 중시             | 비정규화, 조회 성능 중시         |
| **확장성**   | 쓰기 확장 (비즈니스 로직)               | 읽기 확장 (캐시, 검색엔진)       |
| **트랜잭션**  | ACID 보장                       | Eventually Consistent  |
