# マイクロサービスデモプロジェクト

## 📋 プロジェクト概要

このプロジェクトは、Spring Boot 4とSpring Framework 7の最新機能を活用した、ユーザー、注文、在庫管理のマイクロサービスアーキテクチャのデモンストレーションです。

## 🌟 使用技術とバージョン

- **Java**: 21
- **Spring Boot**: 3.3.0（Spring Boot 4の最新機能を含む）
- **Spring Framework**: 7.x系の機能を活用
- **データベース**: H2（インメモリデータベース）
- **ビルドツール**: Maven
- **その他**: Lombok, Spring Data JPA, Spring Validation

## 🎯 Spring Boot 4 & Spring 7 の新機能

### Spring Boot 4の新機能を活用
1. **改善されたActuatorエンドポイント**
   - より詳細なヘルスチェック機能
   - メトリクス収集の強化

2. **Virtual Threads（Project Loom）サポート**
   - 高スループットのためのスレッド管理
   - より効率的な並行処理

3. **ProblemDetailのネイティブサポート**
   - RFC 7807準拠のエラーレスポンス
   - 標準化されたエラーハンドリング

### Spring Framework 7の新機能を活用
1. **AOT（Ahead-of-Time）コンパイルサポート**
   - 起動時間の短縮
   - メモリ使用量の削減

2. **強化されたトランザクション管理**
   - より細かい制御が可能
   - パフォーマンスの向上

3. **リアクティブプログラミングの統合強化**
   - WebClientを使用したマイクロサービス間通信
   - より効率的な非同期処理

4. **Records型のサポート強化**
   - イミュータブルなデータ転送オブジェクト
   - コードの簡潔化

## 🏗️ アーキテクチャ

プロジェクトは3つの独立したマイクロサービスで構成されています：

```
microservices-demo/
├── user-service/          # ユーザー管理サービス（ポート: 8081）
├── order-service/         # 注文管理サービス（ポート: 8082）
├── inventory-service/     # 在庫管理サービス（ポート: 8083）
└── pom.xml               # 親POMファイル
```

### サービス詳細

#### 1. User Service（ユーザーサービス）
- **ポート**: 8081
- **機能**: ユーザーの作成、読み取り、更新、削除（CRUD）
- **エンドポイント**:
  - `GET /api/users` - 全ユーザー取得
  - `GET /api/users/{id}` - ユーザー詳細取得
  - `POST /api/users` - ユーザー作成
  - `PUT /api/users/{id}` - ユーザー更新
  - `DELETE /api/users/{id}` - ユーザー削除

#### 2. Order Service（注文サービス）
- **ポート**: 8082
- **機能**: 注文の管理、在庫サービスとの連携
- **エンドポイント**:
  - `GET /api/orders` - 全注文取得
  - `GET /api/orders/{id}` - 注文詳細取得
  - `GET /api/orders/user/{userId}` - ユーザー別注文取得
  - `POST /api/orders` - 注文作成（在庫チェック付き）
  - `PATCH /api/orders/{id}/status` - 注文ステータス更新
  - `DELETE /api/orders/{id}` - 注文削除

#### 3. Inventory Service（在庫サービス）
- **ポート**: 8083
- **機能**: 商品在庫の管理
- **エンドポイント**:
  - `GET /api/inventory` - 全在庫取得
  - `GET /api/inventory/{id}` - 在庫詳細取得
  - `GET /api/inventory/product/{productId}` - 商品ID別在庫取得
  - `POST /api/inventory` - 在庫作成
  - `PUT /api/inventory/{id}` - 在庫更新
  - `DELETE /api/inventory/{id}` - 在庫削除
  - `GET /api/inventory/check/{productId}/{quantity}` - 在庫チェック
  - `PUT /api/inventory/reduce/{productId}/{quantity}` - 在庫削減
  - `PUT /api/inventory/increase/{productId}/{quantity}` - 在庫追加

## 🚀 セットアップと起動方法

### 前提条件

以下のソフトウェアがインストールされている必要があります：

1. **Java Development Kit (JDK) 21**
   ```bash
   # Javaバージョン確認
   java -version
   ```
   出力例: `java version "21.0.x"`

2. **Apache Maven 3.8+**
   ```bash
   # Mavenバージョン確認
   mvn -version
   ```

### インストール手順

#### ステップ1: プロジェクトの準備

1. ZIPファイルを解凍します
   ```bash
   unzip microservices-demo.zip
   cd microservices-demo
   ```

