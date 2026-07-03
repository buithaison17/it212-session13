# Script tự động chuyển đổi SRS HTML sang PDF bằng Microsoft Edge Headless
$htmlPath = (Resolve-Path "SRS_TechNova_SmartEShop.html").Path
$pdfPath = Join-Path (Get-Item ".").FullName "SRS_TechNova_SmartEShop.pdf"

Write-Host "Đang chuyển đổi tài liệu đặc tả SRS từ HTML sang PDF..." -ForegroundColor Cyan
& "msedge" --headless --disable-gpu --print-to-pdf="$pdfPath" "$htmlPath"

if (Test-Path $pdfPath) {
    Write-Host "Đã tạo thành công file PDF tại: $pdfPath" -ForegroundColor Green
} else {
    Write-Host "Lỗi: Không thể sinh ra file PDF. Vui lòng kiểm tra lại Microsoft Edge đã được cài đặt chưa." -ForegroundColor Red
}
