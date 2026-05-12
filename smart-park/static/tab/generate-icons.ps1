Add-Type -AssemblyName System.Drawing

$fontPath = "d:\Users\Lenvov\Documents\HBuilderProjects\smart-park\static\iconfont\iconfont.ttf"
$tabDir = "d:\Users\Lenvov\Documents\HBuilderProjects\smart-park\static\tab"
$size = 64

# 加载字体
$fontBytes = [System.IO.File]::ReadAllBytes($fontPath)
$fontPtr = [System.Runtime.InteropServices.Marshal]::AllocHGlobal($fontBytes.Length)
[System.Runtime.InteropServices.Marshal]::Copy($fontBytes, 0, $fontPtr, $fontBytes.Length)

$pfc = New-Object System.Drawing.Text.PrivateFontCollection
$null = $pfc.AddMemoryFont($fontPtr, $fontBytes.Length)
[System.Runtime.InteropServices.Marshal]::FreeHGlobal($fontPtr)

$fontFamily = $pfc.Families[0]
Write-Host "Font loaded: $($fontFamily.Name)"

# Unicode 码点 (来自 iconfont.css)
$icons = @{
    "home"        = 0xE618  # icon-shouye-shouye
    "map"         = 0xEC3F  # icon-ditu-dibiao
    "my"          = 0xE61D  # icon-wode-copy
}

$gray = [System.Drawing.Color]::FromArgb(153, 153, 153)
$blue = [System.Drawing.Color]::FromArgb(59, 134, 255)

function Render-Icon($name, $codePoint, $color, $suffix) {
    $outputPath = Join-Path $tabDir "$name$suffix.png"
    
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $g.Clear([System.Drawing.Color]::Transparent)
    
    $text = [char]::ConvertFromUtf32($codePoint)
    $font = New-Object System.Drawing.Font($fontFamily, ($size * 0.7), [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
    $brush = New-Object System.Drawing.SolidBrush($color)
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    
    $g.DrawString($text, $font, $brush, [System.Drawing.RectangleF]::new(0, 0, $size, $size), $sf)
    
    $bmp.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    
    $g.Dispose()
    $bmp.Dispose()
    $font.Dispose()
    $brush.Dispose()
    $sf.Dispose()
    
    Write-Host "Generated: $outputPath"
}

# 生成所有图标
foreach ($key in $icons.Keys) {
    Render-Icon $key $icons[$key] $gray ""
    Render-Icon $key $icons[$key] $blue "-active"
}

Write-Host "All icons generated!"
$pfc.Dispose()