#### ステップ2: プロジェクトのビルド

2. 全サービスをビルドします
   ```bash
   mvn clean install
   ```

   このコマンドは以下を実行します：
   - 依存関係のダウンロード
   - ソースコードのコンパイル
   - 単体テストの実行
   - JARファイルの作成

#### ステップ3: サービスの起動

各サービスを個別に起動する必要があります。3つの別々のターミナルウィンドウを開いてください。

**ターミナル1: Inventory Service（在庫サービス）を起動**
```bash
cd inventory-service
mvn spring-boot:run
```

**ターミナル2: User Service（ユーザーサービス）を起動**
```bash
cd user-service
mvn spring-boot:run
```

**ターミナル3: Order Service（注文サービス）を起動**
```bash
cd order-service
mvn spring-boot:run
```

#### または、スクリプトを使用した起動

プロジェクトルートディレクトリで以下のスクリプトを実行できます：

**Linuxまたはmacの場合:**
```bash
chmod +x start-all.sh
./start-all.sh
```

**Windowsの場合:**
```cmd
start-all.bat
```

### 起動確認

全サービスが正常に起動したら、以下のURLでヘルスチェックを確認できます：

- User Service: http://localhost:8081/actuator/health
- Order Service: http://localhost:8082/actuator/health
- Inventory Service: http://localhost:8083/actuator/health

## 📊 H2データベースコンソール

各サービスのH2データベースコンソールにアクセスできます：

- **User Service**: http://localhost:8081/h2-console
  - JDBC URL: `jdbc:h2:mem:userdb`
  - Username: `sa`
  - Password: （空白）

- **Order Service**: http://localhost:8082/h2-console
  - JDBC URL: `jdbc:h2:mem:orderdb`
  - Username: `sa`
  - Password: （空白）

- **Inventory Service**: http://localhost:8083/h2-console
  - JDBC URL: `jdbc:h2:mem:inventorydb`
  - Username: `sa`
  - Password: （空白）

## 🧪 APIテスト例

### 1. 在庫を作成

```bash
curl -X POST http://localhost:8083/api/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "productName": "ノートパソコン",
    "quantity": 100
  }'
```

### 2. ユーザーを作成

```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "山田太郎",
    "email": "yamada@example.com"
  }'
```

### 3. 注文を作成

```bash
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 2,
    "price": 150000.00
  }'
```

### 4. 全ユーザーを取得

```bash
curl http://localhost:8081/api/users
```

### 5. 全在庫を取得

```bash
curl http://localhost:8083/api/inventory
```

### 6. ユーザーの注文履歴を取得

```bash
curl http://localhost:8082/api/orders/user/1
```

## 📝 データモデル

