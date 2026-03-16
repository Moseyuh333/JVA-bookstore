# JVA Bookstore - Hướng dẫn khởi động

## Các file khởi động

### 1. `start-server.bat` - Chỉ khởi động server local
- **Chức năng**: Build project và chạy server trên http://localhost:8081
- **Sử dụng**: Double-click file hoặc chạy trong terminal
- **Khi nào dùng**: Khi chỉ cần test local, không cần public URL

### 2. `start-tunnel.bat` - Chỉ khởi động Cloudflare tunnel
- **Chức năng**: Tạo public URL trỏ tới server đang chạy
- **Điều kiện**: Server phải đã chạy trước (port 8081)
- **Sử dụng**: Double-click file hoặc chạy trong terminal
- **Khi nào dùng**: Khi server đã chạy và cần expose ra internet

### 3. `start-all.ps1` - Khởi động tất cả (RECOMMENDED)
- **Chức năng**: Build + Start server + Start tunnel tất cả trong 1 lần
- **Sử dụng**: 
  ```powershell
  powershell -ExecutionPolicy Bypass -File start-all.ps1
  ```
  Hoặc chuột phải → Run with PowerShell
- **Khi nào dùng**: Sau khi tắt máy, khởi động lại toàn bộ

## Bước khởi động nhanh (sau khi tắt máy)

### Cách 1: Sử dụng PowerShell script (KHUYẾN NGHỊ)
1. Chuột phải vào `start-all.ps1`
2. Chọn "Run with PowerShell"
3. Chờ 15-20 giây
4. Copy public URL từ terminal

### Cách 2: Chạy riêng từng phần
1. **Terminal 1**: Double-click `start-server.bat`
   - Chờ thấy dòng "INFO: Started ContextHandler"
   
2. **Terminal 2**: Double-click `start-tunnel.bat`
   - Copy public URL từ output

## Tài khoản test

```
Username: shino113399
Password: 123456

Username: admin01
Password: 123456

Username: seller1
Password: 123456
```

## Database

- **Host**: localhost:5432
- **Database**: jva_bookstore
- **Username**: postgres
- **Password**: postgres

## Ports

- **Local Server**: http://localhost:8081
- **Public Tunnel**: https://[random].trycloudflare.com (thay đổi mỗi lần khởi động)

## Lưu ý quan trọng

1. **IPv4 Force**: Đã thêm `-Djava.net.preferIPv4Stack=true` để tránh lỗi IPv6
2. **Cloudflared URL**: Dùng `127.0.0.1` thay vì `localhost` để force IPv4
3. **Database**: PostgreSQL phải chạy trước khi start server
4. **Port 8081**: Đảm bảo không có ứng dụng nào khác dùng port này
5. **Quick Tunnel**: URL sẽ khác mỗi lần khởi động (không có uptime guarantee)

## Troubleshooting

### Lỗi "Port 8081 already in use"
```powershell
Get-NetTCPConnection -LocalPort 8081 | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

### Lỗi "Unable to reach the origin service"
- Kiểm tra server đã chạy: http://localhost:8081
- Kiểm tra firewall có block port 8081 không

### Lỗi "JAVA_HOME not defined"
- Kiểm tra JDK 17 đã cài: `C:\Program Files\Java\jdk-17`
- Chỉnh sửa đường dẫn trong file `.bat` hoặc `.ps1` nếu khác

## Dừng server

- **Trong terminal**: Nhấn `Ctrl + C`
- **Force kill**: 
  ```powershell
  Get-Process -Name java | Stop-Process -Force
  ```
