# DeukNet-BE
## 아키텍쳐
<img width="923" height="585" alt="image" src="https://github.com/user-attachments/assets/9effc7b5-3526-44b1-9e3b-bf689432be7d" />
<img width="712" height="191" alt="image" src="https://github.com/user-attachments/assets/25ba08f2-e7ac-49dd-8e9e-7792bf5e482c" />

### Aggregate 설계 원칙
1. **작은 Aggregate**: 각 Aggregate는 단일 책임
2. **ID로 참조**: Aggregate 간에는 Entity 객체가 아닌 ID로 참조
3. **일관성 경계**: 각 Aggregate는 독립적인 트랜잭션 경계
4. **명확한 루트**: 각 Aggregate는 명확한 Root Entity 보유



## CQRS 적용 방법
<img width="747" height="557" alt="image" src="https://github.com/user-attachments/assets/1f358070-c91f-495b-845c-bf9bafe823d2" />