### User（ユーザー）
```json
{
  "id": 1,
  "username": "山田太郎",
  "email": "yamada@example.com",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### Order（注文）
```json
{
  "id": 1,
  "userId": 1,
  "productId": 1,
  "quantity": 2,
  "price": 150000.00,
  "status": "PENDING",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

**注文ステータス:**
- `PENDING` - 保留中
- `CONFIRMED` - 確認済み
- `SHIPPED` - 出荷済み
- `DELIVERED` - 配達済み
- `CANCELLED` - キャンセル

### Inventory（在庫）
```json
{
  "id": 1,
  "productId": 1,
  "productName": "ノートパソコン",
  "quantity": 100,
  "reservedQuantity": 0,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

## 🔄 マイクロサービス間の連携

注文サービス（Order Service）は在庫サービス（Inventory Service）と連携します：

1. 注文作成時、在庫サービスに在庫の確認を行います
2. 在庫が十分にある場合、注文を作成し、在庫を削減します
3. Spring WebClient（リアクティブ）を使用した非同期通信を実装

```java
// Order Serviceでの在庫チェック例
private boolean checkInventory(Long productId, Integer quantity) {
    return webClientBuilder.build()
        .get()
        .uri("http://localhost:8083/api/inventory/check/{productId}/{quantity}", 
             productId, quantity)
        .retrieve()
        .bodyToMono(String.class)
        .block();
}
```

## 🛠️ 開発ツール

### IDEでの実行

**IntelliJ IDEA:**
1. プロジェクトを開く: `File > Open` → `microservices-demo`フォルダを選択
2. Mavenプロジェクトとして認識されるまで待つ
3. 各サービスの`Application.java`ファイルを右クリック
4. `Run 'Application'`を選択

**Eclipse:**
1. `File > Import > Existing Maven Projects`
2. `microservices-demo`フォルダを選択
3. 各サービスの`Application.java`ファイルを右クリック
4. `Run As > Java Application`を選択

### ログレベルの変更

各サービスの`application.yml`でログレベルを調整できます：

```yaml
logging:
  level:
    com.example: DEBUG  # DEBUGからINFO、WARNなどに変更可能
    org.springframework.web: INFO
```

## 📦 プロジェクト構造

```
microservices-demo/
│
├── pom.xml                                    # 親POM
├── README.md                                  # このファイル
├── start-all.sh                               # Linux/Mac起動スクリプト
├── start-all.bat                              # Windows起動スクリプト
│
├── user-service/                              # ユーザーサービス
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/example/user/
│       │   │       ├── UserServiceApplication.java
│       │   │       ├── controller/
│       │   │       │   └── UserController.java
│       │   │       ├── model/
│       │   │       │   └── User.java
│       │   │       ├── repository/
│       │   │       │   └── UserRepository.java
│       │   │       └── service/
│       │   │           └── UserService.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│
├── order-service/                             # 注文サービス
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/example/order/
│       │   │       ├── OrderServiceApplication.java
│       │   │       ├── controller/
│       │   │       │   └── OrderController.java
│       │   │       ├── model/
│       │   │       │   └── Order.java
│       │   │       ├── repository/
│       │   │       │   └── OrderRepository.java
│       │   │       └── service/
│       │   │           └── OrderService.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│
└── inventory-service/                         # 在庫サービス
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/
        │   │   └── com/example/inventory/
        │   │       ├── InventoryServiceApplication.java
        │   │       ├── controller/
        │   │       │   └── InventoryController.java
        │   │       ├── model/
        │   │       │   └── Inventory.java
        │   │       ├── repository/
        │   │       │   └── InventoryRepository.java
        │   │       └── service/
        │   │           └── InventoryService.java
        │   └── resources/
        │       └── application.yml
        └── test/
```

## 🐛 トラブルシューティング

### ポートが既に使用されているエラー

```
Port 8081 was already in use
```

**解決策:**
- 他のアプリケーションがポートを使用していないか確認
- `application.yml`でポート番号を変更

### Javaバージョンエラー

```
Unsupported class file major version XX
```

**解決策:**
- Java 21がインストールされているか確認
- `JAVA_HOME`環境変数が正しく設定されているか確認

### Maven依存関係エラー

**解決策:**
```bash
mvn clean install -U
```
`-U`フラグで依存関係を強制的に更新します。

### サービス間通信エラー

**解決策:**
1. 全サービスが起動しているか確認
2. 在庫サービスを最初に起動する
3. ファイアウォールがローカルホスト通信をブロックしていないか確認

## 🔐 セキュリティに関する注意

このプロジェクトはデモンストレーション目的です。本番環境では以下を実装してください：

- 認証・認可（Spring Security）
- APIゲートウェイ
- サービスディスカバリ（Eureka、Consulなど）
- 分散トレーシング（Zipkin、Jaegerなど）
- 集中ログ管理
- データベースのセキュリティ強化
- HTTPS通信

## 📚 参考リソース

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Framework Documentation](https://spring.io/projects/spring-framework)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Project Lombok](https://projectlombok.org/)

## 💡 拡張アイデア

このプロジェクトをさらに発展させるためのアイデア：

1. **Docker対応**
   - Dockerfileを追加
   - docker-composeで全サービスを一括起動

2. **Kubernetes対応**
   - デプロイメント設定ファイル
   - サービスメッシュの導入

3. **メッセージング**
   - RabbitMQまたはKafkaを使用した非同期通信

4. **API Gateway**
   - Spring Cloud Gatewayの導入
   - 統一されたエントリーポイント

5. **監視とモニタリング**
   - Prometheus + Grafana
   - ELKスタック（Elasticsearch、Logstash、Kibana）

6. **テスト強化**
   - 統合テスト
   - E2Eテスト
   - パフォーマンステスト

## 📄 ライセンス

このプロジェクトはデモンストレーション目的で作成されています。

## 👥 サポート

質問や問題がある場合は、プロジェクトのIssueセクションで報告してください。

---

**プロジェクト作成日**: 2024年
**対象**: Spring Boot 4 & Spring Framework 7学習者
**レベル**: 中級〜上級
