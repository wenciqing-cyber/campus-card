# Export the report docx to PDF with the locally installed WPS (or Word).
# Usage: powershell -NoProfile -ExecutionPolicy Bypass -File export_pdf.ps1
$ErrorActionPreference = "Stop"
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$doc = (Get-ChildItem $here -Filter *.docx | Where-Object { $_.Name -notlike "~$*" } | Select-Object -First 1).FullName
if (-not $doc) { throw "no docx found, run make_report.py first" }
$pdf = [System.IO.Path]::ChangeExtension($doc, ".pdf")
if (Test-Path $pdf) { Remove-Item $pdf -Force }

$app = New-Object -ComObject KWps.Application
$app.Visible = $false
$app.DisplayAlerts = 0
$d = $app.Documents.Open($doc)
try {
    $toc = $d.TablesOfContents
    if ($toc.Count -gt 0) { $toc.Item(1).Update() }
} catch { "toc skipped" }
try { $d.Fields.Update() | Out-Null } catch { "fields skipped" }
$d.Repaginate()
$d.ExportAsFixedFormat($pdf, 17)
$d.Close(0)
$app.Quit()
"exported=" + (Test-Path $pdf)
