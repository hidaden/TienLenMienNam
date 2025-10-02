# Tien Len Mien Nam - Protocol v1.0

## Định dạng chung
- Cấu trúc: `COMMAND|PAYLOAD_1|PAYLOAD_2|...`
- Dữ liệu lá bài: `3S`, `KC` (Rank + Suit).
- Dữ liệu nước đi: Các lá bài cách nhau bởi dấu phẩy `,`. Ví dụ: `3S,4C,5D`.

---

## Client -> Server Commands

| Lệnh                     | Ví dụ                     | Mô tả                                      |
| ------------------------ | ------------------------- | ------------------------------------------ |
| `SET_NAME\|player_name`  | `SET_NAME\|John`          | Đặt tên cho người chơi khi mới kết nối.    |
| `PLAY\|card_1,card_2,...`| `PLAY\|5S,5H`             | Đánh một bộ bài khi đến lượt.              |
| `PASS`                   | `PASS`                    | Bỏ lượt khi không thể hoặc không muốn đánh. |

---

## Server -> Client Commands

| Lệnh                                       | Ví dụ                             | Mô tả                                                        |
| ------------------------------------------ | --------------------------------- | ------------------------------------------------------------ |
| `MSG\|message`                             | `MSG\|Waiting for players...`     | Gửi một tin nhắn thông báo chung.                            |
| `GAME_START\|card_1,...`                   | `GAME_START\|3S,4H,...`           | Bắt đầu ván chơi, gửi 13 lá bài cho client.                 |
| `YOUR_TURN\|last_move`                     | `YOUR_TURN\|4C,5C,6C`             | Thông báo đến lượt của client. `last_move` có thể rỗng.       |
| `UPDATE_GAME\|player\|action\|cards`       | `UPDATE_GAME\|Bob\|PLAY\|8D,8H`   | Cập nhật cho mọi người về một hành động vừa diễn ra.          |
| `INVALID_MOVE\|reason`                     | `INVALID_MOVE\|Not a valid combo` | Báo cho client biết nước đi của họ không hợp lệ.             |
| `GAME_OVER\|winner`                        | `GAME_OVER\|Alice`                | Thông báo game kết thúc và người chiến thắng.                 |