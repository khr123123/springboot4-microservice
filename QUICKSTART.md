# クイックスタートガイド

## 最速で始める方法

### 1. 解凍
```bash
unzip microservices-demo.zip
cd microservices-demo
```

### 2. 起動（自動）

**Linux/macOS:**
```bash
./start-all.sh
```

**Windows:**
```cmd
start-all.bat
```

### 3. 確認

ブラウザで以下を開く:
- http://localhost:8081/actuator/health (ユーザーサービス)
- http://localhost:8082/actuator/health (注文サービス)
- http://localhost:8083/actuator/health (在庫サービス)

### 4. APIテスト

```bash
./test-api.sh
```

または手動で:

```bash
# 在庫作成
curl -X POST http://localhost:8083/api/inventory \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "productName": "ノートPC", "quantity": 100}'

# ユーザー作成
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{"username": "山田太郎", "email": "yamada@example.com"}'

# 注文作成
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "productId": 1, "quantity": 2, "price": 150000.00}'
```

## トラブルシューティング

### Java 21が必要です
```bash
# Java確認
java -version
```

### ポートが使用中
`application.yml`でポート番号を変更してください

### 停止方法

**Linux/macOS:**
```bash
./stop-all.sh
```

**Windows:**
各コマンドウィンドウを閉じる

---

詳細は `README.md` をご覧ください。
